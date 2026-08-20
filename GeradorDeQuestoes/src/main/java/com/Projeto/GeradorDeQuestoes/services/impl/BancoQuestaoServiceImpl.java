package com.Projeto.GeradorDeQuestoes.services.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.Projeto.GeradorDeQuestoes.dto.ClassificacaoDTO;
import com.Projeto.GeradorDeQuestoes.dto.ClassificacaoLoteDTO;
import com.Projeto.GeradorDeQuestoes.dto.ConceitoConfigDTO;
import com.Projeto.GeradorDeQuestoes.dto.GeracaoAutomaticaRequest;
import com.Projeto.GeradorDeQuestoes.dto.QuestaoDTO;
import com.Projeto.GeradorDeQuestoes.entities.BancoQuestaoEntity;
import com.Projeto.GeradorDeQuestoes.entities.ExtracaoJobEntity;
import com.Projeto.GeradorDeQuestoes.entities.UsuarioEntity;
import com.Projeto.GeradorDeQuestoes.enums.NivelTecnico;
import com.Projeto.GeradorDeQuestoes.repositories.BancoQuestaoRepository;
import com.Projeto.GeradorDeQuestoes.services.BancoQuestaoService;
import com.Projeto.GeradorDeQuestoes.services.CobrancaLlmService;
import com.Projeto.GeradorDeQuestoes.services.JobService;
import com.Projeto.GeradorDeQuestoes.services.SseNotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import jakarta.transaction.Transactional;


@Service
public class BancoQuestaoServiceImpl implements BancoQuestaoService {

    private BancoQuestaoRepository bancoQuestaoRepository;
    private final ChatClient chatClient;        
    private final ChatClient anthropicChatClient;
    private final CobrancaLlmService cobrancaLlmService;
    private final JobService jobService;
    private final SseNotificationService sseNotificationService;

    BancoQuestaoServiceImpl(BancoQuestaoRepository bancoQuestaoRepository, 
        @Qualifier("openAiChatClient") ChatClient chatClient, 
        @Qualifier("anthropicChatClient") ChatClient anthropicChatClient, 
        CobrancaLlmService cobrancaLlmService, 
        JobService jobService,
        SseNotificationService sseNotificationService, SseNotificationService sseNotificationService2) {
        this.chatClient = chatClient;
        this.bancoQuestaoRepository = bancoQuestaoRepository;
        this.anthropicChatClient = anthropicChatClient;
        this.cobrancaLlmService = cobrancaLlmService;
        this.jobService = jobService;
        this.sseNotificationService = sseNotificationService2;
    }

    @Override
    public List<QuestaoDTO> listarQuestoes() {
        return bancoQuestaoRepository.findAll().stream()
                .map(entity -> new QuestaoDTO(
                       entity.getId().toString(),
                        entity.getEnunciado(),
                        entity.getAlternativas(),
                        entity.getRespostaCorreta(),
                        entity.getConceito(),
                        entity.getCompetencia(),
                        entity.getComentarioTecnico(),
                        entity.getNivel()
                ))
                .toList();
    }


    

    @Override
    public List<QuestaoDTO> listarQuestoesPorNivel(String nivel) {
        return bancoQuestaoRepository.findByNivel(nivel).stream()
                     .map(entity -> new QuestaoDTO(
                        entity.getId().toString(),
                        entity.getEnunciado(),
                        entity.getAlternativas(),
                        entity.getRespostaCorreta(),
                        entity.getConceito(),
                        entity.getCompetencia(),
                        entity.getComentarioTecnico(),
                        entity.getNivel()
                ))
                .toList(); 
    }

    public List<QuestaoDTO> listaQuestoesPorConceito(String conceito){

            return bancoQuestaoRepository.findByConceito(conceito).stream()
                    .map(entity -> new QuestaoDTO(
                        entity.getId().toString(),
                        entity.getEnunciado(),
                        entity.getAlternativas(),
                        entity.getRespostaCorreta(),
                        entity.getConceito(),
                        entity.getCompetencia(),
                        entity.getComentarioTecnico(),
                        entity.getNivel()
            ))
            .toList(); 
    }



