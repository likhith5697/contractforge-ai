package com.likhith.contractforge.model;

import java.util.List;

/**
 * The outcome of running {@code ScenarioValidator} against one LLM response:
 * scenarios that passed every deterministic check, and scenarios (or the whole
 * batch) that didn't, each with the specific reasons.
 */
public record ScenarioValidationResult(
        List<ApiScenario> accepted,
        List<ScenarioRejection> rejected) {
}
