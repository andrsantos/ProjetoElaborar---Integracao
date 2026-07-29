package com.Projeto.GeradorDeQuestoes.services;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.Projeto.GeradorDeQuestoes.dto.QuestaoDTO;
import com.Projeto.GeradorDeQuestoes.entities.ExtracaoJobEntity;
import com.Projeto.GeradorDeQuestoes.entities.UsuarioEntity;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;

@Service
public class IngestaoMaterialService {

    private final VectorStore vectorStore;
    private final ObjectMapper objectMapper;
    private final ChatClient anthropicChatClient;
    private final ChatClient openAiChatClient;
    private final ExtracaoJobService jobService;
    private final CobrancaLlmService cobrancaLlmService;
    private final CarteiraService carteiraService;

    public IngestaoMaterialService(VectorStore vectorStore, 
       @Qualifier("anthropicChatClient") ChatClient anthropicChatClient,
       @Qualifier("openAiChatClient") ChatClient openAiChatClient,
       ExtracaoJobService jobService,
       CobrancaLlmService cobrancaLlmService, CarteiraService carteiraService) {
        
        this.vectorStore = vectorStore;
        this.anthropicChatClient = anthropicChatClient;
        this.openAiChatClient = openAiChatClient;
        this.jobService = jobService;
        this.cobrancaLlmService = cobrancaLlmService;
        this.objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.carteiraService = carteiraService;
    }

    public void importarCapituloLivroDificil(Resource pdfResource, String topico, String fonte) {
        processarRAG(pdfResource, topico, fonte, "universitario_avancado");
    }

    public void importarCapituloLivroMedio(Resource pdfResource, String topico, String fonte) {
        processarRAG(pdfResource, topico, fonte, "universitario_intermediario");
    }

    public void importarCapituloLivroFacil(Resource pdfResource, String topico, String fonte) {
        processarRAG(pdfResource, topico, fonte, "universitario_iniciante");
    }

    private void processarRAG(Resource pdfResource, String topico, String fonte, String nivel) {
        TikaDocumentReader pdfReader = new TikaDocumentReader(pdfResource);
        List<Document> documentosBrutos = pdfReader.get();
        
        System.out.println("Documentos lidos pelo Tika: " + documentosBrutos.size());
        if (!documentosBrutos.isEmpty()) {
            String conteudo = documentosBrutos.get(0).getText();
            System.out.println("Tamanho do texto extraído: " + (conteudo != null ? conteudo.length() : "NULO"));
            System.out.println("Início do texto: " + (conteudo != null && conteudo.length() > 100 
                ? conteudo.substring(0, 100) : conteudo));
        }
        
        TokenTextSplitter splitter = new TokenTextSplitter(1500, 400, 10, 5000, true);
        List<Document> chunks = splitter.apply(documentosBrutos);
        
        chunks.forEach(chunk -> {
            chunk.getMetadata().put("topico", topico);
            chunk.getMetadata().put("fonte", fonte);
            chunk.getMetadata().put("nivel_material", nivel);
        });
        
        this.vectorStore.accept(chunks);
        System.out.println("Sucesso: " + chunks.size() + " fragmentos [" + nivel + "] importados.");
    }

    public List<QuestaoDTO> processarPdfParaQuestoes(File pdfFile, String promptPersonalizado, String modoExtracao, 
                                                     UsuarioEntity usuario, AtomicBoolean saldoEsgotado) {
        
        List<String> jsonsBrutos = extrairTextoDePdf(pdfFile, usuario, saldoEsgotado);
        System.out.println("JSONs brutos extraídos: " + jsonsBrutos.size() + " blocos processados.");
        
        List<QuestaoDTO> questoesExtraidas = filtrarQuestoesValidas(jsonsBrutos);
        
        for (QuestaoDTO questao : questoesExtraidas) {
            if (questao.getRespostaCorreta() == null || questao.getRespostaCorreta().trim().isEmpty()) {
                System.out.println("[PLANO B] Resolvendo questão sem gabarito oficial: " + questao.getId());
                try {
                    String respostaCalculada = resolverQuestaoSemGabarito(questao, usuario);
                    respostaCalculada = respostaCalculada.replaceAll("[^A-E]", ""); 
                    if (!respostaCalculada.isEmpty()) {
                        questao.setRespostaCorreta(respostaCalculada);
                        questao.setGabaritoGeradoPorIa(true);
                    }
                } catch (Exception e) {
                    System.err.println("Falha ao resolver a questão " + questao.getId() + ": " + e.getMessage());
                }
            }
        }
        
        if ("APENAS_ORIGINAIS".equals(modoExtracao) || saldoEsgotado.get()) {
            System.out.println("Modo APENAS_ORIGINAIS ou Saldo Esgotado detectado. Pulando o agente revisor.");
            return questoesExtraidas; 
        }

        List<QuestaoDTO> questoesRevisadas = chamarAgenteRevisor(questoesExtraidas, promptPersonalizado, modoExtracao, usuario, saldoEsgotado);
        embaralharLoteDeQuestoes(questoesRevisadas);
        System.out.println("Saldo atual após operação:" + carteiraService.consultarSaldoAtual(usuario));
        return questoesRevisadas;
    }

