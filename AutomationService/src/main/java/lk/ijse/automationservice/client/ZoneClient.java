package lk.ijse.automationservice.client;

import lk.ijse.automationservice.dto.ZoneResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "zone-service")
public interface ZoneClient {
    @GetMapping("/api/zones/{id}")
    ZoneResponseDTO getZoneById(@PathVariable("id") int id);
}
