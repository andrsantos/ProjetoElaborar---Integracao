package com.Projeto.GeradorDeQuestoes.controllers;

import com.Projeto.GeradorDeQuestoes.dto.JobResumoDTO;
import com.Projeto.GeradorDeQuestoes.entities.ExtracaoJobEntity;
import com.Projeto.GeradorDeQuestoes.services.JobService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/jobs")
@CrossOrigin(origins = "*")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExtracaoJobEntity> consultarStatusJob(@PathVariable String id) {
        ExtracaoJobEntity job = jobService.consultarStatusJob(id);
        if (job != null) {
            return ResponseEntity.ok(job);
        }
        return ResponseEntity.notFound().build();
    }

    

    @GetMapping
    public ResponseEntity<List<ExtracaoJobEntity>> listarJobs() {
        List<ExtracaoJobEntity> jobs = jobService.listarJobs();
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/disciplina/{disciplinaId}")
    public ResponseEntity<List<JobResumoDTO>> listarJobsPorDisciplina(@PathVariable String disciplinaId) {
        List<JobResumoDTO> jobsResumo = jobService.listarJobsPorDisciplina(disciplinaId);
        return ResponseEntity.ok(jobsResumo);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletarJob(@PathVariable String id) {
        try {
            jobService.deletarJob(id);
            return ResponseEntity.noContent().build(); 
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.notFound().build(); 
        }
    }


}