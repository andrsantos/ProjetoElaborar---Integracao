package com.Projeto.GeradorDeQuestoes.services.impl;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.Media;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

import java.util.Base64;
import java.util.List;

import com.Projeto.GeradorDeQuestoes.entities.UsuarioEntity;
import com.Projeto.GeradorDeQuestoes.services.CobrancaLlmService;
import com.Projeto.GeradorDeQuestoes.services.TradutorVisualService;

@Service
public class TradutorVisualServiceImpl implements TradutorVisualService {

    private final ChatClient anthropicChatClient;

    private CobrancaLlmService cobrancaLlmService;

    public TradutorVisualServiceImpl( @Qualifier("anthropicChatClient") ChatClient anthropicChatClient,
     CobrancaLlmService cobrancaLlmService) {
        this.anthropicChatClient = anthropicChatClient;
        this.cobrancaLlmService = cobrancaLlmService;
    }



    
    public String traduzirImagemComContexto(String base64Image, String textoDaPagina, UsuarioEntity usuario) {
        
        cobrancaLlmService.verificarSaldoMinimo(usuario);
        
        String instrucao = """
            Você é um Especialista em Avaliações Psicométricas.
            Você está recebendo duas informações:
            1. O texto completo de uma página de prova.
            2. Uma imagem extraída dessa mesma página.

            SUA MISSÃO:
            1. Leia o texto e descubra a qual questão esta imagem pertence (busque por referências como 'figura abaixo', 'diagrama a seguir', 'gráfico', 'tabela', etc.).
            2. Leia o enunciado dessa questão para entender o que está sendo cobrado.
            3. Descreva a imagem detalhadamente, focando ESTRITAMENTE nos elementos visuais técnicos necessários para resolver a questão. Pode ser um autômato, uma árvore binária, um circuito digital, um gráfico cartesiano, um trecho de código, uma topologia de rede ou um diagrama de classes. Descreva com rigor acadêmico.

            RETORNO OBRIGATÓRIO:
            Devolva APENAS uma string no formato exato abaixo, sem introduções ou saudações:
            VÍNCULO: [Número ou início da questão correspondente] | DESCRIÇÃO TÉCNICA: [Sua descrição detalhada]
            """;

        try {
            byte[] imageBytes = Base64.getDecoder().decode(base64Image);
            ByteArrayResource imageResource = new ByteArrayResource(imageBytes);
            Media imageMedia = new Media(MimeTypeUtils.IMAGE_PNG, imageResource);
            
            String conteudoTexto = instrucao + "\n\n--- TEXTO DA PÁGINA ---\n" + textoDaPagina;
            UserMessage userMessage = new UserMessage(conteudoTexto, List.of(imageMedia));
            
            ChatResponse response = anthropicChatClient.prompt(new Prompt(userMessage))
                    .options(ChatOptions.builder()
                            .temperature(0.1)
                            .build())
                    .call()
                    .chatResponse();

            if (response.getMetadata() != null && response.getMetadata().getUsage() != null) {
                Usage usage = response.getMetadata().getUsage();
                
                cobrancaLlmService.deduzirCusto(
                        usuario, 
                        usage.getPromptTokens(), 
                        usage.getCompletionTokens(), 
                        "claude-3-5-sonnet"
                );
            }

            return response.getResult().getOutput().getText().trim();

        } catch (Exception e) {
            System.err.println("Erro crítico ao traduzir imagem: " + e.getMessage());
            if (e instanceof com.Projeto.GeradorDeQuestoes.exceptions.SaldoInsuficienteException) {
                throw e; 
            }
            return ""; 
        }
    }


}
