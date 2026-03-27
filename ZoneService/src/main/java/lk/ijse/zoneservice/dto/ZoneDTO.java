package lk.ijse.zoneservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ZoneDTO {
    private int id;
    private String name;
    private double minTemp;
    private double maxTemp;
    private String deviceId;
}
