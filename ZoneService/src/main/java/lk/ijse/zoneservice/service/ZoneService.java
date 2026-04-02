package lk.ijse.zoneservice.service;

import lk.ijse.zoneservice.dto.ZoneDTO;

import java.util.List;

public interface ZoneService {
    ZoneDTO save(ZoneDTO dto);
    ZoneDTO update(int id, ZoneDTO dto);
    List<ZoneDTO> getAll();
    ZoneDTO getById(int id);
    void delete(int id);
    String checkZoneStatus(int id, double currentTemp);
    List<ZoneDTO> searchByName(String name);
    List<ZoneDTO> filterByTemperature(double temp);



}
