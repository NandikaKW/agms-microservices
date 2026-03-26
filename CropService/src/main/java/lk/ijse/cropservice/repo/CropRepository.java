package lk.ijse.cropservice.repo;

import lk.ijse.cropservice.entity.CropBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CropRepository extends JpaRepository<CropBatch, Long> {
}