    public List<QuestaoDTO> enriquecerQuestoes(List<QuestaoDTO> questoes) {
        return questoes;
    }

    public List<QuestaoDTO> chamarAgenteRevisor(List<QuestaoDTO> questoes, String promptPersonalizado, String modoExtracao, 
                                                UsuarioEntity usuario, AtomicBoolean saldoEsgotado) {
        if (questoes == null || questoes.isEmpty()) return questoes;

        List<QuestaoDTO> todasRevisadas = new ArrayList<>();
        int tamanhoLote = 2; 

        for (int i = 0; i < questoes.size(); i += tamanhoLote) {
            int fim = Math.min(i + tamanhoLote, questoes.size());
            List<QuestaoDTO> loteAtual = questoes.subList(i, fim);
            
            if (saldoEsgotado.get()) {
                todasRevisadas.addAll(loteAtual);
                continue;
            }

            System.out.println("Agente Revisor: Processando lote " + (i/tamanhoLote + 1) + " em modo " + modoExtracao);
            
            try {
                String jsonLote = objectMapper.writeValueAsString(loteAtual);

                if ("APENAS_VARIACOES".equals(modoExtracao)) {
                    System.out.println("-> Iniciando Etapa 1: Extração do Mapa Conceitual...");
                    String promptAnalista = """
                        Você é um Agente Analista de Avaliações.
                        Sua tarefa é analisar o lote de questões e desconstruir o conhecimento.
                        
                        Retorne EXCLUSIVAMENTE um array JSON contendo objetos com a exata estrutura abaixo:
                        [
                          {
                            "idOriginal": "Q123",
                            "conceitoCentral": "O tema principal da questão",
                            "competencia": "O que a questão avalia do aluno",
                            "todasAsPropriedades": ["Liste de 6 a 10 características, regras, subconceitos ou exceções sobre o conceito central"],
                            "propriedadesExploradas": ["Liste APENAS as propriedades que foram cobradas/citadas no texto e alternativas desta questão específica"]
                          }
                        ]
                        Não inclua as alternativas ou o enunciado no retorno. Retorne apenas o array JSON.
                        """;
                    
                    ChatResponse respostaAnalista = this.anthropicChatClient.prompt(promptAnalista + "\n\nLOTE DE QUESTÕES ORIGINAIS:\n" + jsonLote)
                        .options(ChatOptions.builder().temperature(0.0).maxTokens(2000).build())
                        .call().chatResponse();
                        
                    Usage usageAnalista = respostaAnalista.getMetadata().getUsage();
                    cobrancaLlmService.deduzirCusto(usuario, usageAnalista.getPromptTokens(), usageAnalista.getCompletionTokens(), "claude-haiku");

                    String jsonGrafo = respostaAnalista.getResult().getOutput().getText().replaceAll("(?s)```json\\s*|```", "").trim();
                    jsonGrafo = garantirFechamentoJson(jsonGrafo);

                    com.fasterxml.jackson.databind.JsonNode grafoArray = objectMapper.readTree(jsonGrafo);
                    StringBuilder instrucoesCriadorPorQuestao = new StringBuilder();

                    List<String> formatos = java.util.Arrays.asList(
                        "Estudo de Caso / Cenário Prático (Mundo Real)",
                        "Associação de Conceitos (Relacionar colunas ou itens)",
                        "Análise de Múltiplas Afirmações (I, II, III e IV)",
                        "Diálogo / Debate entre personagens ou profissionais",
                        "Inversão Lógica (Focar na exceção ou no caminho inverso)",
                        "Sequência de Eventos / Ordenação de Processos"
                    );
                    
                    List<String> raciocinios = java.util.Arrays.asList(
                        "Diagnóstico / Identificação da Causa Raiz",
                        "Predição de Resultado (O que acontecerá se...)",
                        "Comparação Analítica entre abordagens",
                        "Aplicação Prática de Regra ou Teoria",
                        "Inferência / Dedução a partir de informações incompletas",
                        "Eliminação de Soluções Inviáveis"
                    );

                    for (com.fasterxml.jackson.databind.JsonNode node : grafoArray) {
                        String idOriginal = node.path("idOriginal").asText();
                        String conceito = node.path("conceitoCentral").asText();
                        String competencia = node.path("competencia").asText();
                        
                        List<String> todasPropriedades = new ArrayList<>();
                        node.path("todasAsPropriedades").forEach(p -> todasPropriedades.add(p.asText()));
                        
                        List<String> exploradas = new ArrayList<>();
                        node.path("propriedadesExploradas").forEach(p -> exploradas.add(p.asText()));

                        List<String> livres = new ArrayList<>(todasPropriedades);
                        livres.removeAll(exploradas);
                        java.util.Collections.shuffle(livres);

                        String pVar1A = livres.size() > 0 ? livres.get(0) : todasPropriedades.get(0);
                        String pVar1B = livres.size() > 1 ? livres.get(1) : todasPropriedades.get(todasPropriedades.size() - 1);
                        String pVar2A = livres.size() > 2 ? livres.get(2) : todasPropriedades.get(0);
                        String pVar2B = livres.size() > 3 ? livres.get(3) : todasPropriedades.get(todasPropriedades.size() - 1);

                        java.util.Collections.shuffle(formatos);
                        java.util.Collections.shuffle(raciocinios);

                        instrucoesCriadorPorQuestao.append("--- PARA A QUESTÃO ORIGINAL ID: ").append(idOriginal).append(" ---\n");
                        instrucoesCriadorPorQuestao.append("Conceito Central: ").append(conceito).append("\n");
                        instrucoesCriadorPorQuestao.append("Competência Avaliada: ").append(competencia).append("\n");
                        instrucoesCriadorPorQuestao.append("-> INSTRUÇÃO VAR 1:\n");
                        instrucoesCriadorPorQuestao.append("   - Use o Formato: [").append(formatos.get(0)).append("]\n");
                        instrucoesCriadorPorQuestao.append("   - Use o Raciocínio: [").append(raciocinios.get(0)).append("]\n");
                        instrucoesCriadorPorQuestao.append("   - Propriedades OBRIGATÓRIAS a serem avaliadas: '").append(pVar1A).append("' e '").append(pVar1B).append("'\n");
                        instrucoesCriadorPorQuestao.append("-> INSTRUÇÃO VAR 2:\n");
                        instrucoesCriadorPorQuestao.append("   - Use o Formato: [").append(formatos.get(1)).append("]\n");
                        instrucoesCriadorPorQuestao.append("   - Use o Raciocínio: [").append(raciocinios.get(1)).append("]\n");
                        instrucoesCriadorPorQuestao.append("   - Propriedades OBRIGATÓRIAS a serem avaliadas: '").append(pVar2A).append("' e '").append(pVar2B).append("'\n\n");
                    }

                    System.out.println("-> Iniciando Etapa 3: Geração com Criador Cego...");
                    String instrucaoCriador = """
                        Você é um Agente Pedagógico Criador.
                        Sua missão é gerar um array JSON com DUAS QUESTÕES INÉDITAS para cada bloco de instruções fornecido abaixo.
                        ATENÇÃO: Você propositalmente NÃO receberá o texto das questões originais. Você deve criar as questões inteiramente do zero, baseando-se APENAS no 'Conceito', na 'Competência', nos 'Formatos/Raciocínios' exigidos e focando EXCLUSIVAMENTE nas 'Propriedades Obrigatórias'.

                        REGRAS MANDATÓRIAS:
                        1. NOVIDADE ESTRITA: A questão deve depender do conhecimento das 'Propriedades Obrigatórias' exigidas para ser resolvida.
                        2. DISTRATORES ANTI-VÍCIO: Cada alternativa incorreta deve representar um erro conceitual DIFERENTE e independente. Não crie alternativas que explorem o mesmo equívoco com palavras diferentes.
                        
                        3. SIMETRIA VISUAL E VERBOSIDADE (CRÍTICO): Todas as 5 alternativas devem ter comprimentos de texto rigorosamente semelhantes. É ESTRITAMENTE PROIBIDO que a alternativa correta seja visivelmente mais longa, mais detalhada ou mais explicativa que as incorretas. Desenvolva os distratores com o mesmo nível de detalhamento, tom acadêmico e complexidade textual da resposta certa.
                        
                        4. ESTRUTURA DO JSON:
                        - No campo 'id', use o prefixo 'VAR1-' e 'VAR2-' seguido do ID fornecido no bloco de instrução.
                        - Preencha todos os campos: id, enunciado, alternativas (A a E), respostaCorreta, explicacao, conceito, competencia, comentarioTecnico, topico e nivel.
                        - O campo tópico DEVE SER o nome da disciplina mais adequada ao conceito.
                        - Alterne os níveis (UNIVERSITARIO_INICIANTE, UNIVERSITARIO_INTERMEDIARIO, UNIVERSITARIO_AVANCADO).
                        - Certifique-se de que existe uma e APENAS UMA alternativa correta.
                        
                        Retorne EXCLUSIVAMENTE o array JSON [].
                        """;

                    if (promptPersonalizado != null && !promptPersonalizado.trim().isEmpty()) {
                        instrucaoCriador += "\nATENÇÃO - INSTRUÇÕES DE TOM/ESTILO DO USUÁRIO (Aplique em todas as questões):\n" + promptPersonalizado + "\n";
                    }

                    String promptFinal = instrucaoCriador + 
                                         "\n\n=== INSTRUÇÕES DE GERAÇÃO (CRIADOR CEGO) ===\n" + instrucoesCriadorPorQuestao.toString();

                    ChatResponse respostaCriador = this.anthropicChatClient.prompt(promptFinal)
                            .options(ChatOptions.builder().temperature(0.3).maxTokens(4000).build())
                            .call().chatResponse();

                    Usage usageCriador = respostaCriador.getMetadata().getUsage();
                    cobrancaLlmService.deduzirCusto(usuario, usageCriador.getPromptTokens(), usageCriador.getCompletionTokens(), "claude-haiku");

                    String cleanJsonCriador = respostaCriador.getResult().getOutput().getText().replaceAll("(?s)```json\\s*|```", "").trim();
                    cleanJsonCriador = garantirFechamentoJson(cleanJsonCriador);

                    List<QuestaoDTO> loteRevisado = objectMapper.readValue(cleanJsonCriador,
                            new com.fasterxml.jackson.core.type.TypeReference<List<QuestaoDTO>>() {});
                    
                    todasRevisadas.addAll(loteRevisado);

                } else {
                    String instrucao = """
                        Você é um Agente Pedagógico Sênior especializado em engenharia de avaliações.
                        Sua missão é ler o lote de questões originais abaixo e criar UMA VARIAÇÃO INÉDITA para cada uma delas, alocando-a no campo 'questaoInspirada'.

                        REGRAS MANDATÓRIAS DE ESTRUTURA E PRESERVAÇÃO:
                        1. INTOCABILIDADE DA ORIGINAL: Você é ESTRITAMENTE PROIBIDO de alterar o 'enunciado', as 'alternativas', o 'gabarito' ou qualquer outro dado da questão original na raiz do JSON. Eles devem ser retornados exatamente como foram enviados.
                        2. A QUESTÃO INSPIRADA: O campo 'questaoInspirada' deve ser um OBJETO JSON completo. É EXCLUSIVAMENTE AQUI que você deve aplicar sua criatividade e as instruções personalizadas do usuário.
                        - Mude o cenário, os valores e as marcas citadas para evitar plágio, mas mantenha a competência avaliada.
                        - No campo 'id' da questaoInspirada, use o prefixo 'INS-' seguido do ID original.
                        - Preencha os campos da questão inspirada: explicacao, conceito, competencia, comentarioTecnico, topico e nivel.
                        - O campo tópico deve conter a grande área do conhecimento.
                        - No campo 'nivel', use EXCLUSIVAMENTE: UNIVERSITARIO_INICIANTE, UNIVERSITARIO_INTERMEDIARIO ou UNIVERSITARIO_AVANCADO.
                        - Faça as questões inspiradas com 5 alternativas: A,B,C,D e E. Se a original não tiver letra E, mesmo assim coloque letra E na inspirada.
                        
                        3. SIMETRIA VISUAL E VERBOSIDADE (CRÍTICO): Todas as 5 alternativas da 'questaoInspirada' devem ter comprimentos de texto rigorosamente semelhantes. É ESTRITAMENTE PROIBIDO que a alternativa correta seja visivelmente mais longa, mais detalhada ou mais explicativa que as incorretas.
                        
                        REGRAS DE FORMATAÇÃO DA SAÍDA:
                        - Retorne APENAS o array JSON [].
                        - NUNCA envie texto livre ou explicações fora do JSON.
                        """;

                    if (promptPersonalizado != null && !promptPersonalizado.trim().isEmpty()) {
                        instrucao += "\n\nATENÇÃO - INSTRUÇÕES ESPECÍFICAS DO USUÁRIO:\n" + promptPersonalizado + "\n";
                    }

                    ChatResponse respostaIA = this.anthropicChatClient.prompt(instrucao + "\n\n LOTE DE ENTRADA:\n" + jsonLote)
                            .options(ChatOptions.builder().temperature(0.0).maxTokens(4000).build())
                            .call().chatResponse();

                    Usage usageIA = respostaIA.getMetadata().getUsage();
                    cobrancaLlmService.deduzirCusto(usuario, usageIA.getPromptTokens(), usageIA.getCompletionTokens(), "claude-haiku");

                    String cleanJson = respostaIA.getResult().getOutput().getText().replaceAll("(?s)```json\\s*|```", "").trim();
                    cleanJson = garantirFechamentoJson(cleanJson);

                    List<QuestaoDTO> loteRevisado = objectMapper.readValue(cleanJson,
                            new com.fasterxml.jackson.core.type.TypeReference<List<QuestaoDTO>>() {});
                    
                    todasRevisadas.addAll(loteRevisado);
                }
                
            } catch (RuntimeException e) {
                if (e.getMessage() != null && e.getMessage().contains("Saldo insuficiente")) {
                    System.err.println("Saldo esgotado durante a revisão do lote " + (i/tamanhoLote + 1));
                    saldoEsgotado.set(true);
                } else {
                    System.err.println("Erro crítico no lote " + (i/tamanhoLote + 1) + ": " + e.getMessage());
                }
                todasRevisadas.addAll(loteAtual);
            } catch (Exception e) {
                System.err.println("Erro crítico no lote " + (i/tamanhoLote + 1) + ": " + e.getMessage());
                todasRevisadas.addAll(loteAtual);
            }
        }
        return todasRevisadas;
    }

