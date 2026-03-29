package lk.ijse.automationservice.repo;

import lk.ijse.automationservice.entity.AutomationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AutomationLogRepository extends JpaRepository<AutomationLog, Long> {
    List<AutomationLog> findAllByOrderByTimestampDesc();
    long count();
}
