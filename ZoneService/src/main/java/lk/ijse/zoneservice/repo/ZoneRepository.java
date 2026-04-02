package lk.ijse.zoneservice.repo;

import lk.ijse.zoneservice.entity.Zone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ZoneRepository extends JpaRepository<Zone, Integer> {
    List<Zone> findByNameContainingIgnoreCase(String name);

    List<Zone> findByMinTempLessThanEqualAndMaxTempGreaterThanEqual(double min, double max);

}
