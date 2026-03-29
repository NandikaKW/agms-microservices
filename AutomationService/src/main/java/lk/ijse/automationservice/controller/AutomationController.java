package lk.ijse.automationservice.controller;

import lk.ijse.automationservice.dto.SensorReadingDTO;
import lk.ijse.automationservice.entity.AutomationLog;
import lk.ijse.automationservice.service.AutomationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/automation")
@RequiredArgsConstructor
public class AutomationController {
    private final AutomationService service;

    @PostMapping("/process")
    public void process(@RequestBody SensorReadingDTO dto) {
        service.processReading(dto);
    }

    @GetMapping("/logs")
    public List<AutomationLog> logs() {
        return service.getLogs();
    }
    @GetMapping("/logs/count")
    public long logsCount() {
        return service.getLogsCount();
    }
    @GetMapping("/logs/zone/{zoneId}")
    public List<AutomationLog> logsByZone(@PathVariable String zoneId) {
        return service.getLogsByZone(zoneId);
    }

    @GetMapping("/health")
    public String health() {
        return service.healthCheck();
    }
    @GetMapping("/logs/recent")
    public List<AutomationLog> recentLogs(@RequestParam int minutes) {
        return service.getRecentLogs(minutes);
    }


}
