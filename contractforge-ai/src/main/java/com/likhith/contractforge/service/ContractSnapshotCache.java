package com.likhith.contractforge.service;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.stereotype.Component;

import com.likhith.contractforge.model.ContractParseResult;

/**
 * Holds the most recent successful parse result in memory. A single
 * AtomicReference is sufficient here: parse runs are infrequent, reads are
 * simple lookups, and we only ever need the latest result, never a history.
 */
@Component
public class ContractSnapshotCache {

    private final AtomicReference<ContractParseResult> latest = new AtomicReference<>();

    public void store(ContractParseResult result) {
        latest.set(result);
    }

    public Optional<ContractParseResult> get() {
        return Optional.ofNullable(latest.get());
    }

    public boolean hasResult() {
        return latest.get() != null;
    }
}
