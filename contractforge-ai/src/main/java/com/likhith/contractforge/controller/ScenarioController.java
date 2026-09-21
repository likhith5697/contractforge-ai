package com.likhith.contractforge.controller;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.likhith.contractforge.exception.ScenarioArtifactNotFoundException;
import com.likhith.contractforge.model.ScenarioArtifact;
import com.likhith.contractforge.model.ScenarioGenerationRequest;
import com.likhith.contractforge.model.ScenarioGenerationResponse;
import com.likhith.contractforge.service.ScenarioArtifactStore;
import com.likhith.contractforge.service.ScenarioGenerationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/scenarios")
@RequiredArgsConstructor
public class ScenarioController {

    private static final Logger log = LoggerFactory.getLogger(ScenarioController.class);

    private final ScenarioGenerationService scenarioGenerationService;
    private final ScenarioArtifactStore scenarioArtifactStore;

    @PostMapping("/generate")
    public ResponseEntity<ScenarioGenerationResponse> generate(@Valid @RequestBody ScenarioGenerationRequest request) {
        log.info("Received scenario generation request for endpoint {}", request.endpointId());
        return ResponseEntity.ok(scenarioGenerationService.generate(request));
    }

    @GetMapping("/artifacts/{artifactId}")
    public ResponseEntity<ScenarioArtifact> getArtifact(@PathVariable UUID artifactId) {
        ScenarioArtifact artifact = scenarioArtifactStore.findById(artifactId)
                .orElseThrow(() -> new ScenarioArtifactNotFoundException(
                        "No scenario artifact found for id '" + artifactId + "'"));
        return ResponseEntity.ok(artifact);
    }
}