    private void embaralharLoteDeQuestoes(List<QuestaoDTO> questoes) {
        if (questoes == null || questoes.isEmpty()) {
            return;
        }
        for (QuestaoDTO questao : questoes) {
            embaralharAlternativas(questao);
        }
    }

    private void embaralharAlternativas(QuestaoDTO questao) {
        if (questao.getAlternativas() == null || questao.getAlternativas().isEmpty()) {
            return; 
        }

        Map<String, String> alternativas = questao.getAlternativas();
        String letraCorretaAtual = questao.getRespostaCorreta();
        
        String textoRespostaCorreta = alternativas.get(letraCorretaAtual);

        List<String> textosEmbaralhados = new java.util.ArrayList<>(alternativas.values());
        java.util.Collections.shuffle(textosEmbaralhados);

        Map<String, String> novasAlternativas = new java.util.LinkedHashMap<>();
        String[] letras = {"A", "B", "C", "D", "E"};

        for (int i = 0; i < Math.min(textosEmbaralhados.size(), letras.length); i++) {
            String textoAtual = textosEmbaralhados.get(i);
            String letraAtual = letras[i];
            
            novasAlternativas.put(letraAtual, textoAtual);

            if (textoAtual != null && textoAtual.equals(textoRespostaCorreta)) {
                questao.setRespostaCorreta(letraAtual);
            }
        }

        questao.setAlternativas(novasAlternativas);
    }

