package lk.ijse.zoneservice.repo;

import lk.ijse.zoneservice.entity.Zone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ZoneRepository extends JpaRepository<Zone, Integer> {
}
