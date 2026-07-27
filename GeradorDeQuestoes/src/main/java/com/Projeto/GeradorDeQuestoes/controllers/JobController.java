package com.Projeto.GeradorDeQuestoes.controllers;

import com.Projeto.GeradorDeQuestoes.entities.ExtracaoJobEntity;
import com.Projeto.GeradorDeQuestoes.repositories.ExtracaoJobRepository;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/jobs")
@CrossOrigin(origins = "*")
public class JobController {

    private final ExtracaoJobRepository jobRepository;

    public JobController(ExtracaoJobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExtracaoJobEntity> consultarStatusJob(@PathVariable String id) {

        return jobRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
                
    }

    @GetMapping
    public ResponseEntity<List<ExtracaoJobEntity>> listarJobs() {

        return ResponseEntity.ok(jobRepository.findAllByOrderByDataCriacaoDesc());
        
    }

    
}