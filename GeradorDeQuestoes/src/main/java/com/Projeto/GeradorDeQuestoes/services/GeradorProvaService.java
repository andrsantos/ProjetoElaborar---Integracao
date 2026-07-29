package com.Projeto.GeradorDeQuestoes.services;

import com.Projeto.GeradorDeQuestoes.dto.GeracaoAutomaticaRequest;
import com.Projeto.GeradorDeQuestoes.dto.GeracaoExpressaRequest;
import com.Projeto.GeradorDeQuestoes.dto.GerarQuestaoRequest;
import com.Projeto.GeradorDeQuestoes.dto.ListaQuestoes;
import com.Projeto.GeradorDeQuestoes.dto.Prova;
import com.Projeto.GeradorDeQuestoes.dto.Questao;
import com.Projeto.GeradorDeQuestoes.entities.BancoQuestaoEntity;
import com.Projeto.GeradorDeQuestoes.entities.ProvaEntity;
import com.Projeto.GeradorDeQuestoes.entities.QuestaoProvaEntity;
import com.Projeto.GeradorDeQuestoes.entities.UsuarioEntity;
import com.Projeto.GeradorDeQuestoes.repositories.BancoQuestaoRepository;
import com.Projeto.GeradorDeQuestoes.repositories.ProvaRepository;
import com.Projeto.GeradorDeQuestoes.repositories.TopicoConfigRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import com.fasterxml.jackson.core.type.TypeReference;

@Service
public class GeradorProvaService {

    private static final Map<UUID, Prova> provasEmMemoria = new ConcurrentHashMap<>();
    private final GeradorQuestaoService questaoService;
    private final ProvaRepository provaRepository;
    private final PdfService pdfService;
    private final BancoQuestaoRepository bancoQuestaoRepository;
    private final ObjectMapper objectMapper;
    private final ChatClient anthropicChatClient;

    public GeradorProvaService(GeradorQuestaoService questaoService, 
    ProvaRepository provaRepository, 
    PdfService pdfService,
    TopicoConfigRepository configRepository, 
    BancoQuestaoRepository bancoQuestaoRepository, 
    @Qualifier("anthropicChatClient") ChatClient anthropicChatClient, 
    ObjectMapper objectMapper
    ) {
        this.questaoService = questaoService;
        this.provaRepository = provaRepository;
        this.pdfService = pdfService;
        this.bancoQuestaoRepository = bancoQuestaoRepository;
        this.objectMapper = objectMapper;
        this.anthropicChatClient = anthropicChatClient;
    }

  
    public Prova criarNovaProva(String disciplinaId) {
        Prova novaProva = new Prova();
        novaProva.setDisciplinaId(disciplinaId);
        provasEmMemoria.put(novaProva.getId(), novaProva);
        return novaProva;
    }


    public Prova getProva(UUID id) {
        return provasEmMemoria.get(id);
    }

  
    public Prova adicionarQuestoes(UUID idProva, GerarQuestaoRequest request, UsuarioEntity usuario) {
        Prova prova = getProva(idProva);
        if (prova == null) {
            throw new RuntimeException("Prova não encontrada!"); 
        }

        ListaQuestoes novasQuestoes = questaoService.gerarQuestoes(request, usuario);

        novasQuestoes.questoes().forEach(prova::adicionarQuestao);

        return prova;
    }

    public Prova adicionarQuestoesDoBanco(UUID idProva, List<Questao> questoesBanco) {
        Prova prova = getProva(idProva);
        if (prova == null) {
            throw new RuntimeException("Prova não encontrada!"); 
        }

        questoesBanco.forEach(prova::adicionarQuestao);

        return prova;
    }


  
    public Prova descartarQuestao(UUID idProva, int indiceQuestao) {
        Prova prova = getProva(idProva);
        if (prova == null) {
            throw new RuntimeException("Prova não encontrada!");
        }
        prova.removerQuestao(indiceQuestao);
        return prova;
    }

