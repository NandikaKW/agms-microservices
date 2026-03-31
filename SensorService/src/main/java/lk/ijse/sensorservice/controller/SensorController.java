package lk.ijse.sensorservice.controller;

import lk.ijse.sensorservice.dto.SensorDTO;
import lk.ijse.sensorservice.service.SensorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sensors")
@RequiredArgsConstructor
public class SensorController {

    private final SensorService service;

    @GetMapping("/latest")
    public SensorDTO getLatest() {
        return service.getLatestReading();
    }
    //get average temperature
    @GetMapping("/stats/average-temperature")
    public double getAverageTemperature() {
        return service.getAverageTemperature();
    }
    //get average humidity
    @GetMapping("/stats/average-humidity")
    public double getAverageHumidity() {
        return service.getAverageHumidity();
    }

    //get total readings
    @GetMapping("/stats/total-readings")
    public long getTotalReadings() {
        return service.getTotalReadingsCount();
    }

}
