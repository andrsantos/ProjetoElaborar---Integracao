package com.Projeto.GeradorDeQuestoes.services.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.Projeto.GeradorDeQuestoes.entities.DisciplinaEntity;
import com.Projeto.GeradorDeQuestoes.entities.DocumentosReferenciaEntity;
import com.Projeto.GeradorDeQuestoes.entities.TemplateDisciplinaEntity; 
import com.Projeto.GeradorDeQuestoes.entities.TemplateTopicoEntity;
import com.Projeto.GeradorDeQuestoes.entities.UsuarioEntity;
import com.Projeto.GeradorDeQuestoes.repositories.BancoQuestaoRepository;
import com.Projeto.GeradorDeQuestoes.repositories.ConceitoRepository;
import com.Projeto.GeradorDeQuestoes.repositories.DisciplinaRepository;
import com.Projeto.GeradorDeQuestoes.repositories.DocumentosReferenciaRepository;
import com.Projeto.GeradorDeQuestoes.repositories.PromptRepository;
import com.Projeto.GeradorDeQuestoes.repositories.ProvaRepository;
import com.Projeto.GeradorDeQuestoes.repositories.TemplateDisciplinaRepository; 
import com.Projeto.GeradorDeQuestoes.services.ConceitoService;
import com.Projeto.GeradorDeQuestoes.services.DisciplinaService;
import jakarta.transaction.Transactional;

@Service
public class DisciplinaServiceImpl implements DisciplinaService{

    @Autowired
    private DisciplinaRepository disciplinaRepository;

    @Autowired
    private DocumentosReferenciaRepository documentosReferenciaRepository;

    @Autowired
    private BancoQuestaoRepository bancoQuestaoRepository;

    @Autowired
    private ConceitoRepository conceitoRepository;

    @Autowired
    private ProvaRepository provaRepository;

    @Autowired
    private PromptRepository promptRepository;

    @Autowired
    private ConceitoService conceitoService;

    @Autowired
    private TemplateDisciplinaRepository templateDisciplinaRepository; 

    @Override
    public List<DisciplinaEntity> listarTodas() {
       return disciplinaRepository.findAll();
    }

    @Override
    public Optional<DisciplinaEntity> buscarPorId(String id) {
        return disciplinaRepository.findById(id);
    }

    @Override
    @Transactional 
    public DisciplinaEntity salvar(DisciplinaEntity disciplina) {

        Optional<DisciplinaEntity> existente = disciplinaRepository.findByNomeIgnoreCase(disciplina.getNome());
        if (existente.isPresent() && !existente.get().getId().equals(disciplina.getId())) {
            throw new RuntimeException("Já existe uma disciplina cadastrada com o nome: " + disciplina.getNome());
        }

        DisciplinaEntity disciplinaSalva = disciplinaRepository.save(disciplina);

        try {
            System.out.println("🌱 Iniciando o plantio da Árvore Semente para: " + disciplinaSalva.getNome());
            
            List<String> macroTopicos = null;
            String nomeNormalizado = disciplinaSalva.getNome().toLowerCase().trim();
            
            List<TemplateDisciplinaEntity> templates = templateDisciplinaRepository.findAll();
            TemplateDisciplinaEntity templateMatch = null;

            for (TemplateDisciplinaEntity t : templates) {
                if (t.getNomeDisciplina().toLowerCase().equals(nomeNormalizado)) {
                    templateMatch = t;
                    break;
                }
                for (String palavra : t.getPalavrasChave()) {
                    if (nomeNormalizado.contains(palavra.toLowerCase())) {
                        templateMatch = t;
                        break;
                    }
                }
                if (templateMatch != null) break;
            }

            if (templateMatch != null) {
                System.out.println("🎯 Match encontrado no banco! Usando template padrão de: " + templateMatch.getNomeDisciplina());
                macroTopicos = templateMatch.getTopicos().stream()
                        .map(TemplateTopicoEntity::getNome)
                        .collect(Collectors.toList());
            } else {
                System.out.println("🤖 Nenhum template compatível. Acionando IA (Claude) para gerar árvore semente...");
                macroTopicos = conceitoService.gerarArvoreSemente(
                    disciplinaSalva.getNome(), 
                    disciplinaSalva.getDescricao()
                );
            }

            if (macroTopicos != null) {
                for (String topico : macroTopicos) {
                    conceitoService.salvarConceitoSemente(topico, disciplinaSalva.getId());
                }
                System.out.println("✅ Árvore Semente criada com " + macroTopicos.size() + " tópicos.");
            }

        } catch (Exception e) {
            System.err.println("⚠️ Falha ao gerar a Árvore Semente. A disciplina foi criada vazia. Erro: " + e.getMessage());
        }

        return disciplinaSalva;
    }

    @Override
    public void deletar(String id) {
        if (!disciplinaRepository.existsById(id)) {
            throw new RuntimeException("Disciplina não encontrada para exclusão.");
        }
        disciplinaRepository.deleteById(id);
    }

    @Override
    @Transactional 
    public void deletarDisciplina(String id) {
        if (!disciplinaRepository.existsById(id)) {
            throw new RuntimeException("Disciplina não encontrada para exclusão.");
        }

        conceitoRepository.deleteByDisciplina(id);
        bancoQuestaoRepository.deleteByDisciplinaId(id);

        List<DocumentosReferenciaEntity> documentos = documentosReferenciaRepository.findByDisciplinaId(id);
        
        if (documentos != null && !documentos.isEmpty()) {
            List<String> documentosIds = documentos.stream()
                    .map(DocumentosReferenciaEntity::getId)
                    .collect(Collectors.toList());
            
            promptRepository.deleteByDocumentoIdIn(documentosIds);
        }

        documentosReferenciaRepository.deleteByDisciplinaId(id);
        provaRepository.deleteByDisciplinaId(id);
        disciplinaRepository.deleteById(id);
        
        System.out.println("🗑️ Disciplina e todos os seus rastros (conceitos, questões, prompts, provas e documentos) foram excluídos com sucesso. ID: " + id);
    }

    @Override
    public List<DisciplinaEntity> listarTodasPorUsuario(UsuarioEntity usuarioLogado) {
        List<DisciplinaEntity> disciplinas = disciplinaRepository.findAllByUsuarioId(usuarioLogado.getId());
        return disciplinas;
    }
}