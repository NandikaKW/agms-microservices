package lk.ijse.sensorservice.service;

import lk.ijse.sensorservice.dto.SensorDTO;
import java.util.List;

public interface SensorService {
    SensorDTO getLatestReading();
    void fetchAndPushData();
    double getAverageTemperature();
    double getAverageHumidity();

    long getTotalReadingsCount();
}
