package lk.ijse.zoneservice.service;

import lk.ijse.zoneservice.client.IoTApiClient;
import lk.ijse.zoneservice.dto.ZoneDTO;
import lk.ijse.zoneservice.entity.Zone;
import lk.ijse.zoneservice.repo.ZoneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ZoneServiceImpl implements ZoneService {

    private final ZoneRepository repo;
    private final IoTApiClient ioTApiClient;

    @Override
    public ZoneDTO save(ZoneDTO dto) {
        // Business Rule: minTemp must be strictly less than maxTemp
        if (dto.getMinTemp() >= dto.getMaxTemp()) {
            throw new RuntimeException("minTemp must be strictly less than maxTemp");
        }

        // External Integration: Register Device with IoT API
        // Step 1: Call IoT API POST /devices
        // Step 2: Store the returned deviceId
        String deviceId;
        try {
            deviceId = ioTApiClient.registerDevice(
                    dto.getName() + "-Sensor",
                    "Zone-" + dto.getName()
            );
            log.info("Device registered with IoT API. DeviceId: {}", deviceId);
        } catch (Exception e) {
            log.error("Failed to register device with IoT API: {}", e.getMessage());
            deviceId = "fallback-" + System.currentTimeMillis();
        }

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
        Zone zone = repo.findById(id).orElseThrow(
                () -> new RuntimeException("Zone not found with id: " + id)
        );
        if (dto.getMinTemp() >= dto.getMaxTemp()) {
            throw new RuntimeException("minTemp must be strictly less than maxTemp");
        }
        zone.setName(dto.getName());
        zone.setMinTemp(dto.getMinTemp());
        zone.setMaxTemp(dto.getMaxTemp());
        return toDTO(repo.save(zone));
    }

    @Override
    public List<ZoneDTO> getAll() {
        return repo.findAll().stream().map(this::toDTO).toList();
    }

    @Override
    public ZoneDTO getById(int id) {
        return toDTO(repo.findById(id).orElseThrow(
                () -> new RuntimeException("Zone not found with id: " + id)
        ));
    }

    @Override
    public void delete(int id) {
        repo.deleteById(id);
    }

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
