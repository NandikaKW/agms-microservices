package lk.ijse.automationservice.service;

import lk.ijse.automationservice.client.ZoneClient;
import lk.ijse.automationservice.dto.SensorReadingDTO;
import lk.ijse.automationservice.dto.ZoneResponseDTO;
import lk.ijse.automationservice.entity.AutomationLog;
import lk.ijse.automationservice.repo.AutomationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AutomationServiceImpl implements AutomationService {

    private final AutomationLogRepository repo;
    private final ZoneClient zoneClient;

    @Override
    public void processReading(SensorReadingDTO readingDTO) {
        log.info("Processing sensor reading for Zone ID: {}", readingDTO.getZoneId());
        
        try {
            // Requirement: Synchronous inter-service communication via Feign
            // Usually zoneId in the reading might be something like "Zone-1" or just "1"
            String zoneIdRaw = readingDTO.getZoneId().replaceAll("[^0-9]", "");
            int zoneId = Integer.parseInt(zoneIdRaw.isEmpty() ? "1" : zoneIdRaw);
            
            ZoneResponseDTO zone = zoneClient.getZoneById(zoneId);
            
            String action = "NO_ACTION";
            if (readingDTO.getTemperature() > zone.getMaxTemp()) {
                action = "TURN_FAN_ON";
            } else if (readingDTO.getTemperature() < zone.getMinTemp()) {
                action = "TURN_HEATER_ON";
            }
            
            log.info("Rule decision for Zone {}: {}", zone.getName(), action);
            
            if (!action.equals("NO_ACTION")) {
                AutomationLog automationLog = AutomationLog.builder()
                        .zoneId(readingDTO.getZoneId())
                        .action(action)
                        .temperature(readingDTO.getTemperature())
                        .timestamp(LocalDateTime.now())
                        .build();
                repo.save(automationLog);
            }
            
        } catch (Exception e) {
            log.error("Error processing rule engine in AutomationService: {}", e.getMessage());
        }
    }

    @Override
    public List<AutomationLog> getLogs() {
        return repo.findAllByOrderByTimestampDesc();
    }
    @Override
    public long getLogsCount() {
        return repo.count();
    }
    @Override
    public List<AutomationLog> getLogsByZone(String zoneId) {
        return repo.findByZoneIdOrderByTimestampDesc(zoneId);
    }
}
