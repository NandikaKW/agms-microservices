package lk.ijse.cropservice.service;

import lk.ijse.cropservice.dto.CropDTO;
import java.util.List;

public interface CropService {
    CropDTO save(CropDTO dto);
    CropDTO updateStatus(Long id, String status);
    List<CropDTO> getAll();
}