    public byte[] finalizarEGerarPdf(UUID idProva) throws IOException {
        Prova provaEmMemoria = getProva(idProva);
        if (provaEmMemoria == null) {
            throw new RuntimeException("Prova não encontrada!");
        }

        ProvaEntity provaEntity = new ProvaEntity();
        provaEntity.setId(provaEmMemoria.getId());
        provaEntity.setDataCriacao(OffsetDateTime.now());
        provaEntity.setTitulo("Prova de Redes - " + provaEmMemoria.getId().toString().substring(0, 8));

        for (Questao questaoDto : provaEmMemoria.getQuestoes()) {
            QuestaoProvaEntity questaoEntity = new QuestaoProvaEntity();
            questaoEntity.setEnunciado(questaoDto.getEnunciado());
            questaoEntity.setAlternativas(questaoDto.getAlternativas());
            questaoEntity.setRespostaCorreta(questaoDto.getRespostaCorreta());
            
            provaEntity.addQuestao(questaoEntity); 
        }

        provaRepository.save(provaEntity);

        byte[] pdfBytes = pdfService.gerarPdfProva(provaEmMemoria);
        System.out.println("SERVICE: PDF gerado para prova " + idProva);

        provasEmMemoria.remove(idProva);
        System.out.println("SERVICE: Prova " + idProva + " removida da memória.");
        
        return pdfBytes;
    }

   public Prova adicionarQuestoesAutomatico(UUID idProva, GeracaoAutomaticaRequest request, UsuarioEntity usuario) {
        
        Prova prova = getProva(idProva);
        if (prova == null) {
            throw new RuntimeException("Prova não encontrada!");
        }

        GerarQuestaoRequest ragRequest = new GerarQuestaoRequest(request.getDocumentos());
        
        ListaQuestoes novasQuestoes = questaoService.gerarQuestoes(ragRequest, usuario);

        novasQuestoes.questoes().forEach(questao -> {

            String letraCorretaOriginal = questao.getRespostaCorreta().toLowerCase();
            String textoCorreto = questao.getAlternativas().get(letraCorretaOriginal);

            List<String> textos = new ArrayList<>(questao.getAlternativas().values());
            Collections.shuffle(textos);

            Map<String, String> novasAlternativas = new LinkedHashMap<>();
            String[] letras = {"a", "b", "c", "d", "e"};
            String novaLetraCorreta = "";

            for (int i = 0; i < textos.size(); i++) {
                String letraAtual = letras[i];
                String textoAtual = textos.get(i);
                
                novasAlternativas.put(letraAtual, textoAtual);

                if (textoAtual.equals(textoCorreto)) {
                    novaLetraCorreta = letraAtual;
                }
            }

            questao.setAlternativas(novasAlternativas);
            questao.setRespostaCorreta(novaLetraCorreta);

            System.out.println("--- Questão Randomizada ---");
            System.out.println("Nova correta: " + questao.getRespostaCorreta());
            
            prova.adicionarQuestao(questao);
        });
        
        System.out.println("SERVICE: Adicionadas " + novasQuestoes.questoes().size() + " questões à prova " + idProva);
        
        return prova;
    }

 
    public Prova adicionarQuestoesManuais(UUID idProva, List<Questao> questoes) {
        Prova prova = getProva(idProva);
        if (prova == null) {
            throw new RuntimeException("Prova não encontrada!");
        }
        
        questoes.forEach(prova::adicionarQuestao);
        
        System.out.println("SERVICE: Adicionadas " + questoes.size() + " questões manuais à prova " + idProva);
        return prova;
    }






    public Prova gerarProvaExpressa(GeracaoExpressaRequest request) {

        Prova novaProva = criarNovaProva(request.getDisciplinaId());

        List<BancoQuestaoEntity> candidatas = bancoQuestaoRepository.findByDisciplinaIdAndConceitoIn(
            request.getDisciplinaId(), request.getTopicos()
        );

        if (candidatas == null || candidatas.isEmpty()) {
            throw new IllegalArgumentException("Nenhuma questão encontrada para os tópicos selecionados.");
        }

        String nivelDesejado = request.getNivel();
        
        if ("UNIVERSITARIO_AVANCADO".equalsIgnoreCase(nivelDesejado)) {
            candidatas.sort(Comparator.comparingInt(this::getPesoDificuldade).reversed());
            
        } else if ("UNIVERSITARIO_INICIANTE".equalsIgnoreCase(nivelDesejado)) {
            candidatas.sort(Comparator.comparingInt(this::getPesoDificuldade));
            
        } else if ("UNIVERSITARIO_INTERMEDIARIO".equalsIgnoreCase(nivelDesejado)) {
            candidatas.sort(Comparator.comparingInt(q -> Math.abs(getPesoDificuldade(q) - 2)));
            
        } else {
            Collections.shuffle(candidatas);
        }

        List<BancoQuestaoEntity> selecionadas;

        if (request.getDiretriz() != null && !request.getDiretriz().trim().isEmpty()) {
            selecionadas = selecionarCandidatasComIA(candidatas, request.getQuantidade(), request.getDiretriz());
        } else {
            int limite = Math.min(request.getQuantidade(), candidatas.size());
            selecionadas = candidatas.subList(0, limite);
        }

        for (BancoQuestaoEntity entity : selecionadas) {
            novaProva.adicionarQuestao(converterBancoParaQuestaoDTO(entity));
        }

        return novaProva;
    }


