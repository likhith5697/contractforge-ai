package com.likhith.contractforge.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.likhith.contractforge.model.PipelineRunReport;
import com.likhith.contractforge.model.PipelineRunRequest;
import com.likhith.contractforge.service.PipelineOrchestrationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * The single "run everything" entrypoint: parse banking-api's contract,
 * generate and validate scenarios for every discovered endpoint, execute the
 * accepted ones against a validated Test API target, and return one
 * consolidated report. A request body is entirely optional.
 */
@RestController
@RequestMapping("/api/pipeline")
@RequiredArgsConstructor
public class PipelineController {

    private static final Logger log = LoggerFactory.getLogger(PipelineController.class);

    private final PipelineOrchestrationService pipelineOrchestrationService;

    @PostMapping("/run")
    public ResponseEntity<PipelineRunReport> runAll(
            @Valid @RequestBody(required = false) PipelineRunRequest request) {
        PipelineRunRequest effectiveRequest = request != null ? request : new PipelineRunRequest(null, null, null);
        log.info("Received pipeline run-all request");
        return ResponseEntity.ok(pipelineOrchestrationService.runAll(effectiveRequest));
    }
}
