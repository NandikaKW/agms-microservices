package lk.ijse.zoneservice.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/*
    This class is used to communicate with the external IoT API.
    I created this to handle authentication and device-related operations.
*/
@Component
@Slf4j
public class IoTApiClient {

    // Base URL of the external IoT service
    private static final String BASE_URL = "http://104.211.95.241:8080/api";

    // Simple credentials (used for testing purpose)
    private static final String USERNAME = "agms_user";
    private static final String PASSWORD = "123456";

    private final RestTemplate restTemplate = new RestTemplate();

    // Tokens received after login
    private String accessToken;
    private String refreshToken;

    /*
        First step - register user in IoT API
        (If already registered, it will give an error but it's okay)
    */
    public void register() {
        try {
            Map<String, String> payload = new HashMap<>();
            payload.put("username", USERNAME);
            payload.put("password", PASSWORD);

            restTemplate.postForObject(BASE_URL + "/auth/register", payload, Map.class);
            log.info("User registered to IoT API");
        } catch (Exception e) {
            log.warn("User might be already registered: {}", e.getMessage());
        }
    }

    /*
        Second step - login and get tokens
    */
    public void login() {
        try {
            Map<String, String> payload = new HashMap<>();
            payload.put("username", USERNAME);
            payload.put("password", PASSWORD);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(
                    BASE_URL + "/auth/login", payload, Map.class);

            if (response != null) {
                this.accessToken = (String) response.get("accessToken");
                this.refreshToken = (String) response.get("refreshToken");
                log.info("Login success, tokens received");
            }
        } catch (Exception e) {
            log.error("Login failed: {}", e.getMessage());
        }
    }

    /*
        This method is used when access token expires
        It will use refresh token to get a new one
    */
    public void refreshAccessToken() {
        try {
            Map<String, String> payload = new HashMap<>();
            payload.put("refreshToken", this.refreshToken);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(
                    BASE_URL + "/auth/refresh", payload, Map.class);

            if (response != null) {
                this.accessToken = (String) response.get("accessToken");
                log.info("Token refreshed");
            }
        } catch (Exception e) {
            log.error("Refresh failed, trying login again: {}", e.getMessage());
            login(); // fallback
        }
    }

    /**
     * Step 4: Register a device with the External IoT API
     * Returns the deviceId assigned by the IoT platform
     */
    public String registerDevice(String deviceName, String zoneId) {
        ensureAuthenticated();
        try {
            HttpHeaders headers = createAuthHeaders();
            Map<String, String> payload = new HashMap<>();
            payload.put("name", deviceName);
            payload.put("zoneId", zoneId);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(payload, headers);

            @SuppressWarnings("unchecked")
            ResponseEntity<Map> response = restTemplate.exchange(
                    BASE_URL + "/devices", HttpMethod.POST, entity, Map.class);

            if (response.getBody() != null) {
                String deviceId = (String) response.getBody().get("deviceId");
                log.info("Device registered: {} -> deviceId: {}", deviceName, deviceId);
                return deviceId;
            }
        } catch (Exception e) {
            log.error("Failed to register device: {}", e.getMessage());
            // Try refreshing token and retry once
            try {
                refreshAccessToken();
                HttpHeaders headers = createAuthHeaders();
                Map<String, String> payload = new HashMap<>();
                payload.put("name", deviceName);
                payload.put("zoneId", zoneId);
                HttpEntity<Map<String, String>> entity = new HttpEntity<>(payload, headers);
                @SuppressWarnings("unchecked")
                ResponseEntity<Map> response = restTemplate.exchange(
                        BASE_URL + "/devices", HttpMethod.POST, entity, Map.class);
                if (response.getBody() != null) {
                    return (String) response.getBody().get("deviceId");
                }
            } catch (Exception ex) {
                log.error("Retry also failed: {}", ex.getMessage());
            }
        }
        return "device-" + System.currentTimeMillis(); // Fallback
    }

    /**
     * Step 5: Fetch telemetry for a specific device
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> fetchTelemetry(String deviceId) {
        ensureAuthenticated();
        try {
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    BASE_URL + "/devices/telemetry/" + deviceId,
                    HttpMethod.GET, entity, Map.class);

            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to fetch telemetry: {}", e.getMessage());
            // Try refreshing token and retry
            try {
                refreshAccessToken();
                HttpHeaders headers = createAuthHeaders();
                HttpEntity<Void> entity = new HttpEntity<>(headers);
                ResponseEntity<Map> response = restTemplate.exchange(
                        BASE_URL + "/devices/telemetry/" + deviceId,
                        HttpMethod.GET, entity, Map.class);
                return response.getBody();
            } catch (Exception ex) {
                log.error("Retry also failed: {}", ex.getMessage());
            }
        }
        return null;
    }

    private void ensureAuthenticated() {
        if (this.accessToken == null) {
            register();
            login();
        }
    }

    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(this.accessToken);
        return headers;
    }
}
