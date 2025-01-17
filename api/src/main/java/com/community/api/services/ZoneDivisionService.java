package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.dto.DivisionProjectionDTO;
import com.community.api.entity.StateCode;
import com.community.api.entity.Zone;
import com.community.api.entity.ZoneDivisions;
import javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

@Service
public class ZoneDivisionService {

    @Autowired
    private EntityManager entityManager;
    @Autowired
    private DistrictService districtService;

    public List<DivisionProjectionDTO> getDivisionsByZoneId(Integer zoneId) throws NotFoundException {
        if(zoneId==null)
            throw new IllegalArgumentException("Zone id is compulsory");
        Zone zone=entityManager.find(Zone.class,zoneId);
        if(zone==null)
            throw new NotFoundException("Invalid zone selected");
        Query query =entityManager.createNativeQuery(Constant.GET_DIVISION_BY_ZONE);
        query.setParameter("zoneId",zoneId);
        List<Integer>divisionIds=query.getResultList();
        if(divisionIds.isEmpty())
            throw new NoResultException("No result found");
        List<DivisionProjectionDTO>resultList=new ArrayList<>();
        for(Integer a:divisionIds)
        {
            StateCode stateCode=entityManager.find(StateCode.class,a);
            if(stateCode!=null) {
                DivisionProjectionDTO divisionProjectionDTO=new DivisionProjectionDTO();
                divisionProjectionDTO.setDivisionId(stateCode.getState_id());
                divisionProjectionDTO.setDivisionName(stateCode.getState_name());
                divisionProjectionDTO.setDivisionCode(stateCode.getState_code());
                resultList.add(divisionProjectionDTO);
            }
        }
      return resultList;
    }

    public List<Integer> getDivisionIdsByZoneId(Integer zoneId) throws NotFoundException {
        if (zoneId == null) {
            throw new IllegalArgumentException("Zone ID is compulsory");
        }

        // Check if the zone exists
        Zone zone = entityManager.find(Zone.class, zoneId);
        if (zone == null) {
            throw new NotFoundException("Invalid zone selected");
        }

        // Using the correct field name state_id from StateCode entity
        List<Integer> divisionIds = entityManager.createQuery(
                        "SELECT zd.divisions.state_id FROM ZoneDivisions zd WHERE zd.zone.zoneId = :zoneId", Integer.class)
                .setParameter("zoneId", zoneId)
                .getResultList();
        System.out.println(divisionIds);

        if (divisionIds.isEmpty()) {
            throw new NoResultException("No divisions found for the selected zone");
        }

        return divisionIds;
    }
  
    public List<Zone>getAllZones()
    {
        Query query=entityManager.createQuery(Constant.GET_ALL_ZONES, Zone.class);
        return query.getResultList();
    }
    public Zone findDivisionsLinkedZone(Integer divisionId) throws NotFoundException {
        StateCode division=entityManager.find(StateCode.class,divisionId);
        if(division==null)
            throw new NotFoundException("Invalid Division");
        Query query=entityManager.createNativeQuery(Constant.GET_ZONE_LINKED_TO_DIVISION);
        query.setParameter("divisionId",divisionId);
        Integer zoneId=(Integer)query.getSingleResult();
        if(zoneId==null)
            throw new NoResultException("No results found");
        return entityManager.find(Zone.class,zoneId);
    }
    public void generateZoneDivision(Integer zoneId, Integer divisionId)
    {
        try{
            Zone zone=entityManager.find(Zone.class,zoneId);
            if (zone==null)
                throw new NotFoundException("Cannot insert value into ZoneDivision : Zone not found");
            StateCode division=entityManager.find(StateCode.class,divisionId);
            if(division==null)
                throw new NotFoundException("Cannot insert value into ZoneDivision : Division not found");
            ZoneDivisions zoneDivisions=new ZoneDivisions();
            zoneDivisions.setZone(zone);
            zoneDivisions.setDivisions(division);
            entityManager.persist(zoneDivisions);
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    public void populateZoneDivision()
    {
        Long count = entityManager.createQuery("SELECT count(z) FROM ZoneDivisions z", Long.class).getSingleResult();
        if(count==0) {
            // Northern Zone - Zone ID: 1
            generateZoneDivision(1, 8);  // Haryana
            generateZoneDivision(1, 9);  // Himachal Pradesh
            generateZoneDivision(1, 20); // Punjab
            generateZoneDivision(1, 29); // Jammu and Kashmir
            generateZoneDivision(1, 34); // Delhi
            generateZoneDivision(1, 27); // Uttarakhand
            generateZoneDivision(1, 26); // Uttar Pradesh
            generateZoneDivision(1, 21); // Rajasthan
            generateZoneDivision(1, 31); // Chandigarh

// Southern Zone - Zone ID: 2
            generateZoneDivision(2, 1);  // Andhra Pradesh
            generateZoneDivision(2, 24); // Telangana
            generateZoneDivision(2, 11); // Karnataka
            generateZoneDivision(2, 23); // Tamil Nadu
            generateZoneDivision(2, 12); // Kerala
            generateZoneDivision(2, 33); // Lakshadweep
            generateZoneDivision(2, 35); // Puducherry

// Eastern Zone - Zone ID: 3
            generateZoneDivision(3, 28); // West Bengal
            generateZoneDivision(3, 19); // Odisha
            generateZoneDivision(3, 4);  // Bihar
            generateZoneDivision(3, 10); // Jharkhand
            generateZoneDivision(3, 22); // Sikkim
            generateZoneDivision(3, 30); // Andaman and Nicobar Islands

// Western Zone - Zone ID: 4
            generateZoneDivision(4, 21); // Rajasthan
            generateZoneDivision(4, 7);  // Gujarat
            generateZoneDivision(4, 14); // Maharashtra
            generateZoneDivision(4, 6);  // Goa
            generateZoneDivision(4, 32); // Dadra and Nagar Haveli and Daman and Diu

// Central Zone - Zone ID: 5
            generateZoneDivision(5, 13); // Madhya Pradesh
            generateZoneDivision(5, 5);  // Chhattisgarh

// North-Eastern Zone - Zone ID: 6
            generateZoneDivision(6, 3);  // Assam
            generateZoneDivision(6, 16); // Meghalaya
            generateZoneDivision(6, 15); // Manipur
            generateZoneDivision(6, 17); // Mizoram
            generateZoneDivision(6, 18); // Nagaland
            generateZoneDivision(6, 25); // Tripura
            generateZoneDivision(6, 2);  // Arunachal Pradesh

// Special Union Territories Zone (Optional) - Zone ID: 7
            generateZoneDivision(7, 29); // Jammu and Kashmir
            generateZoneDivision(7, 31); // Chandigarh
            generateZoneDivision(7, 32); // Dadra and Nagar Haveli and Daman and Diu
            generateZoneDivision(7, 30); // Andaman and Nicobar Islands
            generateZoneDivision(7, 33); // Lakshadweep
            generateZoneDivision(7, 35); // Puducherry
            generateZoneDivision(7, 34); // Delhi
        }
    }
}
