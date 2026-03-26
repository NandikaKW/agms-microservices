package lk.ijse.automationservice.service;

import lk.ijse.automationservice.dto.SensorReadingDTO;
import lk.ijse.automationservice.entity.AutomationLog;
import java.util.List;

public interface AutomationService {
    void processReading(SensorReadingDTO readingDTO);
    List<AutomationLog> getLogs();
}
