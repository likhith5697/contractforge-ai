package com.likhith.contractforge.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.likhith.contractforge.exception.SnapshotNotFoundException;
import com.likhith.contractforge.exception.SnapshotsNotAvailableException;
import com.likhith.contractforge.model.ContractParseResult;
import com.likhith.contractforge.model.EndpointSnapshot;
import com.likhith.contractforge.service.ContractParseService;
import com.likhith.contractforge.service.ContractSnapshotCache;
import com.likhith.contractforge.util.EndpointIdCodec;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
public class ContractParseController {

    private static final Logger log = LoggerFactory.getLogger(ContractParseController.class);

    private final ContractParseService contractParseService;
    private final ContractSnapshotCache cache;

    @PostMapping("/parse")
    public ResponseEntity<ContractParseResult> parse() {
        log.info("Received request to parse the configured OpenAPI source");
        return ResponseEntity.ok(contractParseService.parseConfiguredSource());
    }

    @GetMapping("/snapshots")
    public ResponseEntity<ContractParseResult> getSnapshots() {
        ContractParseResult result = cache.get()
                .orElseThrow(() -> new SnapshotsNotAvailableException(
                        "No contract has been parsed yet. Call POST /api/contracts/parse first."));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/snapshots/{encodedEndpointId}")
    public ResponseEntity<EndpointSnapshot> getSnapshot(@PathVariable String encodedEndpointId) {
        ContractParseResult result = cache.get()
                .orElseThrow(() -> new SnapshotsNotAvailableException(
                        "No contract has been parsed yet. Call POST /api/contracts/parse first."));

        String endpointId = EndpointIdCodec.decode(encodedEndpointId);

        EndpointSnapshot snapshot = result.endpoints().stream()
                .filter(e -> e.endpointId().equals(endpointId))
                .findFirst()
                .orElseThrow(() -> new SnapshotNotFoundException(
                        "No snapshot found for endpoint id '" + endpointId + "'"));

        return ResponseEntity.ok(snapshot);
    }
}
