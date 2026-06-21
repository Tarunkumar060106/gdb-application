package com.gdb.account.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
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

    /**
     * Verifies an Aadhar number by calling the external Aadhar Service.
     *
     * @param aadharNumber the 12-digit Aadhar number to verify
     * @return true if the Aadhar number is valid, false otherwise
     */
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

        } catch (Exception e) {
            // TODO: MOD11-CR-01: Resilience4j Retry & Fallback.
            // Trainee task: Introduce a retry configuration on this client call.
            // Add Resilience4j dependency to pom.xml and use @Retry with a fallback method.
            // 
            // TODO: MOD11-BUG-01: Swallowed client validation error.
            // Trainee task: When the Aadhar service rejects an Aadhar number due to formatting errors,
            // it throws a 400 Bad Request exception, which hits this generic catch block and shows
            // "Aadhar verification service is unavailable".
            // Add a specific catch block for HttpClientErrorException to parse the response body 
            // and throw the specific validation error message instead of the generic outage message.
            log.error("Error calling Aadhar Service: {}", e.getMessage());
            throw new RuntimeException("Aadhar verification service is unavailable. Please try again later.");
        }
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
