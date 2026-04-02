package lk.ijse.zoneservice.controller;

import lk.ijse.zoneservice.dto.ZoneDTO;
import lk.ijse.zoneservice.service.ZoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
    This controller is used to handle zone-related API requests.
    It connects the frontend with the service layer.
*/
@RestController
@RequestMapping("/api/zones")
@RequiredArgsConstructor
@CrossOrigin
public class ZoneController {

    // Injecting ZoneService
    private final ZoneService service;

    /*
        Save a new zone
    */
    @PostMapping
    public ZoneDTO save(@RequestBody ZoneDTO dto) {
        return service.save(dto);
    }

    /*
        Update zone details using id
    */
    @PutMapping("/{id}")
    public ZoneDTO update(@PathVariable int id, @RequestBody ZoneDTO dto) {
        return service.update(id, dto);
    }

    /*
        Get all zones
    */
    @GetMapping
    public List<ZoneDTO> all() {
        return service.getAll();
    }

    /*
        Get a single zone by id
    */
    @GetMapping("/{id}")
    public ZoneDTO byId(@PathVariable int id) {
        return service.getById(id);
    }

    /*
        Delete a zone using id
    */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id) {
        service.delete(id);
    }

    @GetMapping("/{id}/status")
    public String checkStatus(@PathVariable int id,
                              @RequestParam double temp) {
        return service.checkZoneStatus(id, temp);
    }

    @GetMapping("/search")
    public List<ZoneDTO> search(@RequestParam String name) {
        return service.searchByName(name);
    }
    @GetMapping("/filter")
    public List<ZoneDTO> filter(@RequestParam double temp) {
        return service.filterByTemperature(temp);
    }

    @GetMapping("/devices")
    public List<String> getDevices() {
        return service.getAllDeviceIds();
    }




}
