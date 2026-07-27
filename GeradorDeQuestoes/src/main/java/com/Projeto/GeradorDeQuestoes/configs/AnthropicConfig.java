package com.Projeto.GeradorDeQuestoes.configs;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AnthropicConfig {

    @Bean(name = "anthropicChatClient")
    public ChatClient anthropicChatClient(
            @Qualifier("anthropicChatModel") ChatModel anthropicChatModel) {
        return ChatClient.create(anthropicChatModel);
    }
}