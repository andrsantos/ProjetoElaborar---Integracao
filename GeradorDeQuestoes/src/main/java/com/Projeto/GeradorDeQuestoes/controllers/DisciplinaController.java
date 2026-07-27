package com.Projeto.GeradorDeQuestoes.controllers;

import com.Projeto.GeradorDeQuestoes.dto.DisciplinaDTO;
import com.Projeto.GeradorDeQuestoes.entities.DisciplinaEntity;
import com.Projeto.GeradorDeQuestoes.entities.UsuarioEntity;
import com.Projeto.GeradorDeQuestoes.services.DisciplinaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/disciplinas")
public class DisciplinaController {

    @Autowired
    private DisciplinaService service;


    @GetMapping
    public ResponseEntity<List<DisciplinaEntity>> listarMinhasDisciplinas(
            @AuthenticationPrincipal UsuarioEntity usuarioLogado) {
        
        List<DisciplinaEntity> disciplinas = service.listarTodasPorUsuario(usuarioLogado);
        
        return ResponseEntity.ok(disciplinas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DisciplinaEntity> buscarPorId(@PathVariable String id) {

        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());

    }

    @PostMapping
    public ResponseEntity<DisciplinaEntity> criarDisciplina(
            @RequestBody DisciplinaDTO dto,
            @AuthenticationPrincipal UsuarioEntity usuarioLogado) {
        
        DisciplinaEntity novaDisciplina = new DisciplinaEntity(dto.getNome(), dto.getDescricao(), usuarioLogado);
        

        service.salvar(novaDisciplina);
        return ResponseEntity.ok(novaDisciplina);
    }



    // @PostMapping
    // public ResponseEntity<?> criar(@RequestBody DisciplinaEntity disciplina) {

    //     try {
    //         DisciplinaEntity salva = service.salvar(disciplina);
    //         return ResponseEntity.status(HttpStatus.CREATED).body(salva);
    //     } catch (RuntimeException e) {
    //         return ResponseEntity.badRequest().body(e.getMessage());
    //     }

    // }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable String id, @RequestBody DisciplinaEntity disciplina) {

        return service.buscarPorId(id).map(existente -> {
            existente.setNome(disciplina.getNome());
            try {
                DisciplinaEntity atualizada = service.salvar(existente);
                return ResponseEntity.ok(atualizada);
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        }).orElse(ResponseEntity.notFound().build());

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable String id) {

        try {
            service.deletar(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

    @GetMapping("/{id}/nome")
    public ResponseEntity<?> buscarNomePorId(@PathVariable String id) {

        return service.buscarPorId(id)
                .map(disciplina -> ResponseEntity.ok(java.util.Map.of("nome", disciplina.getNome())))
                .orElse(ResponseEntity.notFound().build());

    }

    @DeleteMapping("/{id}/disciplina")
    public ResponseEntity<?> deletarDisciplina(@PathVariable String id) {
        try {
            service.deletarDisciplina(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("erro", e.getMessage()));
        }

    }


}