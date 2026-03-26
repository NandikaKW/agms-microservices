package lk.ijse.cropservice.service;

import lk.ijse.cropservice.dto.CropDTO;
import lk.ijse.cropservice.entity.CropBatch;
import lk.ijse.cropservice.repo.CropRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CropServiceImpl implements CropService {

    private final CropRepository repo;

    // Valid state transitions as per Section 13
    private static final Map<String, String> ALLOWED_TRANSITIONS = Map.of(
            "SEEDLING", "VEGETATIVE",
            "VEGETATIVE", "HARVESTED"
    );

    @Override
    public CropDTO save(CropDTO dto) {
        CropBatch batch = CropBatch.builder()
                .cropName(dto.getCropName())
                .zoneId(dto.getZoneId())
                .status("SEEDLING") // Default status as per requirement
                .quantity(dto.getQuantity())
                .createdDate(LocalDateTime.now())
                .build();
        return toDTO(repo.save(batch));
    }

    @Override
    public CropDTO updateStatus(Long id, String status) {
        CropBatch batch = repo.findById(id).orElseThrow(
                () -> new RuntimeException("Crop batch not found with id: " + id)
        );
        String currentStatus = batch.getStatus();
        String newStatus = status.toUpperCase();

        // Validate state machine transition
        String allowedNext = ALLOWED_TRANSITIONS.get(currentStatus);
        if (allowedNext == null || !allowedNext.equals(newStatus)) {
            throw new RuntimeException(
                    "Invalid transition: " + currentStatus + " → " + newStatus +
                    ". Allowed: " + (allowedNext != null ? currentStatus + " → " + allowedNext : "No further transitions")
            );
        }

        batch.setStatus(newStatus);
        return toDTO(repo.save(batch));
    }

    @Override
    public List<CropDTO> getAll() {
        return repo.findAll().stream().map(this::toDTO).toList();
    }

    private CropDTO toDTO(CropBatch batch) {
        return CropDTO.builder()
                .id(batch.getId())
                .cropName(batch.getCropName())
                .zoneId(batch.getZoneId())
                .status(batch.getStatus())
                .quantity(batch.getQuantity())
                .createdDate(batch.getCreatedDate())
                .build();
    }
}
