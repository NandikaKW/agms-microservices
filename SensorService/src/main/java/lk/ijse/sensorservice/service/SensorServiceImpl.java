package lk.ijse.sensorservice.service;

import lk.ijse.sensorservice.dto.SensorDTO;
import lk.ijse.sensorservice.entity.SensorReading;
import lk.ijse.sensorservice.repo.SensorReadingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class SensorServiceImpl implements SensorService {

    private static final String IOT_BASE_URL = "http://104.211.95.241:8080/api";
    private static final String IOT_USERNAME = "agms_sensor_user";
    private static final String IOT_PASSWORD = "123456";

    private final SensorReadingRepository repo;
    private final RestTemplate restTemplate;
    private final RestTemplate plainRestTemplate = new RestTemplate(); // Non-LB for external API

    private String accessToken;
    private String refreshToken;

    @Autowired
    public SensorServiceImpl(SensorReadingRepository repo, RestTemplate restTemplate) {
        this.repo = repo;
        this.restTemplate = restTemplate;
    }

    @Override
    public SensorDTO getLatestReading() {
        return repo.findFirstByOrderByCapturedAtDesc()
                .map(this::toDTO)
                .orElse(null);
    }

    @Override
    @Scheduled(fixedRate = 10000) // Every 10 seconds as per assignment
    public void fetchAndPushData() {
        log.info("=== Sensor Fetcher: Running scheduled IoT data fetch ===");
        try {
            // Step 1: Ensure we have a valid token for the External IoT API
            ensureAuthenticated();

            // Step 2: Fetch all registered devices from External IoT API
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<List> devicesResponse = plainRestTemplate.exchange(
                    IOT_BASE_URL + "/devices", HttpMethod.GET, entity, List.class);

            List<Map<String, Object>> devices = devicesResponse.getBody();
            if (devices == null || devices.isEmpty()) {
                log.warn("No devices found on IoT platform. Using simulated data.");
                fetchSimulatedAndPush();
                return;
            }

            // Step 3: For each device, fetch live telemetry
            for (Map<String, Object> device : devices) {
                String deviceId = (String) device.get("deviceId");
                String zoneId = (String) device.get("zoneId");

                try {
                    ResponseEntity<Map> telemetryResponse = plainRestTemplate.exchange(
                            IOT_BASE_URL + "/devices/telemetry/" + deviceId,
                            HttpMethod.GET, entity, Map.class);

                    Map<String, Object> telemetry = telemetryResponse.getBody();
                    if (telemetry != null && telemetry.get("value") != null) {
                        Map<String, Object> value = (Map<String, Object>) telemetry.get("value");
                        double temperature = ((Number) value.get("temperature")).doubleValue();
                        double humidity = ((Number) value.get("humidity")).doubleValue();

                        // Save to local database
                        SensorReading reading = SensorReading.builder()
                                .deviceId(deviceId)
                                .zoneId(zoneId)
                                .temperature(temperature)
                                .humidity(humidity)
                                .capturedAt(LocalDateTime.now())
                                .build();
                        repo.save(reading);

                        log.info("LIVE Telemetry saved: Device={}, Zone={}, Temp={}, Hum={}",
                                deviceId, zoneId,
                                String.format("%.2f", temperature),
                                String.format("%.2f", humidity));

                        // Step 4: Push to Automation Service (The Pusher)
                        pushToAutomation(toDTO(reading));
                    }
                } catch (Exception e) {
                    log.warn("Failed to fetch telemetry for device {}: {}", deviceId, e.getMessage());
                }
            }

        } catch (Exception e) {
            log.warn("External IoT API unreachable, falling back to simulation: {}", e.getMessage());
            fetchSimulatedAndPush();
        }
    }

    /**
     * Fallback: Generate simulated data when External IoT API is unreachable
     */
    private void fetchSimulatedAndPush() {
        double simulatedTemp = 20 + Math.random() * 15; // 20 - 35
        double simulatedHum = 40 + Math.random() * 30;  // 40 - 70

        SensorReading reading = SensorReading.builder()
                .deviceId("simulated-device")
                .zoneId("Zone-A")
                .temperature(simulatedTemp)
                .humidity(simulatedHum)
                .capturedAt(LocalDateTime.now())
                .build();
        repo.save(reading);
        log.info("SIMULATED Telemetry: Temp={}, Hum={}",
                String.format("%.2f", simulatedTemp),
                String.format("%.2f", simulatedHum));
        pushToAutomation(toDTO(reading));
    }

    /**
     * Push sensor data to Automation Service via load-balanced RestTemplate
     */
    private void pushToAutomation(SensorDTO dto) {
        try {
            restTemplate.postForObject(
                    "http://automation-service/api/automation/process", dto, Void.class);
            log.info("Data pushed to Automation Service successfully.");
        } catch (Exception e) {
            log.warn("Could not push to Automation Service: {}", e.getMessage());
        }
    }

    // ========== External IoT API Authentication ==========

    private void ensureAuthenticated() {
        if (this.accessToken == null) {
            iotRegister();
            iotLogin();
        }
    }

    private void iotRegister() {
        try {
            Map<String, String> payload = new HashMap<>();
            payload.put("username", IOT_USERNAME);
            payload.put("password", IOT_PASSWORD);
            plainRestTemplate.postForObject(IOT_BASE_URL + "/auth/register", payload, Map.class);
            log.info("Registered with External IoT API");
        } catch (Exception e) {
            log.warn("IoT registration (may already exist): {}", e.getMessage());
        }
    }

    private void iotLogin() {
        try {
            Map<String, String> payload = new HashMap<>();
            payload.put("username", IOT_USERNAME);
            payload.put("password", IOT_PASSWORD);
            Map<String, Object> response = plainRestTemplate.postForObject(
                    IOT_BASE_URL + "/auth/login", payload, Map.class);
            if (response != null) {
                this.accessToken = (String) response.get("accessToken");
                this.refreshToken = (String) response.get("refreshToken");
                log.info("Logged in to External IoT API. Token obtained.");
            }
        } catch (Exception e) {
            log.error("Failed to login to IoT API: {}", e.getMessage());
        }
    }

    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(this.accessToken);
        return headers;
    }

    private SensorDTO toDTO(SensorReading reading) {
        return SensorDTO.builder()
                .deviceId(reading.getDeviceId())
                .zoneId(reading.getZoneId())
                .temperature(reading.getTemperature())
                .humidity(reading.getHumidity())
                .capturedAt(reading.getCapturedAt())
                .build();
    }
    @Override
    public double getAverageTemperature() {
        return repo.findAll()
                .stream()
                .mapToDouble(SensorReading::getTemperature)
                .average()
                .orElse(0.0);  // Return 0 if no readings
    }
}
