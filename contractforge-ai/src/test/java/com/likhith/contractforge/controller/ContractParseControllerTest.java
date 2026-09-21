package com.likhith.contractforge.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.likhith.contractforge.model.ContractParseResult;
import com.likhith.contractforge.model.EndpointSnapshot;
import com.likhith.contractforge.service.ContractParseService;
import com.likhith.contractforge.service.ContractSnapshotCache;
import com.likhith.contractforge.util.EndpointIdCodec;

/**
 * Verifies the REST contract of ContractParseController in isolation: the
 * heavier parsing logic is already covered by ContractParseServiceTest, so
 * ContractParseService and ContractSnapshotCache are mocked here.
 */
@WebMvcTest(ContractParseController.class)
class ContractParseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ContractParseService contractParseService;

    @MockBean
    private ContractSnapshotCache cache;

    @Test
    void snapshotsBeforeAnyParseReturns409() throws Exception {
        given(cache.get()).willReturn(Optional.empty());

        mockMvc.perform(get("/api/contracts/snapshots"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("CONTRACT_NOT_PARSED"));
    }

    @Test
    void parseDelegatesToTheServiceAndReturnsTheResult() throws Exception {
        ContractParseResult result = new ContractParseResult(
                "http://localhost:8080/v3/api-docs", Instant.now(), 1, List.of());
        given(contractParseService.parseConfiguredSource()).willReturn(result);

        mockMvc.perform(post("/api/contracts/parse"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postEndpointCount").value(1));
    }

    @Test
    void snapshotByIdReturnsTheMatchingEndpoint() throws Exception {
        EndpointSnapshot endpoint = new EndpointSnapshot(
                "POST:/api/payments/scheduled", "POST", "/api/payments/scheduled",
                "schedulePayment", "Schedule a future or recurring bill payment",
                List.of("Payments"), List.of(), "ScheduledPaymentRequest", List.of(),
                List.of(201), List.of(400));
        ContractParseResult result = new ContractParseResult(
                "http://localhost:8080/v3/api-docs", Instant.now(), 1, List.of(endpoint));
        given(cache.get()).willReturn(Optional.of(result));

        String encoded = EndpointIdCodec.encode("POST:/api/payments/scheduled");

        mockMvc.perform(get("/api/contracts/snapshots/{id}", encoded))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestSchemaName").value("ScheduledPaymentRequest"));
    }

    @Test
    void snapshotByUnknownIdReturns404() throws Exception {
        ContractParseResult result = new ContractParseResult(
                "http://localhost:8080/v3/api-docs", Instant.now(), 0, List.of());
        given(cache.get()).willReturn(Optional.of(result));

        String encoded = EndpointIdCodec.encode("POST:/api/does-not-exist");

        mockMvc.perform(get("/api/contracts/snapshots/{id}", encoded))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("ENDPOINT_SNAPSHOT_NOT_FOUND"));
    }
}