    private Questao converterBancoParaQuestaoDTO(BancoQuestaoEntity entity) {
            return new Questao(
                entity.getId() != null ? entity.getId().toString() : null,
                entity.getEnunciado(),
                entity.getAlternativas(), 
                entity.getRespostaCorreta(),
                entity.getConceito(),
                entity.getCompetencia(),
                entity.getComentarioTecnico(),
                entity.getTopico(),
                entity.getNivel()
            );
    }


    private int getPesoDificuldade(BancoQuestaoEntity q) {
        String nivel = q.getNivel().toString();
        if (nivel == null) return 2; 
        
        return switch (nivel.toUpperCase()) {
            case "UNIVERSITARIO_AVANCADO" -> 3;
            case "UNIVERSITARIO_INTERMEDIARIO" -> 2;
            case "UNIVERSITARIO_INICIANTE" -> 1;
            default -> 2;

        };
    }

    private List<BancoQuestaoEntity> selecionarCandidatasComIA(List<BancoQuestaoEntity> candidatas, int quantidadeDesejada, String diretriz) {
        if (candidatas.size() <= quantidadeDesejada) {
            return candidatas;
        }

        System.out.println("🤖 Acionando IA para filtro semântico. Diretriz: " + diretriz);

        try {
            List<Map<String, String>> dadosParaIA = candidatas.stream()
                .map(q -> Map.of(
                    "id", q.getId().toString(),
                    "enunciado", q.getEnunciado()
                ))
                .toList();

            String jsonLote = objectMapper.writeValueAsString(dadosParaIA);

            String instrucao = """
                Você é um Agente Pedagógico Sênior especializado em engenharia de avaliações.
                Sua tarefa é analisar uma lista de questões candidatas e selecionar EXATAMENTE %d questões que melhor atendam à seguinte diretriz do usuário:
                
                DIRETRIZ DO USUÁRIO: "%s"
                
                REGRAS MANDATÓRIAS:
                1. Analise o contexto de cada enunciado para ver se ele cumpre a diretriz.
                2. Retorne APENAS um array JSON plano contendo os IDs das questões escolhidas.
                3. Não inclua NENHUM texto explicativo, saudações ou formatação markdown (como ```json).
                4. Você deve retornar exatamente %d IDs.
                
                EXEMPLO DO FORMATO DE SAÍDA:
                ["123e4567-e89b-12d3-a456-426614174000", "987e6543-e21b-12d3-a456-426614174111"]
                """.formatted(quantidadeDesejada, diretriz, quantidadeDesejada);

            String respostaIA = this.anthropicChatClient.prompt(instrucao + "\n\nLISTA DE QUESTÕES DISPONÍVEIS:\n" + jsonLote)
                    .call().content();

            String cleanJson = respostaIA.replaceAll("(?s)```json\\s*|```", "").trim();
            List<String> idsEscolhidos = objectMapper.readValue(cleanJson, new TypeReference<List<String>>() {});

            List<BancoQuestaoEntity> selecionadas = candidatas.stream()
                .filter(q -> idsEscolhidos.contains(q.getId().toString()))
                .limit(quantidadeDesejada)
                .collect(Collectors.toList());

            if (selecionadas.size() < quantidadeDesejada) {
                System.out.println("⚠️ IA retornou apenas " + selecionadas.size() + " IDs válidos. Completando prova com ordenação de melhor esforço...");
                completarComFallback(candidatas, selecionadas, quantidadeDesejada);
            }

            System.out.println("✅ Filtro semântico concluído com sucesso.");
            return selecionadas;

        } catch (Exception e) {
            System.err.println("❌ Erro crítico na IA (Filtro Semântico). Utilizando fallback de ordenação. Erro: " + e.getMessage());
            return candidatas.subList(0, quantidadeDesejada);
        }
    }

    private void completarComFallback(List<BancoQuestaoEntity> candidatas, List<BancoQuestaoEntity> selecionadas, int quantidadeDesejada) {
        for (BancoQuestaoEntity q : candidatas) {
            if (selecionadas.size() >= quantidadeDesejada) {
                break;
            }
            if (!selecionadas.contains(q)) {
                selecionadas.add(q);
            }
        }
    }

}