    @Override
    public List<QuestaoDTO> gerarQuestoesParaProva(GeracaoAutomaticaRequest request) {

        List<QuestaoDTO> questoesGeradas = new ArrayList<>();

        for (int i = 0; i < request.getDocumentos().size(); i++) {
            var blocoRequest = request.getDocumentos().get(i);
            

            for (int j = 0; j < blocoRequest.getSubtopicos().size(); j++) {
                ConceitoConfigDTO conceitoConfig = blocoRequest.getSubtopicos().get(j);
                String nomeConceito = conceitoConfig.getConceito();

                int quantidadeFaceis = conceitoConfig.getQuantidadeFaceis();
                int quantidadeMedias = conceitoConfig.getQuantidadeMedias();
                int quantidadeDificeis = conceitoConfig.getQuantidadeDificeis();
                int quantidadeTotalConceito = quantidadeFaceis + quantidadeMedias + quantidadeDificeis;

                if (quantidadeTotalConceito == 0) continue;


                List<QuestaoDTO> questoesDoConceitoNoBanco = listaQuestoesPorConceito(nomeConceito);

                if (questoesDoConceitoNoBanco.isEmpty()) {
                    throw new IllegalArgumentException("Não existem questões para o conceito: '" + nomeConceito + "' cadastradas no banco.");
                }


                if (quantidadeFaceis > 0) {
                    List<QuestaoDTO> faceisDisponiveis = questoesDoConceitoNoBanco.stream()
                            .filter(q -> q.getNivel().equals(NivelTecnico.UNIVERSITARIO_INICIANTE))
                            .collect(Collectors.toList());

                    if (faceisDisponiveis.size() < quantidadeFaceis) {
                        throw new IllegalArgumentException("Não existem questões de nível FÁCIL suficientes para o conceito '" + nomeConceito + "'. Cadastre mais questões correspondentes.");
                    }
                    Collections.shuffle(faceisDisponiveis);
                    questoesGeradas.addAll(faceisDisponiveis.stream().limit(quantidadeFaceis).toList());
                }


                if (quantidadeMedias > 0) {
                    List<QuestaoDTO> mediasDisponiveis = questoesDoConceitoNoBanco.stream()
                            .filter(q -> q.getNivel().equals(NivelTecnico.UNIVERSITARIO_INTERMEDIARIO))
                            .collect(Collectors.toList());

                    if (mediasDisponiveis.size() < quantidadeMedias) {
                        throw new IllegalArgumentException("Não existem questões de nível MÉDIO suficientes para o conceito '" + nomeConceito + "'. Cadastre mais questões correspondentes.");
                    }
                    Collections.shuffle(mediasDisponiveis);
                    questoesGeradas.addAll(mediasDisponiveis.stream().limit(quantidadeMedias).toList());
                }

                if (quantidadeDificeis > 0) {
                    List<QuestaoDTO> dificeisDisponiveis = questoesDoConceitoNoBanco.stream()
                            .filter(q -> q.getNivel().equals(NivelTecnico.UNIVERSITARIO_AVANCADO))
                            .collect(Collectors.toList());

                    if (dificeisDisponiveis.size() < quantidadeDificeis) {
                        throw new IllegalArgumentException("Não existem questões de nível DIFÍCIL suficientes para o conceito '" + nomeConceito + "'. Cadastre mais questões correspondentes.");
                    }
                    Collections.shuffle(dificeisDisponiveis);
                    questoesGeradas.addAll(dificeisDisponiveis.stream().limit(quantidadeDificeis).toList());
                }
            }
        }

        return questoesGeradas;
    }

   @Override
    public String normalizarConceito(String enunciado, String conceitoSugerido, List<String> conceitosExistentes, 
        UsuarioEntity usuario) {
        
        if (conceitosExistentes == null || conceitosExistentes.isEmpty()) {
            return conceitoSugerido != null ? conceitoSugerido : "Geral";
        }

        String listaConceitosFormatada = String.join("\n- ", conceitosExistentes);

        var outputConverter = new BeanOutputConverter<>(ClassificacaoDTO.class);
        String formatInstructions = outputConverter.getFormat();

        String templateAgente = """
            Você é um Engenheiro de Dados Educacionais especialista em concursos públicos.
            Sua única tarefa é ler a questão fornecida e alocá-la em EXATAMENTE UM dos tópicos da nossa [ÁRVORE OFICIAL].
            
            ### REGRAS OBRIGATÓRIAS ###
            1. CLASSIFICAÇÃO ESTRITA: Você DEVE escolher o tópico que melhor representa a essência da questão.
            2. CÓPIA FIEL: O nome do tópico escolhido DEVE ser idêntico (letra por letra) a um dos itens da [ÁRVORE OFICIAL].
            3. PROIBIÇÃO ABSOLUTA: Você é EXPRESSAMENTE PROIBIDO de inventar novos tópicos, alterar palavras ou criar exceções.
            4. ESPECIFICIDADE: Se houver tópicos pai e filho (ex: '2 Protocolos' e '2.1 TCP/IP'), prefira sempre o tópico mais específico (filho).
            
            ### FORMATO DE SAÍDA ###
            {format_instructions}

            ### DADOS DE ENTRADA ###
            [ÁRVORE OFICIAL DO PROFESSOR]:
            - {arvore}
            
            [QUESTÃO A SER CLASSIFICADA]:
            {enunciado}
            """;

        PromptTemplate promptTemplate = new PromptTemplate(templateAgente);
        Map<String, Object> params = Map.of(
            "arvore", listaConceitosFormatada,
            "enunciado", enunciado,
            "format_instructions", formatInstructions
        );

        try {
            ChatResponse response = this.chatClient.prompt(promptTemplate.render(params))
                .options(ChatOptions.builder().temperature(0.0).build()) 
                .call()
                .chatResponse();

            Usage usage = response.getMetadata().getUsage();
            cobrancaLlmService.deduzirCusto(usuario, usage.getPromptTokens(), usage.getCompletionTokens(), "gpt-4o");

            String respostaJson = response.getResult().getOutput().getText();

            ClassificacaoDTO decisao = outputConverter.convert(respostaJson);
            
            System.out.println("🎯 Questão classificada no tópico oficial: [" + decisao.getTopicoEscolhido() + "]");
            return decisao.getTopicoEscolhido();
            
        } catch (Exception e) {
            System.err.println("Erro no Agente Classificador: " + e.getMessage());
            return conceitoSugerido != null ? conceitoSugerido : "Geral";
        }
    }


