package com.Projeto.GeradorDeQuestoes.services.impl;

import com.Projeto.GeradorDeQuestoes.dto.TaxonomiaDTO;
import com.Projeto.GeradorDeQuestoes.entities.ConceitoEntity;
import com.Projeto.GeradorDeQuestoes.entities.UsuarioEntity;
import com.Projeto.GeradorDeQuestoes.repositories.ConceitoRepository;
import com.Projeto.GeradorDeQuestoes.services.BancoQuestaoService;
import com.Projeto.GeradorDeQuestoes.services.CobrancaLlmService;
import com.Projeto.GeradorDeQuestoes.services.ConceitoService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.ListOutputConverter;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ConceitoServiceImpl implements ConceitoService {

    private final ConceitoRepository conceitoRepository;
    private final ChatClient anthropicChatClient;
    private final CobrancaLlmService cobrancaLlmService;
    private final BancoQuestaoService bancoQuestaoService;

    public ConceitoServiceImpl(ConceitoRepository conceitoRepository,
        ChatClient anthropicChatClient,
        CobrancaLlmService cobrancaLlmService, 
        BancoQuestaoService bancoQuestaoService) {
        this.conceitoRepository = conceitoRepository;
        this.anthropicChatClient = anthropicChatClient;
        this.cobrancaLlmService = cobrancaLlmService;
        this.bancoQuestaoService = bancoQuestaoService;
    }

    @Override
    @Transactional
    public ConceitoEntity salvar(ConceitoEntity conceito) {
        return conceitoRepository.save(conceito);
    }

    @Override
    public Optional<ConceitoEntity> buscarPorId(UUID id) {
        return conceitoRepository.findById(id);
    }

    @Override
    public Optional<ConceitoEntity> buscarPorNome(String nome) {
        return conceitoRepository.findByNomeIgnoreCase(nome);
    }

    @Override
    public List<ConceitoEntity> listarTodos() {
        return conceitoRepository.findAll();
    }

    @Override
    @Transactional
    public ConceitoEntity atualizar(UUID id, ConceitoEntity conceitoAtualizado) {
        return conceitoRepository.findById(id).map(conceitoExistente -> {
            conceitoExistente.setNome(conceitoAtualizado.getNome());
            conceitoExistente.setDescricao(conceitoAtualizado.getDescricao());
            conceitoExistente.setDisciplina(conceitoAtualizado.getDisciplina());
            conceitoExistente.setTipoOrigem(conceitoAtualizado.getTipoOrigem());
            conceitoExistente.setOrigemId(conceitoAtualizado.getOrigemId());
            return conceitoRepository.save(conceitoExistente);
        }).orElseThrow(() -> new RuntimeException("Conceito não encontrado com o ID: " + id));
    }

    @Override
    @Transactional
    public void deletar(UUID id) {
        if (conceitoRepository.existsById(id)) {
            conceitoRepository.deleteById(id);
        } else {
            throw new RuntimeException("Conceito não encontrado com o ID: " + id);
        }
    }

    @Override
    @Transactional
    public ConceitoEntity processarConceitoQuestao(String nomeConceito, String disciplina, ConceitoEntity.TipoOrigem novaOrigem, UUID origemId) {
        Optional<ConceitoEntity> conceitoExistente = conceitoRepository.findByNomeIgnoreCase(nomeConceito);
        if (conceitoExistente.isPresent()) {
            ConceitoEntity conceito = conceitoExistente.get();
            if (conceito.getTipoOrigem() == ConceitoEntity.TipoOrigem.DOCUMENTO && novaOrigem == ConceitoEntity.TipoOrigem.PROVA) {
                conceito.setTipoOrigem(ConceitoEntity.TipoOrigem.PROVA);
                conceito.setOrigemId(origemId);
                return conceitoRepository.save(conceito);
            }
            return conceito;
        }

        ConceitoEntity novoConceito = new ConceitoEntity();
        novoConceito.setNome(nomeConceito);
        novoConceito.setDisciplina(disciplina);
        novoConceito.setTipoOrigem(novaOrigem);
        novoConceito.setOrigemId(origemId);
        
        return conceitoRepository.save(novoConceito);
    }

    @Override
    public List<String> listarConceitosPorDisciplina(String disciplinaId) {
        List<ConceitoEntity> conceitos = conceitoRepository.findByDisciplina(disciplinaId);
        
        return conceitos.stream()
            .map(ConceitoEntity::getNome)
            .collect(Collectors.toList());
    }


    @Override
    @Transactional
    public void sincronizarTaxonomia(String disciplinaId, TaxonomiaDTO taxonomiaDTO, UsuarioEntity usuario) {
        List<ConceitoEntity> conceitosAntigos = conceitoRepository.findByDisciplina(disciplinaId);
        
        if (!conceitosAntigos.isEmpty()) {
            conceitoRepository.deleteAll(conceitosAntigos);
        }

        if (taxonomiaDTO.getTopicos() != null && !taxonomiaDTO.getTopicos().isEmpty()) {
            List<ConceitoEntity> novosConceitos = taxonomiaDTO.getTopicos().stream().map(nomeTopico -> {
                ConceitoEntity conceito = new ConceitoEntity();
                conceito.setNome(nomeTopico);
                conceito.setDisciplina(disciplinaId);
                conceito.setTipoOrigem(ConceitoEntity.TipoOrigem.DOCUMENTO); 
                return conceito;
            }).collect(Collectors.toList());
            conceitoRepository.saveAll(novosConceitos);
        }

        if (taxonomiaDTO.getTopicos() != null && !taxonomiaDTO.getTopicos().isEmpty()) {
            String jobId = UUID.randomUUID().toString();
            bancoQuestaoService.reorganizarBancoAssincrono(disciplinaId, taxonomiaDTO.getTopicos(), usuario, jobId);
        }
    }

    @Override
    public List<String> gerarArvoreSemente(String nomeDisciplina, String descricaoDisciplina,
        UsuarioEntity usuario) {
        var outputConverter = new ListOutputConverter(new DefaultConversionService());
        String formatInstructions = outputConverter.getFormat();

        String descricaoSegura = (descricaoDisciplina != null && !descricaoDisciplina.isBlank())
                                 ? descricaoDisciplina
                                 : "Descrição não fornecida pelo professor.";

        String templateBase = """
            Você é um Coordenador Pedagógico Universitário construindo a taxonomia de um banco de questões.
            Sua missão é criar a estrutura inicial de tópicos para a seguinte disciplina:

            Nome da Disciplina: {nome}
            Descrição/Ementa: {descricao}

            ### REGRAS ###
            1. Crie APENAS Macro-Tópicos (os grandes capítulos da disciplina).
            2. Não crie micro-conceitos. O nível deve ser alto e estrutural (Ex: "Camada de Rede", e não "Protocolo IPv4").
            3. Gere no mínimo 4 e no máximo 8 tópicos essenciais.
            4. Os nomes devem ser extremamente curtos e atômicos (máximo 3 palavras).

            ### FORMATO DE SAÍDA ###
            {format_instructions}
            """;

        PromptTemplate promptTemplate = new PromptTemplate(templateBase);
        Map<String, Object> params = Map.of(
            "nome", nomeDisciplina,
            "descricao", descricaoSegura,
            "format_instructions", formatInstructions
        );

        try {
            ChatResponse response = this.anthropicChatClient.prompt(promptTemplate.render(params))
                .call()
                .chatResponse();

            Usage usage = response.getMetadata().getUsage();
            cobrancaLlmService.deduzirCusto(usuario, usage.getPromptTokens(), usage.getCompletionTokens(), "claude-haiku");

            String respostaJson = response.getResult().getOutput().getText();

            return outputConverter.convert(respostaJson);

        } catch (Exception e) {
            System.err.println("⚠️ Erro no LLM ao gerar Árvore Semente. Injetando fallback estrutural: " + e.getMessage());
            return List.of("Fundamentos de " + nomeDisciplina, "Tópicos Avançados", "Aplicações Práticas");
        }
    }

    @Override
    @Transactional
    public ConceitoEntity salvarConceitoSemente(String nomeTopico, String disciplinaId) {
        Optional<ConceitoEntity> conceitoExistente = conceitoRepository.findByNomeIgnoreCase(nomeTopico);
        if (conceitoExistente.isPresent()) {
            return conceitoExistente.get();
        }

        ConceitoEntity semente = new ConceitoEntity();
        semente.setNome(nomeTopico);
        semente.setDisciplina(disciplinaId);
        semente.setTipoOrigem(ConceitoEntity.TipoOrigem.DOCUMENTO);
        
        return conceitoRepository.save(semente);
    }
}