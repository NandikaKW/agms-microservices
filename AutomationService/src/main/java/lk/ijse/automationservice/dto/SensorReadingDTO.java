package lk.ijse.automationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SensorReadingDTO {
    private String deviceId;
    private String zoneId;
    private double temperature;
    private double humidity;
    private LocalDateTime capturedAt;
}
