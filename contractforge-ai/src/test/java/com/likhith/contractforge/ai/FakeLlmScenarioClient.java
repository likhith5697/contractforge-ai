package com.likhith.contractforge.ai;

import com.likhith.contractforge.model.EndpointSnapshot;
import com.likhith.contractforge.model.LlmScenarioBatch;

/**
 * Test double standing in for a real OpenAI call. Tests configure exactly one
 * of a batch to return or an exception to throw, then invoke the service -
 * no network call is ever made.
 */
public class FakeLlmScenarioClient implements LlmScenarioClient {

    private LlmScenarioBatch batchToReturn;
    private RuntimeException exceptionToThrow;

    public void willReturn(LlmScenarioBatch batch) {
        this.batchToReturn = batch;
        this.exceptionToThrow = null;
    }

    public void willThrow(RuntimeException exception) {
        this.exceptionToThrow = exception;
        this.batchToReturn = null;
    }

    @Override
    public LlmScenarioBatch generateScenarios(EndpointSnapshot endpoint, String intent, int maxScenarios) {
        if (exceptionToThrow != null) {
            throw exceptionToThrow;
        }
        return batchToReturn;
    }
}