    private String garantirFechamentoJson(String json) {
        long abertos = json.chars().filter(ch -> ch == '{').count();
        long fechados = json.chars().filter(ch -> ch == '}').count();
        StringBuilder sb = new StringBuilder(json);
        while (fechados < abertos) {
            sb.append("}");
            fechados++;
        }
        if (!json.trim().endsWith("]")) sb.append("]");
        return sb.toString();
    }

    private String extrairTextoPagina(PDDocument document, int numeroPagina) throws Exception {
        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setStartPage(numeroPagina);
        stripper.setEndPage(numeroPagina);
        String textoNativo = stripper.getText(document);

        if (isPaginaComTexto(textoNativo)) {
            System.out.println("Página " + numeroPagina + ": texto nativo extraído.");
            return textoNativo;
        }

        System.out.println("Página " + numeroPagina + ": sem texto nativo, usando OCR...");
        PDFRenderer renderer = new PDFRenderer(document);
        BufferedImage imagem = renderer.renderImageWithDPI(numeroPagina - 1, 300);

        ITesseract tesseract = new Tesseract();
        tesseract.setDatapath("C:/Program Files/Tesseract-OCR/tessdata");
        tesseract.setLanguage("por");

        String textoOcr = tesseract.doOCR(imagem);
        imagem.flush();
        return textoOcr;
    }

