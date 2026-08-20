package com.Projeto.GeradorDeQuestoes.controllers;

import com.Projeto.GeradorDeQuestoes.dto.JobResumoDTO;
import com.Projeto.GeradorDeQuestoes.entities.ExtracaoJobEntity;
import com.Projeto.GeradorDeQuestoes.services.JobService;
import com.Projeto.GeradorDeQuestoes.services.SseNotificationService;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/admin/jobs")
@CrossOrigin(origins = "*")
public class JobController {

    private final JobService jobService;
    private final SseNotificationService sseNotificationService;

    public JobController(JobService jobService, 
        SseNotificationService sseNotificationService) {
        this.jobService = jobService;
        this.sseNotificationService = sseNotificationService;
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

    @PatchMapping("/disciplina/{disciplinaId}/marcar-vistos")
    public ResponseEntity<Void> marcarVisualizados(@PathVariable String disciplinaId) {
        jobService.marcarVisualizadosPorDisciplina(disciplinaId);
        return ResponseEntity.noContent().build(); 
    }

    @PatchMapping("/{id}/marcar-visto")
    public ResponseEntity<Void> marcarVisualizadoIndividual(@PathVariable String id) {
        try {
            jobService.marcarJobComoVisualizado(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
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

    @PutMapping("/{id}/consolidar")
    public ResponseEntity<?> consolidarJob(@PathVariable String id) {
        try {
            jobService.consolidarJob(id);
            return ResponseEntity.ok().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping(value = "/disciplina/{disciplinaId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamNotificacoes(@PathVariable String disciplinaId) {
        return sseNotificationService.subscribe(disciplinaId);
    }

    


}