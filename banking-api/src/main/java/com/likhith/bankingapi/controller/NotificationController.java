package com.likhith.bankingapi.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.likhith.bankingapi.common.ApiResponse;
import com.likhith.bankingapi.dto.request.NotificationPreferencesRequest;
import com.likhith.bankingapi.dto.response.NotificationPreferencesResponse;
import com.likhith.bankingapi.service.NotificationPreferenceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Customer notification preferences")
public class NotificationController {

    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);

    private final NotificationPreferenceService notificationPreferenceService;

    @PostMapping("/preferences")
    @Operation(summary = "Set a customer's notification preferences")
    public ResponseEntity<ApiResponse<NotificationPreferencesResponse>> savePreferences(
            @Valid @RequestBody NotificationPreferencesRequest request) {
        log.info("Received notification preferences request for customer {}", request.getCustomerId());
        NotificationPreferencesResponse response = notificationPreferenceService.savePreferences(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Notification preferences saved successfully", response));
    }
}