    public List<String> listarConceitosPorDisciplina(String disciplinaId) {
        return bancoQuestaoRepository.findConceitosDistintosPorDisciplina(disciplinaId);
    }



    public List<ClassificacaoLoteDTO> normalizarConceitosEmLote(String questoesJson, List<String> conceitosExistentes, UsuarioEntity usuario) {
        
        if (conceitosExistentes == null || conceitosExistentes.isEmpty()) {
            return new ArrayList<>();
        }

        String listaConceitosFormatada = String.join("\n- ", conceitosExistentes);
        ObjectMapper objectMapper = new ObjectMapper();

        String formatoJsonExemplo = """
            [
              {
                "questaoId": "123",
                "topicoEscolhido": "Nome do Tópico Exato"
              }
            ]
            """;

        String templateAgente = """
            Você é um Engenheiro de Dados Educacionais especialista em concursos públicos.
            Sua tarefa é ler um LOTE de questões fornecidas e alocar CADA UMA DELAS em EXATAMENTE UM dos tópicos da nossa [ÁRVORE OFICIAL].
            
            ### REGRAS OBRIGATÓRIAS ###
            1. CLASSIFICAÇÃO ESTRITA: Você DEVE escolher o tópico que melhor representa a essência da questão.
            2. CÓPIA FIEL: O nome do tópico escolhido DEVE ser idêntico (letra por letra) a um dos itens da [ÁRVORE OFICIAL].
            3. PROIBIÇÃO ABSOLUTA: Você é EXPRESSAMENTE PROIBIDO de inventar novos tópicos, alterar palavras ou criar exceções.
            4. ESPECIFICIDADE: Se houver tópicos pai e filho (ex: '2 Protocolos' e '2.1 TCP/IP'), prefira sempre o tópico mais específico.
            
            ### FORMATO DE SAÍDA ###
            Retorne EXCLUSIVAMENTE um array JSON puro, sem marcações markdown, com a seguinte estrutura:
            {formato_saida}

            ### DADOS DE ENTRADA ###
            [ÁRVORE OFICIAL DO PROFESSOR]:
            - {arvore}
            
            [LOTE DE QUESTÕES A CLASSIFICAR]:
            {questoesJson}
            """;

        PromptTemplate promptTemplate = new PromptTemplate(templateAgente);
        
        Map<String, Object> params = Map.of(
            "arvore", listaConceitosFormatada,
            "questoesJson", questoesJson,
            "formato_saida", formatoJsonExemplo
        );

        try {

                ChatResponse response = this.anthropicChatClient.prompt(promptTemplate.render(params))
                .options(ChatOptions.builder().temperature(0.0).build()) 
                .call()
                .chatResponse();

            Usage usage = response.getMetadata().getUsage();
            cobrancaLlmService.deduzirCusto(usuario, usage.getPromptTokens(), usage.getCompletionTokens(), "gpt-4o");

            String respostaJson = response.getResult().getOutput().getText().replaceAll("(?s)```json\\s*|```", "").trim();

            List<ClassificacaoLoteDTO> classificacoes = objectMapper.readValue(
                respostaJson, 
                new com.fasterxml.jackson.core.type.TypeReference<List<ClassificacaoLoteDTO>>() {}
            );
            
            System.out.println("🎯 Lote de " + classificacoes.size() + " questões classificado com sucesso na Árvore Oficial.");
            return classificacoes;
            
        } catch (Exception e) {
            System.err.println("❌ Erro crítico no Agente Classificador em Lote: " + e.getMessage());
            return new ArrayList<>();
        }
    }


    
    @Async
    public void reorganizarBancoAssincrono(String disciplinaId, List<String> novaTaxonomia, UsuarioEntity usuario, String jobId) {

        System.out.println("[ASYNC] Iniciando recatalogação do banco para a disciplina: " + disciplinaId);

        try {
            System.out.println("Job ID" + jobId);
            ExtracaoJobEntity job = new ExtracaoJobEntity(jobId, "PROCESSING", "Recatalogação de Taxonomia", "EDICAO_TAXONOMIA");
            job.setDisciplinaId(disciplinaId);
            job.setModoExtracao("IA_LOTE");
            jobService.salvar(job);

            List<BancoQuestaoEntity> todasQuestoes = bancoQuestaoRepository.findByDisciplinaId(disciplinaId);
            if (todasQuestoes.isEmpty()) {
                System.out.println("Nenhuma questão encontrada para recatalogar.");
                
                job.setStatus("COMPLETED");
                job.setMensagemErro("Nenhuma questão encontrada para recatalogar.");
                jobService.salvar(job);
                sseNotificationService.notificarAtualizacao(disciplinaId);
                return;
            }

            ObjectMapper mapper = new ObjectMapper();
            int tamanhoLote = 20;

            for (int i = 0; i < todasQuestoes.size(); i += tamanhoLote) {
                int fim = Math.min(todasQuestoes.size(), i + tamanhoLote);
                List<BancoQuestaoEntity> lote = todasQuestoes.subList(i, fim);

                ArrayNode jsonArray = mapper.createArrayNode();
                for (BancoQuestaoEntity q : lote) {
                    ObjectNode no = mapper.createObjectNode();
                    no.put("questaoId", q.getId().toString()); 
                    no.put("enunciado", q.getEnunciado());
                    jsonArray.add(no);
                }
                
                String questoesJson = mapper.writeValueAsString(jsonArray);

                List<ClassificacaoLoteDTO> classificacoes = normalizarConceitosEmLote(questoesJson, novaTaxonomia, usuario);
                
                System.out.println("🤖 IA retornou " + classificacoes.size() + " classificações para o lote atual.");

                Map<String, String> mapaClassificacoes = classificacoes.stream()
                    .filter(c -> c.getQuestaoId() != null && c.getTopicoEscolhido() != null) 
                    .collect(Collectors.toMap(ClassificacaoLoteDTO::getQuestaoId, ClassificacaoLoteDTO::getTopicoEscolhido));

                int atualizadas = 0;
                for (BancoQuestaoEntity questao : lote) {
                    String novoConceito = mapaClassificacoes.get(questao.getId().toString());
                    
                    if (novoConceito != null) {
                        questao.setConceito(novoConceito);
                        atualizadas++;
                    } else {
                        System.out.println("⚠️ ALERTA: Conceito não encontrado/retornado para a Questão ID: " + questao.getId());
                    }
                }

                bancoQuestaoRepository.saveAll(lote);
                System.out.println("✅ Lote " + fim + "/" + todasQuestoes.size() + " salvo. Questões atualizadas: " + atualizadas);
            }
            
            ExtracaoJobEntity jobConcluido = jobService.consultarStatusJob(jobId);
            if (jobConcluido != null) {
                jobConcluido.setStatus("COMPLETED");
                ObjectMapper jsonMapper = new ObjectMapper();
                jobConcluido.setResultadoJson(jsonMapper.writeValueAsString(novaTaxonomia));
                jobService.salvar(jobConcluido);
                sseNotificationService.notificarAtualizacao(disciplinaId);
            }
            
            System.out.println("🎉 [ASYNC] Recatalogação concluída para a disciplina: " + disciplinaId);

        } catch (Exception e) {
            System.err.println("❌ Falha ao processar recatalogação: " + e.getMessage());
            
            try {
                ExtracaoJobEntity jobErro = jobService.consultarStatusJob(jobId);
                if (jobErro != null) {
                    jobErro.setStatus("ERROR");
                    jobErro.setMensagemErro(e.getMessage());
                    jobService.salvar(jobErro);
                    sseNotificationService.notificarAtualizacao(disciplinaId);
                }
            } catch (Exception ex) {
                System.err.println("Falha crítica ao atualizar job de erro: " + ex.getMessage());
            }
        }
    }


    
}