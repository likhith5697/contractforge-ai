package com.likhith.contractforge.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.likhith.contractforge.model.FieldSnapshot;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;

/**
 * Exercises SchemaFlattener directly against hand-built swagger-core models,
 * so cycle detection and array handling can be tested without going through a
 * full JSON document.
 */
class SchemaFlattenerTest {

    private final SchemaFlattener flattener = new SchemaFlattener();

    @Test
    void flattensNestedObjectsIntoDotSeparatedPaths() {
        Schema<Object> address = new Schema<>();
        address.setType("object");
        address.setRequired(List.of("city"));
        address.setProperties(Map.of(
                "city", new Schema<>().type("string"),
                "postalCode", new Schema<>().type("string")));

        Schema<Object> customer = new Schema<>();
        customer.setType("object");
        customer.setRequired(List.of("name"));
        customer.setProperties(Map.of(
                "name", new Schema<>().type("string"),
                "address", refTo("Address")));

        OpenAPI openApi = openApiWithSchemas(Map.of("Address", address, "Customer", customer));

        List<FieldSnapshot> fields = flattener.flatten(refTo("Customer"), openApi);

        assertThat(fields).extracting(FieldSnapshot::jsonPath)
                .containsExactly("address.city", "address.postalCode", "name");
        // "address" itself (a pure object container) must not appear as its own field.
        assertThat(fields).extracting(FieldSnapshot::jsonPath).doesNotContain("address");
    }

    @Test
    void stopsAtCircularReferencesInsteadOfLoopingForever() {
        // Customer -> primaryAddress -> Address -> residentOf -> Customer (cycle)
        Schema<Object> customer = new Schema<>();
        customer.setType("object");
        customer.setProperties(Map.of(
                "name", new Schema<>().type("string"),
                "primaryAddress", refTo("Address")));

        Schema<Object> address = new Schema<>();
        address.setType("object");
        address.setProperties(Map.of(
                "city", new Schema<>().type("string"),
                "residentOf", refTo("Customer")));

        OpenAPI openApi = openApiWithSchemas(Map.of("Address", address, "Customer", customer));

        List<FieldSnapshot> fields = assertTimeoutPreemptively(Duration.ofSeconds(2),
                () -> flattener.flatten(refTo("Customer"), openApi));

        assertThat(fields).extracting(FieldSnapshot::jsonPath)
                .containsExactly("name", "primaryAddress.city");
        // The cycle back into Customer must not have produced a "primaryAddress.residentOf.*" branch.
        assertThat(fields).extracting(FieldSnapshot::jsonPath)
                .noneMatch(path -> path.startsWith("primaryAddress.residentOf"));
    }

    @Test
    void sameSchemaReusedInSiblingBranchesIsNotTreatedAsACycle() {
        Schema<Object> money = new Schema<>();
        money.setType("object");
        money.setRequired(List.of("amount", "currency"));
        money.setProperties(Map.of(
                "amount", new Schema<>().type("number"),
                "currency", new Schema<>().type("string")));

        Schema<Object> payment = new Schema<>();
        payment.setType("object");
        payment.setProperties(Map.of(
                "price", refTo("Money"),
                "tip", refTo("Money")));

        OpenAPI openApi = openApiWithSchemas(Map.of("Money", money, "Payment", payment));

        List<FieldSnapshot> fields = flattener.flatten(refTo("Payment"), openApi);

        assertThat(fields).extracting(FieldSnapshot::jsonPath)
                .containsExactly("price.amount", "price.currency", "tip.amount", "tip.currency");
    }

    @Test
    void flattensArrayOfObjectsWithBracketNotation() {
        Schema<Object> bankDetails = new Schema<>();
        bankDetails.setType("object");
        bankDetails.setRequired(List.of("routingNumber"));
        bankDetails.setProperties(Map.of("routingNumber", new Schema<>().type("string")));

        Schema<Object> beneficiary = new Schema<>();
        beneficiary.setType("object");
        beneficiary.setProperties(Map.of(
                "name", new Schema<>().type("string"),
                "bankDetails", refTo("BankDetails")));

        ArraySchema beneficiariesArray = new ArraySchema();
        beneficiariesArray.setItems(refTo("Beneficiary"));

        Schema<Object> request = new Schema<>();
        request.setType("object");
        request.setProperties(Map.of("beneficiaries", beneficiariesArray));

        OpenAPI openApi = openApiWithSchemas(Map.of(
                "BankDetails", bankDetails,
                "Beneficiary", beneficiary,
                "Request", request));

        List<FieldSnapshot> fields = flattener.flatten(refTo("Request"), openApi);

        assertThat(fields).extracting(FieldSnapshot::jsonPath)
                .containsExactly("beneficiaries", "beneficiaries[].bankDetails.routingNumber", "beneficiaries[].name");

        FieldSnapshot arrayField = fields.stream()
                .filter(f -> f.jsonPath().equals("beneficiaries"))
                .findFirst().orElseThrow();
        assertThat(arrayField.isArray()).isTrue();
        assertThat(arrayField.arrayItemType()).isEqualTo("object");
    }

    @Test
    void extractsEnumMinMaxAndRequiredFlags() {
        Schema<Object> status = new Schema<>();
        status.setType("string");
        status.setEnum(List.of("ACTIVE", "CLOSED"));

        Schema<Object> notifyDays = new Schema<>();
        notifyDays.setType("integer");
        notifyDays.setMinimum(new java.math.BigDecimal("0"));
        notifyDays.setMaximum(new java.math.BigDecimal("30"));

        Schema<Object> root = new Schema<>();
        root.setType("object");
        root.setRequired(List.of("status"));
        root.setProperties(Map.of("status", status, "notifyBeforeDays", notifyDays));

        OpenAPI openApi = openApiWithSchemas(Map.of("Root", root));

        List<FieldSnapshot> fields = flattener.flatten(refTo("Root"), openApi);

        FieldSnapshot statusField = fieldAt(fields, "status");
        assertThat(statusField.required()).isTrue();
        assertThat(statusField.enumValues()).containsExactly("ACTIVE", "CLOSED");

        FieldSnapshot notifyField = fieldAt(fields, "notifyBeforeDays");
        assertThat(notifyField.required()).isFalse();
        assertThat(notifyField.minimum()).isEqualByComparingTo("0");
        assertThat(notifyField.maximum()).isEqualByComparingTo("30");
    }

    private FieldSnapshot fieldAt(List<FieldSnapshot> fields, String jsonPath) {
        return fields.stream().filter(f -> f.jsonPath().equals(jsonPath)).findFirst()
                .orElseThrow(() -> new AssertionError("No field at path " + jsonPath));
    }

    private Schema<Object> refTo(String schemaName) {
        return new Schema<>().$ref("#/components/schemas/" + schemaName);
    }

    private OpenAPI openApiWithSchemas(Map<String, Schema> schemas) {
        Components components = new Components();
        schemas.forEach(components::addSchemas);
        OpenAPI openApi = new OpenAPI();
        openApi.setComponents(components);
        return openApi;
    }
}
