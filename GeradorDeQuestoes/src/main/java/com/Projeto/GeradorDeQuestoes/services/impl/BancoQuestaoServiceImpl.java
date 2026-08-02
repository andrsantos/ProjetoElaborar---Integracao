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
import org.springframework.stereotype.Service;
import com.Projeto.GeradorDeQuestoes.dto.ClassificacaoDTO;
import com.Projeto.GeradorDeQuestoes.dto.ConceitoConfigDTO;
import com.Projeto.GeradorDeQuestoes.dto.GeracaoAutomaticaRequest;
import com.Projeto.GeradorDeQuestoes.dto.QuestaoDTO;
import com.Projeto.GeradorDeQuestoes.entities.UsuarioEntity;
import com.Projeto.GeradorDeQuestoes.enums.NivelTecnico;
import com.Projeto.GeradorDeQuestoes.repositories.BancoQuestaoRepository;
import com.Projeto.GeradorDeQuestoes.services.BancoQuestaoService;
import com.Projeto.GeradorDeQuestoes.services.CobrancaLlmService;


@Service
public class BancoQuestaoServiceImpl implements BancoQuestaoService {

    private BancoQuestaoRepository bancoQuestaoRepository;
    private final ChatClient chatClient;
    private final CobrancaLlmService cobrancaLlmService;


