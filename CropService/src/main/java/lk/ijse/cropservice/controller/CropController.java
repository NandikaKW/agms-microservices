package lk.ijse.cropservice.controller;

import lk.ijse.cropservice.dto.CropDTO;
import lk.ijse.cropservice.service.CropService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/crops")
@RequiredArgsConstructor
public class CropController {

    private final CropService service;

    @PostMapping
    public CropDTO save(@RequestBody CropDTO dto) {
        return service.save(dto);
    }

    @PutMapping("/{id}/status")
    public CropDTO updateStatus(@PathVariable Long id, @RequestParam String status) {
        return service.updateStatus(id, status);
    }

    @GetMapping
    public List<CropDTO> all() {
        return service.getAll();
    }
}
