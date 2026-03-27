package lk.ijse.zoneservice.service;

import lk.ijse.zoneservice.client.IoTApiClient;
import lk.ijse.zoneservice.dto.ZoneDTO;
import lk.ijse.zoneservice.entity.Zone;
import lk.ijse.zoneservice.repo.ZoneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/*
    This class contains the business logic related to Zone.
    It also connects with the IoT API when creating a zone.
*/
@Service
@RequiredArgsConstructor
@Slf4j
public class ZoneServiceImpl implements ZoneService {

    private final ZoneRepository repo;
    private final IoTApiClient ioTApiClient;

    @Override
    public ZoneDTO save(ZoneDTO dto) {

        // Simple validation - minTemp should be less than maxTemp
        if (dto.getMinTemp() >= dto.getMaxTemp()) {
            throw new RuntimeException("minTemp must be strictly less than maxTemp");
        }

        /*
            When creating a zone, I also register a device in IoT system.
            That API returns a deviceId which I store in my database.
        */
        String deviceId;
        try {
            deviceId = ioTApiClient.registerDevice(
                    dto.getName() + "-Sensor",
                    "Zone-" + dto.getName()
            );
            log.info("Device created in IoT system: {}", deviceId);

        } catch (Exception e) {
            // If IoT API fails, I still continue using a temporary id
            log.error("IoT device creation failed: {}", e.getMessage());
            deviceId = "fallback-" + System.currentTimeMillis();
        }

        // Convert DTO -> Entity
        Zone zone = Zone.builder()
                .name(dto.getName())
                .minTemp(dto.getMinTemp())
                .maxTemp(dto.getMaxTemp())
                .deviceId(deviceId)
                .build();

        return toDTO(repo.save(zone));
    }

    @Override
    public ZoneDTO update(int id, ZoneDTO dto) {

        // Find existing zone
        Zone zone = repo.findById(id).orElseThrow(
                () -> new RuntimeException("Zone not found with id: " + id)
        );

        // Validation again
        if (dto.getMinTemp() >= dto.getMaxTemp()) {
            throw new RuntimeException("minTemp must be strictly less than maxTemp");
        }

        // Update values
        zone.setName(dto.getName());
        zone.setMinTemp(dto.getMinTemp());
        zone.setMaxTemp(dto.getMaxTemp());

        return toDTO(repo.save(zone));
    }

    @Override
    public List<ZoneDTO> getAll() {
        // Convert all entities to DTO list
        return repo.findAll().stream().map(this::toDTO).toList();
    }

    @Override
    public ZoneDTO getById(int id) {
        // Get one zone by id
        return toDTO(repo.findById(id).orElseThrow(
                () -> new RuntimeException("Zone not found with id: " + id)
        ));
    }

    @Override
    public void delete(int id) {
        // Delete zone by id
        repo.deleteById(id);
    }

    /*
        Helper method to convert Entity -> DTO
    */
    private ZoneDTO toDTO(Zone zone) {
        return ZoneDTO.builder()
                .id(zone.getId())
                .name(zone.getName())
                .minTemp(zone.getMinTemp())
                .maxTemp(zone.getMaxTemp())
                .deviceId(zone.getDeviceId())
                .build();
    }
}