    BancoQuestaoServiceImpl(BancoQuestaoRepository bancoQuestaoRepository, 
        @Qualifier("openAiChatClient") ChatClient chatClient, 
        CobrancaLlmService cobrancaLlmService) {
        this.chatClient = chatClient;
        this.bancoQuestaoRepository = bancoQuestaoRepository;
        this.cobrancaLlmService = cobrancaLlmService;
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
    public List<QuestaoDTO> listarQuestoesPorTopico(String topico) {
        return bancoQuestaoRepository.findByTopico(topico).stream()
                .map(entity -> new QuestaoDTO(
                    entity.getId().toString(),
                        entity.getEnunciado(),
                        entity.getAlternativas(),
                        entity.getRespostaCorreta(),
                        entity.getConceito(),
                        entity.getCompetencia(),
                        entity.getComentarioTecnico(),
                        entity.getTopico(),
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

    // @Override
    // public String normalizarConceito(String enunciado, String conceitoSugerido, List<String> conceitosExistentes) {
        
    //     // =====================================================================
    //     // AGENTE 1: O EXTRATOR (Analisa a questão e gera jargões puros)
    //     // =====================================================================
        
    //     String templateAgente1 = """
    //         Você é um Engenheiro Sênior de Computação analisando uma questão técnica.
    //         Leia o enunciado abaixo e extraia a essência do assunto.
            
    //         ### REGRAS (BLACKLIST ESTRITA) ###
    //         1. Retorne no máximo 3 termos. Use jargões técnicos ou protocolos exatos (ex: TCP/IP, VLAN, BGP, CSMA/CD).
    //         2. É EXPRESSAMENTE PROIBIDO usar as palavras: "Conceitos", "Básicos", "Introdução", "Fundamentos", "Geral", "Teoria".
    //         3. Não crie frases longas. Maximize a concisão (1 ou 2 palavras).
            
    //         ### QUESTÃO ###
    //         {enunciado}
            
    //         Gere a lista com os termos:
    //         """;
            
    //     List<String> jargoesExtraidos;
    //     try {
    //         String respostaAgente1 = this.chatClient.prompt()
    //             .user(u -> u.text(templateAgente1).param("enunciado", enunciado))
    //             .options(ChatOptions.builder().temperature(0.1).build())
    //             .call()
    //             .content();
                
    //         jargoesExtraidos = Arrays.stream(respostaAgente1.split("\n"))
    //             .map(linha -> linha.replace("-", "").replace("*", "").trim())
    //             .filter(linha -> !linha.isBlank())
    //             .toList();
                
    //         System.out.println("🤖 Agente 1 (Extrator) gerou: " + jargoesExtraidos);
            
    //     } catch (Exception e) {
    //         System.err.println("Erro no Agente 1: " + e.getMessage());
    //         jargoesExtraidos = List.of(conceitoSugerido != null ? conceitoSugerido : "Geral");
    //     }

    //     // =====================================================================
    //     // AGENTE 2: O BIBLIOTECÁRIO (Decide se recicla a árvore ou cria um galho)
    //     // =====================================================================


    //     String listaConceitosFormatada = (conceitosExistentes == null || conceitosExistentes.isEmpty()) 
    //         ? "Nenhum tópico estrutural cadastrado. Baseie-se apenas nos jargões extraídos." 
    //         : String.join("\n- ", conceitosExistentes);

    //     var outputConverter = new BeanOutputConverter<>(DecisaoTaxonomiaDTO.class);
    //     String formatInstructions = outputConverter.getFormat();

    //     String templateAgente2 = """
    //         Você é um Arquiteto de Dados Educacionais. Sua missão é classificar a questão no banco de dados.
            
    //         Você tem duas fontes de dados abaixo:
    //         1. [ÁRVORE OFICIAL]: Os macro-tópicos que o professor já cadastrou.
    //         2. [JARGÕES EXTRAÍDOS]: Os termos técnicos específicos desta questão.
            
    //         ### REGRAS (EM ORDEM DE PRIORIDADE) ###
    //         1. RECICLAGEM OBRIGATÓRIA: Se a essência técnica dos jargões extraídos pertencer, mesmo que de forma ampla, a um dos itens da [ÁRVORE OFICIAL], você DEVE retornar EXATAMENTE o nome do tópico oficial. 
    //            Ex: Se o jargão é 'IPv4' e a árvore possui 'Redes de Computadores e a Internet', use a árvore.
               
    //         2. CRIAÇÃO DE EXCEÇÃO (FALLBACK): Se (e somente se) o tema for TÃO ESPECÍFICO que não caiba em NENHUM tópico da árvore, você pode criar uma nova tag.
    //            - Se criar, você DEVE usar um dos termos exatos da lista de [JARGÕES EXTRAÍDOS].
    //            - NUNCA invente palavras. NUNCA crie frases longas. NUNCA use palavras genéricas.
            
    //         ### FORMATO DE SAÍDA ###
    //         {format_instructions}

    //         ### DADOS DE ENTRADA ###
    //         [ÁRVORE OFICIAL DO PROFESSOR]:
    //         - {arvore}
            
    //         [JARGÕES EXTRAÍDOS DESTA QUESTÃO]:
    //         {jargoes}
    //         """;

    //     PromptTemplate promptTemplate2 = new PromptTemplate(templateAgente2);
    //     Map<String, Object> params = Map.of(
    //         "arvore", listaConceitosFormatada,
    //         "jargoes", String.join(", ", jargoesExtraidos),
    //         "format_instructions", formatInstructions
    //     );

    //     try {
    //         String respostaJson = this.chatClient.prompt(promptTemplate2.render(params))
    //             .options(ChatOptions.builder().temperature(0.0).build()) 
    //             .call()
    //             .content();

    //         DecisaoTaxonomiaDTO decisao = outputConverter.convert(respostaJson);

    //         if (decisao.getConceitoEhNovo()) {
    //             System.out.println("✨ Novo galho técnico criado: [" + decisao.getConceitoFinal() + "] | Motivo: " + decisao.getJustificativa());
    //         } else {
    //             System.out.println("♻️ Reciclagem bem-sucedida: [" + decisao.getConceitoFinal() + "] | Motivo: " + decisao.getJustificativa());
    //         }

    //         return decisao.getConceitoFinal();
            
    //     } catch (Exception e) {
    //         System.err.println("Erro no Agente 2 (Bibliotecário): " + e.getMessage());
    //         return !jargoesExtraidos.isEmpty() ? jargoesExtraidos.get(0) : "Geral";
    //     }
    // }

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

    
}