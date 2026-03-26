package lk.ijse.sensorservice.repo;

import lk.ijse.sensorservice.entity.SensorReading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SensorReadingRepository extends JpaRepository<SensorReading, Long> {
    Optional<SensorReading> findFirstByOrderByCapturedAtDesc();
}
