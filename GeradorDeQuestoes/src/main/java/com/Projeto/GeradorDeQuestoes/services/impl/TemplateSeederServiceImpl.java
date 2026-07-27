package com.Projeto.GeradorDeQuestoes.services.impl;

import java.io.InputStream;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import com.Projeto.GeradorDeQuestoes.dto.TemplateDisciplinaDTO;
import com.Projeto.GeradorDeQuestoes.entities.TemplateDisciplinaEntity;
import com.Projeto.GeradorDeQuestoes.entities.TemplateTopicoEntity;
import com.Projeto.GeradorDeQuestoes.repositories.TemplateDisciplinaRepository;
import com.Projeto.GeradorDeQuestoes.services.TemplateSeederService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;

@Service
public class TemplateSeederServiceImpl implements TemplateSeederService {

    private final TemplateDisciplinaRepository repository;
    private final ObjectMapper objectMapper;

    public TemplateSeederServiceImpl(TemplateDisciplinaRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }



    @PostConstruct
    public void popularTemplatesIniciais() {
        System.out.println("🔍 Verificando templates de árvores de conhecimento...");

        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:templates-arvores/*.json");

            for (Resource resource : resources) {
                try (InputStream is = resource.getInputStream()) {
                    TemplateDisciplinaDTO dto = objectMapper.readValue(is, TemplateDisciplinaDTO.class);

                    if (!repository.existsByNomeDisciplinaIgnoreCase(dto.getNomeDisciplina())) {
                        System.out.println("🌱 Semeando template: " + dto.getNomeDisciplina());
                        salvarTemplateNoBanco(dto);
                    } else {
                        System.out.println("✅ Template já existe no banco: " + dto.getNomeDisciplina());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Erro ao tentar processar os arquivos de template: " + e.getMessage());
        }
    }

    private void salvarTemplateNoBanco(TemplateDisciplinaDTO dto) {
        TemplateDisciplinaEntity entity = new TemplateDisciplinaEntity();
        entity.setNomeDisciplina(dto.getNomeDisciplina());
        
        if (dto.getPalavrasChave() != null) {
            entity.setPalavrasChave(dto.getPalavrasChave());
        }

        if (dto.getTopicos() != null) {
            for (String nomeTopico : dto.getTopicos()) {
                TemplateTopicoEntity topico = new TemplateTopicoEntity();
                topico.setNome(nomeTopico);
                entity.addTopico(topico); 
            }
        }

        repository.save(entity);
    }
    
}
    

