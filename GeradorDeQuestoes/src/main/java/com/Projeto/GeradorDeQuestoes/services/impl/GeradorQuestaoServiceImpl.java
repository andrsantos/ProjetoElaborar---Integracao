package com.Projeto.GeradorDeQuestoes.services.impl;

import com.Projeto.GeradorDeQuestoes.dto.AvaliacaoQuestao;
import com.Projeto.GeradorDeQuestoes.dto.DocumentoGeracaoDTO;
import com.Projeto.GeradorDeQuestoes.dto.GerarQuestaoRequest;
import com.Projeto.GeradorDeQuestoes.dto.ListaQuestoes;
import com.Projeto.GeradorDeQuestoes.dto.Questao;
import com.Projeto.GeradorDeQuestoes.entities.DocumentosReferenciaEntity;
import com.Projeto.GeradorDeQuestoes.entities.PdfQuestaoEntity;
import com.Projeto.GeradorDeQuestoes.entities.UsuarioEntity;
import com.Projeto.GeradorDeQuestoes.enums.NivelTecnico;
import com.Projeto.GeradorDeQuestoes.repositories.DocumentosReferenciaRepository;
import com.Projeto.GeradorDeQuestoes.repositories.PdfQuestaoRepository;
import com.Projeto.GeradorDeQuestoes.repositories.PromptRepository;
import com.Projeto.GeradorDeQuestoes.repositories.TopicoConfigRepository;
import com.Projeto.GeradorDeQuestoes.services.CobrancaLlmService;
import com.Projeto.GeradorDeQuestoes.services.GeradorQuestaoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class GeradorQuestaoServiceImpl implements GeradorQuestaoService {

    private final ChatClient openAiChatClient;
    private final ChatClient anthropicChatClient;
    private final VectorStore vectorStore;
    private final DocumentosReferenciaRepository documentosRepository;
    private final PdfQuestaoRepository pdfQuestaoRepository;
    private final CobrancaLlmService cobrancaLlmService;
    
    @Autowired
    private ObjectMapper objectMapper;

    public GeradorQuestaoServiceImpl(@Qualifier("openAiChatClient") ChatClient openAiChatClient,
                                     VectorStore vectorStore,
                                     TopicoConfigRepository configRepository,
                                     PromptRepository promptRepository,
                                     @Qualifier("anthropicChatClient") ChatClient anthropicChatClient, 
                                     DocumentosReferenciaRepository documentosRepository, 
                                     PdfQuestaoRepository pdfQuestaoRepository, CobrancaLlmService cobrancaLlmService) {
        this.openAiChatClient = openAiChatClient;
        this.anthropicChatClient = anthropicChatClient;
        this.vectorStore = vectorStore;
        this.documentosRepository = documentosRepository;
        this.pdfQuestaoRepository = pdfQuestaoRepository;
        this.cobrancaLlmService = cobrancaLlmService;
    }



    @Override
    public ListaQuestoes gerarQuestoes(GerarQuestaoRequest request, UsuarioEntity usuario) {
        
        List<Questao> todasAsQuestoes = new ArrayList<>();

        if (request.documentos() == null || request.documentos().isEmpty()) {
            System.err.println("Aviso: Requisição de geração de questões chegou vazia.");
            return new ListaQuestoes(todasAsQuestoes);
        }

        for (DocumentoGeracaoDTO bloco : request.documentos()) {
            System.out.println("Processando bloco de geração para o documento ID: " + bloco.getDocumentoId());

            boolean buscaGeral = bloco.getSubtopicos() == null || bloco.getSubtopicos().isEmpty();
            
            String diretriz = bloco.getDiretrizCustomizada(); 

            System.out.println("Diretriz Customizada: " + diretriz);

            if (buscaGeral) {
                if (bloco.getQuantidadeFaceis() > 0) {
                    todasAsQuestoes.addAll(gerarQuestoesParaConceito(bloco.getDocumentoId(), "", "FACIL", bloco.getQuantidadeFaceis(), diretriz,usuario));
                }
                if (bloco.getQuantidadeMedias() > 0) {
                    todasAsQuestoes.addAll(gerarQuestoesParaConceito(bloco.getDocumentoId(), "", "MEDIO", bloco.getQuantidadeMedias(), diretriz, usuario));
                }
                if (bloco.getQuantidadeDificeis() > 0) {
                    todasAsQuestoes.addAll(gerarQuestoesParaConceito(bloco.getDocumentoId(), "", "DIFICIL", bloco.getQuantidadeDificeis(), diretriz, usuario));
                }
            } else {
                for (var conceitoDto : bloco.getSubtopicos()) {
                    String nomeConceito = conceitoDto.getConceito();
                    
                    if (conceitoDto.getQuantidadeFaceis() > 0) {
                        todasAsQuestoes.addAll(gerarQuestoesParaConceito(bloco.getDocumentoId(), nomeConceito, "FACIL", conceitoDto.getQuantidadeFaceis(), diretriz,
                         usuario));
                    }
                    if (conceitoDto.getQuantidadeMedias() > 0) {
                        todasAsQuestoes.addAll(gerarQuestoesParaConceito(bloco.getDocumentoId(), nomeConceito, "MEDIO", conceitoDto.getQuantidadeMedias(), diretriz,
                    usuario));
                    }
                    if (conceitoDto.getQuantidadeDificeis() > 0) {
                        todasAsQuestoes.addAll(gerarQuestoesParaConceito(bloco.getDocumentoId(), nomeConceito, "DIFICIL", conceitoDto.getQuantidadeDificeis(), diretriz, 
                    usuario));
                    }
                }
            }
        }

        System.out.println("Geração concluída. Total de questões geradas: " + todasAsQuestoes.size());
        return new ListaQuestoes(todasAsQuestoes);
    }

    private List<Questao> gerarQuestoesParaConceito(String documentoId, String conceito, String nivel, int quantidadeSolicitada, 
        String diretrizCustomizada, UsuarioEntity usuario) {
        List<Questao> blocoFinal = new ArrayList<>();
        
        String tituloDocumento;
        boolean isOrigemProva = false;

        Optional<DocumentosReferenciaEntity> docOpt = documentosRepository.findById(documentoId);
        if (docOpt.isPresent()) {
            tituloDocumento = docOpt.get().getTitulo();
        } else {
            Optional<PdfQuestaoEntity> provaOpt = pdfQuestaoRepository.findById(UUID.fromString(documentoId));
            if (provaOpt.isPresent()) {
                tituloDocumento = provaOpt.get().getNomeOriginal();
                isOrigemProva = true;
            } else {
                throw new EntityNotFoundException("Fonte não encontrada para o ID: " + documentoId);
            }
        }

        String contextoDoConceito = recuperarContextoDoBanco(documentoId, conceito);
        if (contextoDoConceito.isBlank()) {
            return blocoFinal; 
        }

        String conceitoDeExibicao = (conceito == null || conceito.isBlank()) ? "Geral" : conceito;

        try {
            List<Questao> questoesGeradas = new ArrayList<>();

            if (isOrigemProva) {
                System.out.println("🧠 Acionando Agente Variador para conceito: " + conceitoDeExibicao);
                String questaoRaw = chamarAgenteVariador(tituloDocumento, nivel, contextoDoConceito, conceitoDeExibicao, usuario);
                questoesGeradas = parsearRespostaTags(questaoRaw);
            } else {
                System.out.println("✍️ Acionando Agente Elaborador Sênior em LOTE para criar " + quantidadeSolicitada + " questões de: " + conceitoDeExibicao);
                
                String elaboradaRaw = chamarAgenteElaboradorEmLote(tituloDocumento, nivel, 
                    contextoDoConceito, 
                    conceitoDeExibicao, 
                    quantidadeSolicitada, 
                    diretrizCustomizada,
                 usuario);
                questoesGeradas = parsearRespostaTags(elaboradaRaw);
            }

            for (Questao q : questoesGeradas) {
                if (blocoFinal.size() >= quantidadeSolicitada) break;

                try {
                    Questao questaoFinal = chamarAgenteJulgador(q, conceitoDeExibicao, usuario);
                    questaoFinal.setConceito(conceitoDeExibicao); 
                    
                    AvaliacaoQuestao avaliacao = chamarAgenteAvaliador(questaoFinal, usuario);
                    questaoFinal.setCompetencia(avaliacao.getCompetencia());
                    questaoFinal.setComentarioTecnico(avaliacao.getComentarioTecnico());
                    
                    questaoFinal.setTopico(tituloDocumento);
                    questaoFinal.setNivel(converterDeStringParaNivelTecnico(nivel));

                    blocoFinal.add(questaoFinal);

                } catch (Exception e) {
                    System.err.println("⚠️ Falha ao processar uma questão do lote [" + conceitoDeExibicao + "]. Pulando para a próxima. Erro: " + e.getMessage());
                }
            }

        } catch (Exception e) {
            System.err.println("Erro crítico na geração em lote [" + conceitoDeExibicao + "]: " + e.getMessage());
        }

        return blocoFinal;
    }



    private NivelTecnico converterDeStringParaNivelTecnico(String nivel){
        if(nivel.equals("FACIL")){
            return NivelTecnico.UNIVERSITARIO_INICIANTE;
        }
        if(nivel.equals("MEDIO")){
            return NivelTecnico.UNIVERSITARIO_INTERMEDIARIO;
        } else {
            return NivelTecnico.UNIVERSITARIO_AVANCADO;
        }
    }

   

    private String recuperarContextoDoBanco(String documentoId, String conceitoEspecifico) {
        FilterExpressionBuilder b = new FilterExpressionBuilder();

        SearchRequest sr;
        
        if (conceitoEspecifico == null || conceitoEspecifico.isBlank()) {
            sr = SearchRequest.builder()
                    .query("Conceitos principais e fundamentos gerais") 
                    .filterExpression(b.eq("documento_id", documentoId).build())
                    .topK(5)
                    .build();
            System.out.println("Busca Vetorial: ABRANGENTE para o documento ID [" + documentoId + "]");
        } else {
            sr = SearchRequest.builder()
                    .query(conceitoEspecifico) 
                    .filterExpression(b.and(
                        b.eq("documento_id", documentoId),
                        b.eq("conceito", conceitoEspecifico) 
                    ).build())
                    .topK(5) 
                    .build();
            System.out.println("Busca Vetorial para conceito [" + conceitoEspecifico + "] no documento ID [" + documentoId + "]");
        }

        List<Document> documentos = this.vectorStore.similaritySearch(sr);

        if (documentos.isEmpty()) {
            System.err.println("Aviso: Nenhum contexto encontrado no pgvector para o conceito: " + 
                (conceitoEspecifico.isBlank() ? "Geral" : conceitoEspecifico) + " no documento: " + documentoId);
            return "";
        }

        return documentos.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n---\n"));
    }
    
    @Override
    public List<String> extrairConceitosUnicos(String contexto, int qtd, UsuarioEntity usuario) {
        String prompt = "Liste exatamente %d conceitos técnicos distintos (ex: Protocolo, Atraso de Fila) baseados no material: %s. Separe os itens obrigatoriamente por VÍRGULA.".formatted(qtd, contexto);

        ChatResponse response = this.openAiChatClient.prompt(prompt)
                .options(ChatOptions.builder().temperature(0.7).build())
                .call()
                .chatResponse();
                
        Usage usage = response.getMetadata().getUsage();
        cobrancaLlmService.deduzirCusto(usuario, usage.getPromptTokens(), usage.getCompletionTokens(), "gpt-4o");
        
        String r = response.getResult().getOutput().getText();
        
        String[] partes = r.split(",|\\n|\\r|\\d+\\.");
        
        return Arrays.stream(partes)
                .map(String::trim)
                .filter(s -> s.length() > 3 && s.length() < 60) 
                .distinct()
                .collect(Collectors.toList());
    }

   private List<Questao> parsearRespostaTags(String rawText) {
    List<Questao> questoes = new ArrayList<>();

    Pattern patternBloco = Pattern.compile(
        "(?si)\\[ENUNCIADO\\](.*?)\\[/ENUNCIADO\\]" +
        "(.*?)" +
        "\\[RESPOSTA\\]\\s*(.*?)(?=\\[EXPLICACAO\\])" +
        "\\[EXPLICACAO\\]\\s*(.*?)\\[/EXPLICACAO\\]"
    );

    Matcher matcher = patternBloco.matcher(rawText);
    while (matcher.find()) {
        try {
            String enunciado  = matcher.group(1).trim();
            String blocoAlts  = matcher.group(2).trim();
            String respostaRaw = matcher.group(3).trim();
            String explicacao = matcher.group(4).trim();

            Map<String, String> alternativas = extrairAlternativas(blocoAlts);
            String resposta = mapResposta(respostaRaw);

            boolean valido = !enunciado.isEmpty()
                    && !resposta.isEmpty()
                    && alternativas.size() == 5
                    && !alternativas.containsValue("");

            if (valido) {
                questoes.add(new Questao(
                    UUID.randomUUID().toString(),
                    enunciado,
                    alternativas,
                    resposta,
                    explicacao
                ));
            } else {
                System.err.println("Questão descartada — campos incompletos. Alts: "
                    + alternativas.size() + " | Resposta: '" + resposta + "'");
            }

        } catch (Exception e) {
            System.err.println("Erro Parse: " + e.getMessage());
        }
    }
    return questoes;
    }

    private Map<String, String> extrairAlternativas(String bloco) {
        Map<String, String> alts = new LinkedHashMap<>();

        Pattern p = Pattern.compile(
            "(?m)^\\[([A-Ea-e])\\]\\s*(.*?)(?=^\\[[A-Ea-e]\\]|\\[RESPOSTA\\]|$)",
            Pattern.DOTALL
        );

        Matcher m = p.matcher(bloco);
        while (m.find()) {
            String letra = m.group(1).toLowerCase();
            String texto = m.group(2).trim();
            if (!texto.isEmpty()) {
                alts.put(letra, texto);
            }
        }
        return alts;
    }

    private String mapResposta(String raw) {
        if (raw == null || raw.isBlank()) return "";

        Matcher mExato = Pattern.compile("(?i)^\\s*([a-e])\\s*$").matcher(raw.trim());
        if (mExato.find()) return mExato.group(1).toLowerCase();

        Matcher mFallback = Pattern.compile("(?i)([a-e])").matcher(raw);
        if (mFallback.find()) return mFallback.group(1).toLowerCase();

        return "";
    }

    private Questao chamarAgenteJulgador(Questao questao, String conceito, UsuarioEntity usuario) { 

        String alternativasFormatadas = questao.getAlternativas().entrySet().stream()
                .map(e -> "[" + e.getKey().toUpperCase() + "] " + e.getValue())
                .collect(Collectors.joining("\n"));

        String prompt = """
            Você é um avaliador especialista em elaboração de questões de redes de computadores e telecomunicações,
            atuando no nível de concursos públicos rigorosos e provas universitárias da UFPA.

            Sua tarefa é julgar com extremo rigor a qualidade da questão abaixo e gerar uma versão perfeitamente corrigida.

            ### CONCEITO ALVO OBRIGATÓRIO ###
            A questão DEVE avaliar estritamente o conhecimento sobre: %1$s

            ### QUESTÃO ORIGINAL ###
            [ENUNCIADO]
            %2$s
            [/ENUNCIADO]

            %3$s

            [RESPOSTA]
            %4$s
            [/RESPOSTA]

            [EXPLICACAO]
            %5$s
            [/EXPLICACAO]

            ### CRITÉRIOS DE AVALIAÇÃO CRÍTICOS ###
            Avalie e aplique filtros severos sobre os seguintes pontos:
            1. Clareza do enunciado e ausência de ambiguidades.
            2. Existência de apenas UMA resposta correta.
            3. ADERÊNCIA ESTRITA AO CONCEITO: A questão avalia o conceito alvo (%1$s)? Se a questão sofreu "fuga de conceito" (ex: focou em cálculos genéricos de período ou taxas de bits sem cobrar as propriedades físicas/teóricas de %1$s), ela REPROVOU. Você deve reescrevê-la focando no tema correto.
            4. INTEGRIDADE DAS ALTERNATIVAS (ANTI-DUPLICAÇÃO): É expressamente PROIBIDO que duas alternativas tenham o mesmo valor, mesma resposta ou digam a mesma coisa em palavras diferentes. Todas as opções de [A] a [E] devem ser mutuamente exclusivas e únicas.
            5. PRECISÃO MATEMÁTICA: Resolva a lógica/cálculo da questão mentalmente passo a passo antes de validar. Garanta que a alternativa correta matemática esteja presente entre as opções.
            6. PRECISÃO DO ENUNCIADO: Verifique se o enunciado está estritamente apresentando uma pergunta. Não permita enunciados que mostrem alternativas A,B,C,D,E.
            
            ### REGRAS DE SAÍDA ###
            - Escreva um feedback detalhado, apontando pontos fortes e fracos da questão (especialmente se houve duplicidade ou fuga de conceito).
            - Dê uma nota de 0 a 10.
            - Monte a nova versão corrigida da questão aplicando todas as correções necessárias para satisfazer os critérios acima.
            - O formato de saída da nova questão deve usar rigorosamente as mesmas tags: [ENUNCIADO], [A], [B], [C], [D], [E], [RESPOSTA] e [EXPLICACAO].
            
            """.formatted(
                conceito,                                   
                questao.getEnunciado(),                     
                alternativasFormatadas,                     
                questao.getRespostaCorreta().toUpperCase(), 
                questao.getExplicacao()                     
        );


        ChatResponse response = this.openAiChatClient.prompt(prompt)
                .options(ChatOptions.builder().temperature(0.1).build())
                .call()
                .chatResponse();

        Usage usage = response.getMetadata().getUsage();
        cobrancaLlmService.deduzirCusto(usuario, usage.getPromptTokens(), usage.getCompletionTokens(), "gpt-4o");
        
        String resposta = response.getResult().getOutput().getText();

        List<Questao> questoesMelhoradas = parsearRespostaTags(resposta);
        if (!questoesMelhoradas.isEmpty()) {
            Questao questaoMelhorada = questoesMelhoradas.get(0);
            System.out.println("🤖 Julgador aplicou filtros de integridade e refinou a questão com sucesso.");
            questaoMelhorada.setFeedbackJulgador("Julgador melhorou a questão com sucesso.");
            return questaoMelhorada;
        }

        System.err.println("⚠️ Julgador não gerou questão parseável — mantendo questão original.");
        questao.setFeedbackJulgador(resposta);
        return questao;
    }

    private AvaliacaoQuestao chamarAgenteAvaliador(Questao questao, UsuarioEntity usuario){

      String prompt = """
        Você é um avaliador especialista em elaboração de questões de redes de computadores
        no nível de concursos públicos e provas universitárias.

        Sua tarefa é analisar a questão abaixo, e tecer um comentário técnico a respeito da questão. 

        ### VOCÊ DEVE IDENTIFICAR ### 
        - Competência que está sendo cobrada
        
        ### VOCÊ DEVE FAZER ### 
        - Uma análise técnica da questão, explicando qual a alternativa correta e justificando porque as outras estão erradas.

        ### QUESTÃO ###
        [ENUNCIADO]
        %s
        [/ENUNCIADO]

        %s

        [RESPOSTA]
        %s
        [/RESPOSTA]


        ### FORMATO DE SAÍDA ### 

        Responda APENAS no seguinte formato JSON:

        {
        "competencia": "Competência principal avaliada",
        "comentarioTecnico": "Comentário técnico detalhado da questão"
        }

        Não adicione texto fora do JSON.

        
        """.formatted(
            questao.getEnunciado(),
            questao.getAlternativas().entrySet().stream()
                .map(e -> "[" + e.getKey().toUpperCase() + "] " + e.getValue())
                .collect(Collectors.joining("\n")),
            questao.getRespostaCorreta().toUpperCase()
        );


        ChatResponse response = this.openAiChatClient.prompt(prompt)
                .options(ChatOptions.builder().temperature(0.1).build())
                .call()
                .chatResponse();

        Usage usage = response.getMetadata().getUsage();
        cobrancaLlmService.deduzirCusto(usuario, usage.getPromptTokens(), usage.getCompletionTokens(), "gpt-4o");
        
        String resposta = response.getResult().getOutput().getText();

        resposta = resposta
        .replace("```json", "")
        .replace("```", "")
        .trim();

            try {
                return objectMapper.readValue(resposta, AvaliacaoQuestao.class);
            } catch (Exception e) {
                throw new RuntimeException("Erro ao parsear avaliação da questão: " + resposta, e);
            }
    }


    private String chamarAgenteVariador(String topico, String nivel, String contexto, String conceito, UsuarioEntity usuario) {
        
        String templateBase = """
            Você é um especialista em engenharia reversa de bancas examinadoras de concursos e provas universitárias.
            O contexto abaixo contém QUESTÕES REAIS aplicadas em provas anteriores.
            Seu objetivo NÃO é resumir o texto, mas sim criar UMA questão INÉDITA sobre o conceito '{conceito}', IMITANDO RIGOROSAMENTE o estilo da banca.
            
            ### REGRAS DE CLONAGEM DE ESTILO E DIFICULDADE ###
            1. Copie o formato, o vocabulário e o tom do enunciado original.
            2. Imite o padrão das alternativas (ex: se usam sentenças longas, múltipla escolha pura, ou "Apenas I e II estão corretas", faça igual).
            3. NUNCA copie a questão do contexto de forma idêntica. Use-as apenas como o seu "molde arquitetônico".
            4. CALIBRAÇÃO DE DIFICULDADE: A questão gerada DEVE ser estritamente do nível de dificuldade: {nivel}.
            5. Se o nível for FÁCIL, crie alternativas incorretas mais óbvias e diretas. Se o nível for MÉDIO, equilibre a dificuldade imitando exatamente o padrão médio da banca. Se o nível for DIFÍCIL, crie distratores (pegadinhas) altamente complexos e sutis, exigindo atenção aos detalhes e leitura minuciosa.

            ### PARÂMETROS DO SISTEMA ###
            - Conceito central: {conceito}
            - Nível de dificuldade: {nivel}
            - Tópico principal: {topico}

            ### CONTEXTO DE MOLDE (Questões de Referência da Banca) ###
            {contexto}

            ### FORMATO DE SAÍDA OBRIGATÓRIO (Não adicione nenhum texto extra) ###
            [ENUNCIADO]
            <texto do enunciado objetivo e direto>
            [/ENUNCIADO]
            [A] <alternativa A>
            [B] <alternativa B>
            [C] <alternativa C>
            [D] <alternativa D>
            [E] <alternativa E>
            [RESPOSTA] <apenas a letra: a, b, c, d ou e>
            [EXPLICACAO]
            <explicação detalhada>
            [/EXPLICACAO]
            """;

        PromptTemplate template = new PromptTemplate(templateBase);
        
        Map<String, Object> params = Map.of(
            "nivel", nivel, 
            "topico", topico, 
            "contexto", contexto, 
            "conceito", conceito
        );


        ChatResponse response = this.openAiChatClient.prompt(template.render(params))
                .options(ChatOptions.builder().temperature(0.7).build()) 
                .call()
                .chatResponse();

        Usage usage = response.getMetadata().getUsage();
        cobrancaLlmService.deduzirCusto(usuario, usage.getPromptTokens(), usage.getCompletionTokens(), "gpt-4o");
        
        return response.getResult().getOutput().getText();
    }


    private String chamarAgenteElaboradorEmLote(String topico, String nivel, String contexto, String conceito, int quantidade, 
        String diretrizCustomizada, UsuarioEntity usuario) {
        
        String diretrizSegura = (diretrizCustomizada != null && !diretrizCustomizada.isBlank()) 
                                ? "\n### DIRETRIZES PERSONALIZADAS DO PROFESSOR (PRIORIDADE MÁXIMA) ###\n" + diretrizCustomizada + "\n" 
                                : "";

        String templateBase = """
            Você é um Professor Universitário Sênior e membro de uma banca examinadora de concursos públicos de alto rigor.
            Sua missão é criar exatamente {quantidade} questões de múltipla escolha INÉDITAS baseadas na teoria fornecida.
            {diretrizSegura}
            ### DIRETRIZ OBRIGATÓRIA DE VARIABILIDADE ###
            Cada uma das {quantidade} questões deve testar um aspecto DIFERENTE e COMPLEMENTAR do conceito '{conceito}'.
            - Se uma questão focar em deslocamento temporal/fase , as outras DEVEM focar em sub-temas diferentes, como: cálculo matemático de período/frequência ($T=1/f$) , interpretação de gráficos nos domínios do tempo vs frequência, ou impactos físicos da variação de amplitude.
            - PROIBIDO criar enunciados repetitivos ou cujas respostas corretas foquem no mesmo parâmetro técnico.

            ### PARÂMETROS GERAIS ###
            - Nível de dificuldade: {nivel}
            - Disciplina/Tópico: {topico}

            ### BASE DE CONHECIMENTO ###
            {contexto}

            ### REGRAS DE OURO (ANTI-PADRÕES) ###
            1. PROIBIDO DECOREBA: Nunca pergunte definições diretas textuais ou o significado de siglas.
            2. PROIBIDO PALAVRAS ABSOLUTAS: É estritamente proibido utilizar palavras absolutistas ou restritivas nos distratores, tais como: "sempre", "nunca", "exclusivamente", "apenas", "somente", "automaticamente", "totalmente", "obrigatoriamente", "qualquer" ou "garante".
            3. DISTRATORES TÉCNICOS E CONCEITUAIS: As alternativas erradas não podem ser simplesmente a negação de um fato. Elas devem descrever um comportamento de rede plausível, mas que pertença a OUTRO protocolo, OUTRA camada ou OUTRO conceito técnico associado.
            4. O ENUNCIADO TRAZ O PROBLEMA: O enunciado descreve o cenário prático ou gargalo; a alternativa correta traz o diagnóstico ou a solução arquitetural.
            5. INTEGRIDADE DAS ALTERNATIVAS: Todas as alternativas ([A] até [E]) DEVEM ser frases autossuficientes, com sentido completo e obrigatoriamente terminadas com ponto final (.). É estritamente proibido gerar textos cortados, parágrafos pela metade ou sentenças que terminem abruptamente sem concluir a ideia.

            ### FORMATO DE SAÍDA OBRIGATÓRIO (Repita este bloco para cada questão gerada, sem textos separadores fora das tags) ###
            [ENUNCIADO]
            <narrativa prática + comando da questão>
            [/ENUNCIADO]
            [A] <alternativa>
            [B] <alternativa>
            [C] <alternativa>
            [D] <alternativa>
            [E] <alternativa>
            [RESPOSTA] <letra minúscula: a, b, c, d ou e>
            [EXPLICACAO]
            <Justificativa detalhada>
            [/EXPLICACAO]
            """;

        PromptTemplate template = new PromptTemplate(templateBase);
        
        Map<String, Object> params = Map.of(
            "nivel", nivel, 
            "topico", topico, 
            "contexto", contexto, 
            "conceito", conceito,
            "quantidade", quantidade,
            "diretrizSegura", diretrizSegura
        );


        ChatResponse response = this.openAiChatClient.prompt(template.render(params))
                .options(ChatOptions.builder().temperature(0.7).build()) 
                .call()
                .chatResponse();

        Usage usage = response.getMetadata().getUsage();
        cobrancaLlmService.deduzirCusto(usuario, usage.getPromptTokens(), usage.getCompletionTokens(), "gpt-4o");
        
        return response.getResult().getOutput().getText();
    }




    private String chamarAgenteSubstituto(String conceito, String enunciadoAntigo, UsuarioEntity usuario) {
        String templateBase = """
            Você é um Professor Universitário Sênior e membro de uma banca examinadora rigorosa.
            O usuário deseja substituir a seguinte questão sobre o conceito '{conceito}':
            
            "{enunciadoAntigo}"

            Sua missão é criar UMA NOVA questão de múltipla escolha inédita que aborde o mesmo conceito ('{conceito}'), mas com uma narrativa, cenário prático ou abordagem totalmente diferente da questão fornecida.

            ### REGRAS OBRIGATÓRIAS ###
            1. Siga rigorosamente o padrão de concursos públicos de alto nível.
            2. A alternativa correta não pode usar palavras absolutistas (como "sempre", "nunca", "apenas").
            3. As alternativas devem ter completude textual (terminar com ponto final).
            4. Garanta exatamente 5 alternativas de [A] a [E].

            ### FORMATO DE SAÍDA OBRIGATÓRIO ###
            [ENUNCIADO]
            <narrativa prática + comando da questão>
            [/ENUNCIADO]
            [A] <alternativa>
            [B] <alternativa>
            [C] <alternativa>
            [D] <alternativa>
            [E] <alternativa>
            [RESPOSTA] <letra minúscula: a, b, c, d ou e>
            [EXPLICACAO]
            <Justificativa detalhada>
            [/EXPLICACAO]
            """;

        PromptTemplate template = new PromptTemplate(templateBase);
        Map<String, Object> params = Map.of(
            "conceito", conceito,
            "enunciadoAntigo", enunciadoAntigo
        );


        ChatResponse response = this.openAiChatClient.prompt(template.render(params))
                .options(ChatOptions.builder().temperature(0.8).build()) 
                .call()
                .chatResponse();

        Usage usage = response.getMetadata().getUsage();
        cobrancaLlmService.deduzirCusto(usuario, usage.getPromptTokens(), usage.getCompletionTokens(), "gpt-4o");
        
        return response.getResult().getOutput().getText();
    }




    @Override
    public Questao gerarQuestaoSubstitutaAvulsa(String conceito, String enunciadoAntigo, String nivel, UsuarioEntity usuario) {
        System.out.println("🔄 Iniciando geração de substituta direta via IA para o conceito: " + conceito);
        
        String raw = chamarAgenteSubstituto(conceito, enunciadoAntigo, usuario);
        
        List<Questao> questoesParseadas = parsearRespostaTags(raw);
        if (questoesParseadas.isEmpty()) {
            System.out.println("⚠️ Regex primário falhou. Delegando limpeza para o Agente Parseador (Claude)...");
            questoesParseadas = parsearComAgenteClaude(raw, usuario);
        }

        if (questoesParseadas.isEmpty()) {
            throw new RuntimeException("A IA gerou a questão, mas nem o Agente Parseador conseguiu normalizar a estrutura.");
        }
        
        Questao questaoBruta = questoesParseadas.get(0);

        Questao questaoFinal = chamarAgenteJulgador(questaoBruta, conceito, usuario);
        questaoFinal.setConceito(conceito);
        
        AvaliacaoQuestao avaliacao = chamarAgenteAvaliador(questaoFinal, usuario);
        questaoFinal.setCompetencia(avaliacao.getCompetencia());
        questaoFinal.setComentarioTecnico(avaliacao.getComentarioTecnico());
        questaoFinal.setNivel(converterDeStringParaNivelTecnico(nivel));
        
        System.out.println("✅ Questão substituta gerada diretamente, avaliada e julgada com sucesso!");
        return questaoFinal;
    }

    private List<Questao> parsearComAgenteClaude(String rawText, UsuarioEntity usuario) {

        System.out.println("🤖 Acionando Agente Parseador (Claude) para corrigir formatação...");

        String prompt = """
            Você é um agente de parseamento e formatação de dados textuais de alta precisão.
            O texto abaixo foi gerado por um modelo de IA, mas pode conter pequenas variações de formatação, quebras incorretas ou ausência de tags padronizadas.

            Sua ÚNICA missão é ler o texto bruto fornecido abaixo e reescrevê-lo convertendo-o RIGOROSAMENTE para o formato de tags padrão abaixo, sem alterar o conteúdo pedagógico, sem inventar novas informações e sem adicionar nenhum texto de introdução ou saudação.

            ### FORMATO DE SAÍDA EXIGIDO ###
            [ENUNCIADO]
            <texto do enunciado>
            [/ENUNCIADO]
            [A] <alternativa A>
            [B] <alternativa B>
            [C] <alternativa C>
            [D] <alternativa D>
            [E] <alternativa E>
            [RESPOSTA] <letra minúscula: a, b, c, d ou e>
            [EXPLICACAO]
            <explicação detalhada>
            [/EXPLICACAO]

            ### TEXTO BRUTO PARA NORMALIZAR ###
            %s
            """.formatted(rawText);


        ChatResponse response = this.anthropicChatClient.prompt(prompt)
                .options(ChatOptions.builder().temperature(0.0).build()) 
                .call()
                .chatResponse();

        Usage usage = response.getMetadata().getUsage();
        cobrancaLlmService.deduzirCusto(usuario, usage.getPromptTokens(), usage.getCompletionTokens(), "claude-haiku"); 
        
        String respostaNormalizada = response.getResult().getOutput().getText();

        return parsearRespostaTags(respostaNormalizada);
    }




}