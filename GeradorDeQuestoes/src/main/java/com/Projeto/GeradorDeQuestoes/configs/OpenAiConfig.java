package com.Projeto.GeradorDeQuestoes.configs;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAiConfig {

    @Bean(name = "openAiChatClient")
    public ChatClient openAiChatClient(
            @Qualifier("openAiChatModel") ChatModel openAiChatModel) {

        return ChatClient.create(openAiChatModel);
    }
}