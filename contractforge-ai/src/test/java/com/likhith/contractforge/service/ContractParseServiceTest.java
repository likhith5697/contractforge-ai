package com.likhith.contractforge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import com.likhith.contractforge.config.OpenApiSourceProperties;
import com.likhith.contractforge.exception.OpenApiFetchException;
import com.likhith.contractforge.exception.OpenApiParseException;
import com.likhith.contractforge.model.ApiHeaderSnapshot;
import com.likhith.contractforge.model.ContractParseResult;
import com.likhith.contractforge.model.EndpointSnapshot;
import com.likhith.contractforge.model.FieldSnapshot;
import com.likhith.contractforge.parser.OpenApiContractParser;
import com.likhith.contractforge.parser.SchemaFlattener;

/**
 * Drives ContractParseService against the static fixture in
 * src/test/resources, standing in for a real banking-api response so these
 * tests never need the sibling application running.
 */
class ContractParseServiceTest {

    private OpenApiFetcher openApiFetcher;
    private ContractParseService contractParseService;

    @BeforeEach
    void setUp() {
        openApiFetcher = mock(OpenApiFetcher.class);
        OpenApiSourceProperties properties = new OpenApiSourceProperties();
        properties.setSourceUrl("http://localhost:8080/v3/api-docs");

        contractParseService = new ContractParseService(
                properties,
                openApiFetcher,
                new OpenApiContractParser(new SchemaFlattener()),
                new ContractSnapshotCache());
    }

    @Test
    void parsesExactlyOneEndpointFromTheFixture() {
        when(openApiFetcher.fetchRawContract(anyString())).thenReturn(fixtureJson());

        ContractParseResult result = contractParseService.parseConfiguredSource();

        assertThat(result.postEndpointCount()).isEqualTo(1);
        assertThat(result.endpoints()).hasSize(1);
        assertThat(result.endpoints().get(0).endpointId()).isEqualTo("POST:/api/payments/scheduled");
    }

    @Test
    void resolvesTheRequestSchemaNameAndFlattenedPaths() {
        when(openApiFetcher.fetchRawContract(anyString())).thenReturn(fixtureJson());

        EndpointSnapshot endpoint = contractParseService.parseConfiguredSource().endpoints().get(0);

        assertThat(endpoint.requestSchemaName()).isEqualTo("ScheduledPaymentRequest");
        assertThat(endpoint.requestFields()).extracting(FieldSnapshot::jsonPath)
                .contains("amount.currency", "amount.value", "scheduleDetails.frequency");
    }

    @Test
    void recognisesRequiredAndOptionalTopLevelFields() {
        when(openApiFetcher.fetchRawContract(anyString())).thenReturn(fixtureJson());

        EndpointSnapshot endpoint = contractParseService.parseConfiguredSource().endpoints().get(0);

        assertThat(fieldNamed(endpoint, "accountId").required()).isTrue();
        assertThat(fieldNamed(endpoint, "billerCategory").required()).isTrue();
        assertThat(fieldNamed(endpoint, "notifyBeforeDays").required()).isFalse();
    }

    @Test
    void extractsEnumValuesForBillerCategoryAndFrequency() {
        when(openApiFetcher.fetchRawContract(anyString())).thenReturn(fixtureJson());

        EndpointSnapshot endpoint = contractParseService.parseConfiguredSource().endpoints().get(0);

        assertThat(fieldNamed(endpoint, "billerCategory").enumValues())
                .containsExactly("CREDIT_CARD", "ELECTRICITY", "INSURANCE", "INTERNET", "MOBILE_POSTPAID", "WATER");
        assertThat(fieldNamed(endpoint, "scheduleDetails.frequency").enumValues())
                .containsExactly("MONTHLY", "ONE_TIME", "QUARTERLY", "WEEKLY");
    }

    @Test
    void extractsMinAndMaxForNotifyBeforeDays() {
        when(openApiFetcher.fetchRawContract(anyString())).thenReturn(fixtureJson());

        FieldSnapshot notifyBeforeDays = fieldNamed(
                contractParseService.parseConfiguredSource().endpoints().get(0), "notifyBeforeDays");

        assertThat(notifyBeforeDays.minimum()).isEqualByComparingTo("0");
        assertThat(notifyBeforeDays.maximum()).isEqualByComparingTo("30");
    }

    @Test
    void onlyTheRequiredHeaderIsReturned() {
        when(openApiFetcher.fetchRawContract(anyString())).thenReturn(fixtureJson());

        EndpointSnapshot endpoint = contractParseService.parseConfiguredSource().endpoints().get(0);

        assertThat(endpoint.requiredHeaders()).extracting(ApiHeaderSnapshot::name).containsExactly("X-Client-Id");
        assertThat(endpoint.requiredHeaders()).allMatch(ApiHeaderSnapshot::required);
    }

    @Test
    void invalidSourceContentIsReportedAsAParseFailure() {
        when(openApiFetcher.fetchRawContract(anyString())).thenReturn("this is not an OpenAPI document");

        assertThatThrownBy(() -> contractParseService.parseConfiguredSource())
                .isInstanceOf(OpenApiParseException.class);
    }

    @Test
    void unreachableSourcePropagatesAsAFetchFailure() {
        when(openApiFetcher.fetchRawContract(anyString()))
                .thenThrow(new OpenApiFetchException("Could not reach OpenAPI source"));

        assertThatThrownBy(() -> contractParseService.parseConfiguredSource())
                .isInstanceOf(OpenApiFetchException.class);
    }

    private FieldSnapshot fieldNamed(EndpointSnapshot endpoint, String jsonPath) {
        return endpoint.requestFields().stream()
                .filter(f -> f.jsonPath().equals(jsonPath))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No field at path " + jsonPath));
    }

    private String fixtureJson() {
        try {
            Path path = new ClassPathResource("scheduled-payment-openapi-fixture.json").getFile().toPath();
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
