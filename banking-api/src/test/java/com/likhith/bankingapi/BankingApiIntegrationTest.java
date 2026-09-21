package com.likhith.bankingapi;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

/**
 * End-to-end MockMvc tests covering the golden path and key validation/business-rule
 * failures across the mock banking API, per the ContractForge AI test-automation brief.
 */
@SpringBootTest
@AutoConfigureMockMvc
class BankingApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String createCustomerAndGetId(String email) throws Exception {
        Map<String, Object> request = validCustomerRequest(email);
        String body = mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.data.customerId");
    }

    private String openAccountAndGetId(String customerId) throws Exception {
        Map<String, Object> request = Map.of(
                "customerId", customerId,
                "accountType", "SAVINGS",
                "currency", "USD",
                "initialDeposit", 5000.00,
                "branchCode", "BR-0451",
                "purpose", "PERSONAL",
                "nomineeDetails", Map.of(
                        "nomineeName", "Priya Sharma",
                        "relationship", "SPOUSE",
                        "sharePercentage", 100),
                "channelMetadata", Map.of(
                        "channel", "WEB",
                        "deviceId", "DEV-9F1A2B3C",
                        "ipAddress", "203.0.113.42"));

        String body = mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.data.accountId");
    }

    private Map<String, Object> validCustomerRequest(String email) {
        return Map.of(
                "personalInfo", Map.of(
                        "firstName", "Aarav",
                        "lastName", "Sharma",
                        "dateOfBirth", "1990-05-14",
                        "gender", "MALE",
                        "nationality", "IN"),
                "contactInfo", Map.of(
                        "email", email,
                        "phoneNumber", "+14155552671"),
                "identityDocument", Map.of(
                        "documentType", "PASSPORT",
                        "documentNumber", "SYN-DOC-8842190",
                        "issuingCountry", "IN",
                        "expiryDate", "2030-01-01"),
                "employmentInfo", Map.of(
                        "occupation", "Software Engineer",
                        "employmentType", "SALARIED",
                        "annualIncome", 95000.00),
                "residentialAddress", Map.of(
                        "addressLine1", "221B Synthetic Lane",
                        "city", "Springfield",
                        "postalCode", "62704",
                        "country", "US"),
                "riskProfile", Map.of(
                        "sourceOfFunds", "SALARY",
                        "expectedMonthlyTurnover", 5000.00));
    }

    @Test
    void createCustomer_validRequest_returns201() throws Exception {
        Map<String, Object> request = validCustomerRequest("aarav." + System.nanoTime() + "@example-mail.com");

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.data.customerId").exists())
                .andExpect(jsonPath("$.requestId").exists());
    }

    @Test
    void createCustomer_invalidEmail_returns400WithFieldError() throws Exception {
        Map<String, Object> request = validCustomerRequest("not-a-valid-email");

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'contactInfo.email')]").exists());
    }

    @Test
    void openAccount_forExistingCustomer_returns201() throws Exception {
        String customerId = createCustomerAndGetId("account.holder." + System.nanoTime() + "@example-mail.com");

        Map<String, Object> request = Map.of(
                "customerId", customerId,
                "accountType", "SAVINGS",
                "currency", "USD",
                "initialDeposit", 1000.00,
                "branchCode", "BR-0451",
                "purpose", "PERSONAL",
                "nomineeDetails", Map.of(
                        "nomineeName", "Priya Sharma",
                        "relationship", "SPOUSE",
                        "sharePercentage", 100),
                "channelMetadata", Map.of(
                        "channel", "WEB",
                        "deviceId", "DEV-9F1A2B3C",
                        "ipAddress", "203.0.113.42"));

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.accountId").exists())
                .andExpect(jsonPath("$.data.customerId").value(customerId));
    }

    @Test
    void domesticTransfer_negativeAmount_returns400() throws Exception {
        String customerId = createCustomerAndGetId("neg.amount." + System.nanoTime() + "@example-mail.com");
        String sourceAccountId = openAccountAndGetId(customerId);
        String destinationAccountId = openAccountAndGetId(customerId);

        Map<String, Object> request = Map.of(
                "sourceAccountId", sourceAccountId,
                "destinationAccountId", destinationAccountId,
                "amount", Map.of("amount", -100.00, "currency", "USD"),
                "transferType", "IMPS",
                "purposeCode", "OTHER",
                "channelMetadata", Map.of(
                        "channel", "MOBILE",
                        "deviceId", "DEV-1122AABB",
                        "ipAddress", "203.0.113.50"));

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"));
    }

    @Test
    void domesticTransfer_sameSourceAndDestination_returns400() throws Exception {
        String customerId = createCustomerAndGetId("same.acct." + System.nanoTime() + "@example-mail.com");
        String accountId = openAccountAndGetId(customerId);

        Map<String, Object> request = Map.of(
                "sourceAccountId", accountId,
                "destinationAccountId", accountId,
                "amount", Map.of("amount", 100.00, "currency", "USD"),
                "transferType", "IMPS",
                "purposeCode", "OTHER",
                "channelMetadata", Map.of(
                        "channel", "MOBILE",
                        "deviceId", "DEV-1122AABB",
                        "ipAddress", "203.0.113.50"));

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("SAME_ACCOUNT_TRANSFER"));
    }

    @Test
    void domesticTransfer_duplicateIdempotencyKey_returns409() throws Exception {
        String customerId = createCustomerAndGetId("idem." + System.nanoTime() + "@example-mail.com");
        String sourceAccountId = openAccountAndGetId(customerId);
        String destinationAccountId = openAccountAndGetId(customerId);
        String idempotencyKey = "IDEM-" + System.nanoTime();

        Map<String, Object> request = Map.of(
                "sourceAccountId", sourceAccountId,
                "destinationAccountId", destinationAccountId,
                "amount", Map.of("amount", 50.00, "currency", "USD"),
                "transferType", "IMPS",
                "purposeCode", "OTHER",
                "channelMetadata", Map.of(
                        "channel", "MOBILE",
                        "deviceId", "DEV-1122AABB",
                        "ipAddress", "203.0.113.50"));

        String jsonBody = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Idempotency-Key", idempotencyKey)
                        .content(jsonBody))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Idempotency-Key", idempotencyKey)
                        .content(jsonBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("DUPLICATE_IDEMPOTENCY_KEY"));
    }

    @Test
    void scheduledPayment_pastStartDate_returns400() throws Exception {
        String customerId = createCustomerAndGetId("scheduled." + System.nanoTime() + "@example-mail.com");
        String accountId = openAccountAndGetId(customerId);

        Map<String, Object> request = Map.of(
                "accountId", accountId,
                "billerId", "BLR-4471",
                "billerCategory", "ELECTRICITY",
                "consumerNumber", "CONS-99001122",
                "amount", Map.of("amount", 75.00, "currency", "USD"),
                "scheduleDetails", Map.of(
                        "startDate", LocalDate.now().minusDays(5).toString(),
                        "frequency", "MONTHLY"),
                "autoPayEnabled", true,
                "notifyBeforeDays", 3);

        mockMvc.perform(post("/api/payments/scheduled")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"));
    }
}
