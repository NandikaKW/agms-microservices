package lk.ijse.cropservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CropDTO {
    private Long id;
    private String cropName;
    private String zoneId;
    private String status;
    private int quantity;
    private LocalDateTime createdDate;
}