    private boolean isPaginaComTexto(String texto) {
        if (texto == null || texto.isBlank()) return false;
        long caracteresAlfabeticos = texto.chars()
                .filter(Character::isLetter)
                .count();
        return caracteresAlfabeticos > 50;
    }

    public List<String> extrairTextoDePdf(File pdfFile, UsuarioEntity usuario, AtomicBoolean saldoEsgotado) {
        List<String> resultadosJson = new ArrayList<>();

        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            int totalPaginas = document.getNumberOfPages();

            System.out.println("Lendo o PDF completo para mapeamento global...");
            StringBuilder textoCompletoBuilder = new StringBuilder();
            for (int i = 1; i <= totalPaginas; i++) {
                textoCompletoBuilder.append(extrairTextoPagina(document, i)).append("\n");
            }
            String textoCompleto = textoCompletoBuilder.toString();

            System.out.println("Procurando gabaritos no documento (Agnóstico a formato)...");
            
            String gabaritoGlobal = "";
            try {
                gabaritoGlobal = mapearGabaritosGlobais(textoCompleto, usuario);
                System.out.println("=== GABARITO GLOBAL MAPEADO ===\n" + gabaritoGlobal);
            } catch (RuntimeException e) {
                if (e.getMessage() != null && e.getMessage().contains("Saldo insuficiente")) {
                    saldoEsgotado.set(true);
                    return resultadosJson;
                }
                throw e;
            }

            for (int i = 1; i <= totalPaginas; i++) {
                if (saldoEsgotado.get()) break; 

                String textoPagina = extrairTextoPagina(document, i);
                if (!textoPagina.isBlank()) {
                    System.out.println("Processando questões da página " + i + "/" + totalPaginas);
                    try {
                        resultadosJson.add(IAQuestaoParser(textoPagina, gabaritoGlobal, usuario));
                    } catch (RuntimeException e) {
                        if (e.getMessage() != null && e.getMessage().contains("Saldo insuficiente")) {
                            System.err.println("Saldo esgotado na extração da página " + i + ". Interrompendo...");
                            saldoEsgotado.set(true);
                            break;
                        }
                        throw e;
                    }
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Falha no pipeline de extração: " + e.getMessage(), e);
        }
        return resultadosJson;
    }

    public String mapearGabaritosGlobais(String textoCompleto, UsuarioEntity usuario) {
         String instrucao = """
             OBJETIVO: Você é um extrator especialista em gabaritos de provas de concursos e exames.
             Sua única tarefa é vasculhar o texto completo do documento fornecido e encontrar as respostas (gabaritos) das questões.
            
             O gabarito pode estar:
             - Em uma tabela condensada no final ou início do documento.
             - Misturado no meio do texto, logo após o enunciado ou as alternativas de cada questão.
             - No formato ID da Questão e Resposta.
            
             Retorne APENAS uma lista limpa no formato "Identificador: Resposta".
             Exemplos esperados:
             1: A
             2: C
             Q3976030: B
            
             Se não encontrar ABSOLUTAMENTE NENHUM gabarito no texto, retorne APENAS a palavra: AUSENTE.
             NÃO retorne nenhum texto extra, introdução, explicação ou formatação markdown.
             """;

         ChatResponse response = this.openAiChatClient.prompt(instrucao + "\n\nTEXTO DO DOCUMENTO:\n" + textoCompleto)
                 .options(ChatOptions.builder()
                         .temperature(0.0) 
                         .maxTokens(1024) 
                         .build())
                 .call().chatResponse();
                 
         Usage usage = response.getMetadata().getUsage();
         cobrancaLlmService.deduzirCusto(usuario, usage.getPromptTokens(), usage.getCompletionTokens(), "gpt-4o");

         return response.getResult().getOutput().getText();
    }

    public String resolverQuestaoSemGabarito(QuestaoDTO questao, UsuarioEntity usuario) {
        String instrucao = """
            OBJETIVO: Você é um professor especialista resolvendo uma questão de prova.
            Leia o enunciado e as alternativas fornecidas.
            
            Sua tarefa é resolver a questão e retornar EXCLUSIVAMENTE a letra correspondente à alternativa correta (A, B, C, D ou E).
            NÃO forneça explicações, justificativas ou textos adicionais. Apenas a letra maiúscula.
            """;
        StringBuilder textoQuestao = new StringBuilder();
        textoQuestao.append("ENUNCIADO: ").append(questao.getEnunciado()).append("\n\nALTERNATIVAS:\n");
        
        if (questao.getAlternativas() != null) {
            questao.getAlternativas().forEach((letra, texto) -> 
                textoQuestao.append(letra).append(") ").append(texto).append("\n")
            );
        }

        ChatResponse response = this.openAiChatClient.prompt(instrucao + "\n\nQUESTÃO:\n" + textoQuestao.toString())
                .options(ChatOptions.builder()
                        .temperature(0.2) 
                        .maxTokens(10) 
                        .build())
                .call().chatResponse();
                
        Usage usage = response.getMetadata().getUsage();
        cobrancaLlmService.deduzirCusto(usuario, usage.getPromptTokens(), usage.getCompletionTokens(), "gpt-4o");
        return response.getResult().getOutput().getText().trim();
    }

    public String IAQuestaoParser(String textoPagina, String textoGabarito, UsuarioEntity usuario) {
        String instrucao = """
            OBJETIVO: Você é um extrator e conversor de dados JSON estrito. Converta o texto abaixo em um array JSON.

            ESTRUTURA OBRIGATÓRIA (RESPEITE OS NOMES DOS CAMPOS):
            [
            {
                "id": "código original da questão (ex: Q3976030)",
                "enunciado": "texto completo e limpo do enunciado",
                "alternativas": {
                "A": "texto da alternativa A",
                "B": "texto da alternativa B",
                "C": "texto da alternativa C",
                "D": "texto da alternativa D",
                "E": "texto da alternativa E"
                },
                "gabarito": "letra correta (ex: A)"
            }
            ]

            REGRAS CRÍTICAS:
            1. ASPAS: Use EXCLUSIVAMENTE aspas duplas (") em TODO o JSON — chaves E valores.
            NUNCA use aspas simples, barras invertidas ou escapes desnecessários.
            Se o texto original tiver aspas, substitua por aspas simples (') internamente.
            Se o texto original tiver barras invertidas antes de aspas, remova-as.

            2. VALORES SEMPRE STRING EM UMA ÚNICA LINHA: Todo valor deve ser string entre aspas duplas.
            ERRADO: "A": I        CORRETO: "A": "I"
            ERRADO: "A": I e II   CORRETO: "A": "I e II"

            3. ID DA QUESTÃO: O ID no formato Q seguido de números (ex: Q3976030) pode aparecer
            antes OU depois do enunciado. Procure em todo o bloco da questão.
            PRESERVE o ID COMPLETO — nunca trunce ou abrevie (ex: Q3973813, não Q3813).
            Se não encontrar nenhum ID, use: "id": ""

            4. GABARITO (QUESTÕES ORIGINAIS): O CONTEXTO DE APOIO contém gabaritos no formato "N: LETRA"
            onde N é o número sequencial da questão na prova.
            Se não encontrar, use: "gabarito": ""
            Questões originais de múltipla escolha com gabarito vazio são ACEITAS.

            5. INTEGRIDADE ABSOLUTA: Cada questão tem seu próprio enunciado e suas próprias
            alternativas. NUNCA misture partes de questões diferentes.
            O enunciado termina onde começam as alternativas (A, B, C...).
            As alternativas de uma questão terminam onde começa o enunciado da próxima.

            6. QUESTÕES COM IMAGEM (OMISSÃO OBRIGATÓRIA): OMITA, sem exceções, questões que referenciem 
            figuras, imagens, tabelas ou diagramas externos necessários para responder (ex: "Com base na figura", 
            "Observe o diagrama", "Com base nessa mensagem" quando a mensagem não estiver no texto).

            7. CONVERSÃO DE QUESTÕES DISCURSIVAS (NOVA REGRA): 
            Se você encontrar uma questão que originalmente é DISCURSIVA (aberta, sem alternativas A, B, C, D, E) 
            e que NÃO dependa de imagens (conforme Regra 6), você DEVE convertê-la em uma questão de múltipla escolha.
            Para fazer isso:
            - Mantenha o enunciado original.
            - Formule 1 resposta perfeitamente correta baseada no enunciado.
            - Formule 4 alternativas distratoras plausíveis, porém incorretas.
            - Defina o campo "gabarito" com a letra da alternativa que você criou como correta.

            8. ALTERNATIVAS AUSENTES (QUESTÕES ORIGINAIS): Se a questão era originalmente de múltipla escolha, 
            mas o PDF falhou na extração e ela ficou com menos de 4 alternativas com texto, OMITA a questão inteira. 
            (Esta regra não se aplica às discursivas que você mesmo converteu na Regra 7).

            9. LIMPEZA: Remova numeração de página, cabeçalhos, rodapés e metadados da banca
            (ex: "Ano: 2026  Banca: CESPE  Órgão: ...").

            10. RESPOSTA VAZIA: Se não houver questões válidas, retorne exatamente: []

            11. SEM TEXTO EXTRA: Retorne APENAS o array JSON válido, sem explicações ou markdown.

            12. JSON VÁLIDO: Certifique-se de que todos os objetos e arrays estão fechados.
                Não deixe nenhum campo incompleto — se não conseguir extrair um campo completo,
                OMITA a questão inteira em vez de deixar o JSON mal formado.
            """;

        String promptCompleto = instrucao +
                "\n\nCONTEXTO DE APOIO (GABARITOS NO FORMATO 'N: LETRA'):\n" + textoGabarito +
                "\n\nTEXTO PARA EXTRAÇÃO:\n" + textoPagina;

        ChatResponse response = this.anthropicChatClient.prompt(promptCompleto)
                .options(ChatOptions.builder()
                        .temperature(0.0)
                        .maxTokens(4096)
                        .build())
                .call().chatResponse();

        Usage usage = response.getMetadata().getUsage();
        cobrancaLlmService.deduzirCusto(usuario, usage.getPromptTokens(), usage.getCompletionTokens(), "claude-haiku");

        return response.getResult().getOutput().getText();
    }

    public List<QuestaoDTO> filtrarQuestoesValidas(List<String> paginasJson) {
        Set<String> idsVistos = new HashSet<>();
        Set<String> enunciadosVistos = new HashSet<>();

        return paginasJson.stream()
            .flatMap(json -> {
                try {
                    String cleanJson = json.replaceAll("(?s)```json\\s*|```", "").trim();
                    cleanJson = cleanJson.replace("\\\"", "'");

                    JsonNode root;
                    try {
                        root = objectMapper.readTree(cleanJson);
                    } catch (Exception e) {
                        System.err.println("JSON malformado — tentando recuperação parcial: " + e.getMessage());
                        root = tentarRecuperarJson(cleanJson);
                        if (root == null) return Stream.empty();
                    }

                    List<QuestaoDTO> lista = new ArrayList<>();
                    if (root.isArray()) {
                        for (JsonNode node : root) {
                            try {
                                QuestaoDTO q = objectMapper.treeToValue(node, QuestaoDTO.class);
                                if (!isQuestaoCompleta(q)) continue;

                                String chaveEnunciado = q.getEnunciado().substring(
                                        0, Math.min(60, q.getEnunciado().length()));
                                String chaveId = q.getId() != null && !q.getId().isBlank()
                                        ? q.getId() : null;

                                boolean idDuplicado = chaveId != null && !idsVistos.add(chaveId);
                                boolean enunciadoDuplicado = !enunciadosVistos.add(chaveEnunciado);

                                if (!idDuplicado && !enunciadoDuplicado) {
                                    lista.add(q);
                                } else {
                                    System.err.println("Duplicata ignorada: " + chaveEnunciado.substring(0, 30) + "...");
                                }

                            } catch (Exception e) {
                                System.err.println("Questão ignorada: " + e.getMessage());
                            }
                        }
                    }
                    return lista.stream();

                } catch (Exception e) {
                    System.err.println("Falha ao processar página: " + e.getMessage());
                    return Stream.empty();
                }
            })
            .collect(Collectors.toList());
    }

    private boolean isQuestaoCompleta(QuestaoDTO q) {
        if (q.getEnunciado() == null || q.getEnunciado().isBlank()
                || q.getEnunciado().length() < 20) return false;

        if (q.getAlternativas() == null) return false;

        long alternativasValidas = q.getAlternativas().entrySet().stream()
                .filter(e -> {
                    String chave = e.getKey();
                    String valor = e.getValue();
                    return chave != null
                            && chave.matches("[A-Ea-e]")
                            && valor != null
                            && !valor.isBlank()
                            && !valor.contains("__invalid__");
                })
                .count();

        return alternativasValidas >= 4;
    }

    private JsonNode tentarRecuperarJson(String jsonMalformado) {
        try {
            List<String> objetosValidos = new ArrayList<>();

            int profundidade = 0;
            int inicio = -1;

            for (int i = 0; i < jsonMalformado.length(); i++) {
                char c = jsonMalformado.charAt(i);
                if (c == '{') {
                    if (profundidade == 0) inicio = i;
                    profundidade++;
                } else if (c == '}') {
                    profundidade--;
                    if (profundidade == 0 && inicio != -1) {
                        String objeto = jsonMalformado.substring(inicio, i + 1);
                        try {
                            JsonNode node = objectMapper.readTree(objeto);
                            JsonNode enunciado = node.get("enunciado");
                            if (enunciado != null && !enunciado.asText().isBlank()
                                    && enunciado.asText().length() > 20) {
                                objetosValidos.add(objeto);
                            }
                        } catch (Exception ignored) {
                        }
                        inicio = -1;
                    }
                }
            }

            if (objetosValidos.isEmpty()) return null;

            String arrayRecuperado = "[" + String.join(",", objetosValidos) + "]";
            return objectMapper.readTree(arrayRecuperado);

        } catch (Exception e) {
            System.err.println("Recuperação parcial falhou: " + e.getMessage());
            return null;
        }
    }

    @Async
    public void enfileirarProcessamentoPdf(String jobId, File pdfFile, String disciplinaId, String promptPersonalizado, 
                                           String modoExtracao, UsuarioEntity usuario) { 
        try {
            ExtracaoJobEntity jobInicio = jobService.buscarPorId(jobId).orElseThrow();
            jobInicio.setStatus("PROCESSING");
            jobService.salvar(jobInicio);
            System.out.println("[ASYNC WORKER] Iniciando extração para o Job ID: " + jobId + " na disciplina: " + disciplinaId);

            cobrancaLlmService.verificarSaldoMinimo(usuario);

            AtomicBoolean saldoEsgotado = new AtomicBoolean(false);

            List<QuestaoDTO> questoesExtraidas = processarPdfParaQuestoes(pdfFile, promptPersonalizado, modoExtracao, usuario, saldoEsgotado);

            if (questoesExtraidas != null && !questoesExtraidas.isEmpty()) {
                questoesExtraidas = achatarListaDeQuestoes(questoesExtraidas);
                questoesExtraidas.forEach(questao -> {
                    questao.setDisciplinaId(disciplinaId);
                });
            }

            ExtracaoJobEntity jobConcluido = jobService.buscarPorId(jobId).orElseThrow();
            jobConcluido.setResultadoJson(objectMapper.writeValueAsString(questoesExtraidas));
            
            if (saldoEsgotado.get()) {
                jobConcluido.setStatus("PARCIALMENTE_CONCLUIDO");
                jobConcluido.setMensagemErro("O saldo esgotou durante o processamento. As questões geradas até o momento foram salvas com sucesso.");
            } else {
                jobConcluido.setStatus("COMPLETED");
            }
            
            jobService.salvar(jobConcluido);
            System.out.println("[ASYNC WORKER] Sucesso/Parcial no Job ID: " + jobId);

        } catch (Exception e) {
            System.err.println("[ASYNC WORKER] Falha no Job ID: " + jobId + " - " + e.getMessage());
            
            try {
                ExtracaoJobEntity jobErro = jobService.buscarPorId(jobId).orElseThrow();
                jobErro.setStatus("ERROR");
                jobErro.setMensagemErro(e.getMessage());
                jobService.salvar(jobErro);
            } catch (Exception ex) {
                System.err.println("[ASYNC WORKER] Falha crítica ao tentar salvar status de erro: " + ex.getMessage());
            }
        } 
    }

    private List<QuestaoDTO> achatarListaDeQuestoes(List<QuestaoDTO> questoesOriginais) {
        List<QuestaoDTO> listaAchatada = new ArrayList<>();
        
        for (QuestaoDTO original : questoesOriginais) {
            listaAchatada.add(original);
            
            if (original.getQuestaoInspirada() != null) {
                listaAchatada.add(original.getQuestaoInspirada());
                original.setQuestaoInspirada(null); 
            }
        }
        
        return listaAchatada;
    }
}