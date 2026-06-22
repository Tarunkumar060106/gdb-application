package com.gdb.account.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Client for communicating with the Aadhar Verification Service (port 8005).
 * Called during savings account creation to validate the Aadhar number.
 */
@Component
@Slf4j
public class AadharClient {

    private final RestTemplate restTemplate;
    private final String aadharServiceUrl;

    public AadharClient(RestTemplate restTemplate,
            @Value("${external.aadhar-service.url}") String aadharServiceUrl) {
        this.restTemplate = restTemplate;
        this.aadharServiceUrl = aadharServiceUrl;
    }

    /**
     * Response DTO for the Aadhar verification API.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AadharVerificationResponse {
        @JsonProperty("aadhar_number")
        private String aadharNumber;

        @JsonProperty("is_valid")
        private boolean isValid;

        private String status;
        private String message;
        private String timestamp;
    }

    @Retry(name = "aadharClient", fallbackMethod = "verifyAadharFallback")
    public boolean verifyAadhar(String aadharNumber) {
        String url = aadharServiceUrl + "/api/v1/verify";
        log.info("Calling Aadhar Service at {} for verification", url);

        try {
            Map<String, String> requestBody = Map.of("aadhar_number", aadharNumber);
            AadharVerificationResponse response = restTemplate.postForObject(
                    url, requestBody, AadharVerificationResponse.class);

            if (response != null) {
                log.info("Aadhar verification result for {}: {} - {}",
                        maskAadhar(aadharNumber), response.getStatus(), response.getMessage());
                return response.isValid();
            }

            log.warn("Aadhar Service returned null response");
            return false;

        } catch (HttpClientErrorException e) {
            // MOD11-BUG-01: Propagate client-side (4xx) errors as validation errors instead of
            // swallowing them as a generic outage message.
            log.warn("Aadhar Service rejected request ({}): {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new com.gdb.account.exception.AccountException(
                    "Aadhar validation error: " + e.getResponseBodyAsString(), "AADHAR_VALIDATION_ERROR");
        } catch (Exception e) {
            log.error("Error calling Aadhar Service: {}", e.getMessage());
            throw new RuntimeException("Aadhar verification service is unavailable. Please try again later.");
        }
    }

    public boolean verifyAadharFallback(String aadharNumber, Exception e) {
        log.error("Aadhar service unreachable after retries for {}: {}", maskAadhar(aadharNumber), e.getMessage());
        throw new RuntimeException("Aadhar verification service is unavailable after multiple retries. Please try again later.");
    }

    /**
     * Masks an Aadhar number for logging (showing only first 4 digits).
     */
    private String maskAadhar(String aadharNumber) {
        if (aadharNumber == null || aadharNumber.length() < 4) {
            return "XXXXXXXXXXXX";
        }
        return aadharNumber.substring(0, 4) + "XXXXXXXX";
    }
}
