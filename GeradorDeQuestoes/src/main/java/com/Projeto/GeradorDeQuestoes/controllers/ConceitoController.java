package com.Projeto.GeradorDeQuestoes.controllers;

import com.Projeto.GeradorDeQuestoes.entities.ConceitoEntity;
import com.Projeto.GeradorDeQuestoes.services.ConceitoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/conceitos")
public class ConceitoController {

    private final ConceitoService conceitoService;

    public ConceitoController(ConceitoService conceitoService) {
        this.conceitoService = conceitoService;
    }

    @PostMapping
    public ResponseEntity<ConceitoEntity> criarConceito(@RequestBody ConceitoEntity conceito) {
        ConceitoEntity novoConceito = conceitoService.salvar(conceito);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoConceito);
    }

    @GetMapping
    public ResponseEntity<List<ConceitoEntity>> listarConceitos() {
        List<ConceitoEntity> conceitos = conceitoService.listarTodos();
        return ResponseEntity.ok(conceitos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConceitoEntity> buscarPorId(@PathVariable UUID id) {
        Optional<ConceitoEntity> conceito = conceitoService.buscarPorId(id);
        return conceito.map(ResponseEntity::ok)
                       .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/buscar")
    public ResponseEntity<ConceitoEntity> buscarPorNome(@RequestParam String nome) {
        Optional<ConceitoEntity> conceito = conceitoService.buscarPorNome(nome);
        return conceito.map(ResponseEntity::ok)
                       .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ConceitoEntity> atualizarConceito(@PathVariable UUID id, @RequestBody ConceitoEntity conceito) {
        try {
            ConceitoEntity conceitoAtualizado = conceitoService.atualizar(id, conceito);
            return ResponseEntity.ok(conceitoAtualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarConceito(@PathVariable UUID id) {
        try {
            conceitoService.deletar(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}