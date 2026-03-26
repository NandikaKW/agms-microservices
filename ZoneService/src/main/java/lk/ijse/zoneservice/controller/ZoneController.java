package lk.ijse.zoneservice.controller;

import lk.ijse.zoneservice.dto.ZoneDTO;
import lk.ijse.zoneservice.service.ZoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/zones")
@RequiredArgsConstructor
@CrossOrigin
public class ZoneController {

    private final ZoneService service;

    @PostMapping
    public ZoneDTO save(@RequestBody ZoneDTO dto) {
        return service.save(dto);
    }

    @PutMapping("/{id}")
    public ZoneDTO update(@PathVariable int id, @RequestBody ZoneDTO dto) {
        return service.update(id, dto);
    }

    @GetMapping
    public List<ZoneDTO> all() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public ZoneDTO byId(@PathVariable int id) {
        return service.getById(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id) {
        service.delete(id);
    }
}
