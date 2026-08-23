package com.nhnacademy.front.medicine.controller;

import com.nhnacademy.front.global.dto.PageResponse;
import com.nhnacademy.front.medicine.client.MedicineEnvApiClient;
import com.nhnacademy.front.medicine.client.MedicineInfoApiClient;
import com.nhnacademy.front.medicine.dto.EnvironmentType;
import com.nhnacademy.front.inventory.dto.request.InboundMedicineRequest;
import com.nhnacademy.front.medicine.dto.request.MedicineSearchRequest;
import com.nhnacademy.front.medicine.dto.response.MedicineEnvironmentTypeResponse;
import com.nhnacademy.front.medicine.dto.response.MedicineSearchResponse;
import com.nhnacademy.front.organization.client.StorageApiClient;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor

@Slf4j
public class MedicineSearchController {

    private final MedicineInfoApiClient medicineInfoApiClient;
    private final MedicineEnvApiClient medicineEnvApiClient;
    private final StorageApiClient storageApiClient;
    private static final String INBOUND_VIEW= "inventory/inbound";


    // 의약품 조회
    @GetMapping("/medicines")
    public String getMedicines(@Valid @ModelAttribute("medicineSearchRequest") MedicineSearchRequest medicineSearchRequest,
                               BindingResult bindingResult,
                               @RequestParam(
                                       name = "storageId",
                                       required = false
                               ) Long storageId,
                               @RequestParam(
                                       name = "zoneId",
                                       required = false
                               ) Long zoneId,
                               @RequestParam(name = "page", defaultValue = "0")int page,
                               @RequestParam(name = "size", defaultValue = "10")int size,
                               Model model){

        // 오류나기 전 필요한 정보 갖고 있어야 함
        model.addAttribute("storages",storageApiClient.getStorages());
        model.addAttribute("selectedStorageId",storageId);
        model.addAttribute("selectedZoneId",zoneId);

        model.addAttribute("inboundMedicineRequest",InboundMedicineRequest.from(null,zoneId));


        if(bindingResult.hasErrors()){
            return INBOUND_VIEW;
        }

        log.info(" 제품명 :{}",medicineSearchRequest.search());


        PageResponse<MedicineSearchResponse> medicines = medicineInfoApiClient.getMedicines(medicineSearchRequest,page,size);

        model.addAttribute("medicines",medicines);
        model.addAttribute("currentPage",medicines.page());
        model.addAttribute("totalPages",medicines.totalPages());
        model.addAttribute("pageSize",medicines.size());



        return INBOUND_VIEW;

    }


    // 특정 의약품 상세 정보 조회
    @GetMapping("/medicine-pack-units/{pack-unit-id}")
    public String getMedicine(@PathVariable(name= "pack-unit-id")Long packageUnitId,
                              @RequestParam(name = "storageId", required = false) Long storageId,
                              @RequestParam(name = "zoneId",required = false) Long zoneId,
                              Model model){


        model.addAttribute("storages",storageApiClient.getStorages());
        model.addAttribute("selectedStorageId",storageId);
        model.addAttribute("selectedZoneId",zoneId);


        // 선택한 특정 의약품 상세 정보
        model.addAttribute("medicine", medicineInfoApiClient.getMedicine(packageUnitId));

        List<MedicineEnvironmentTypeResponse> environments = medicineEnvApiClient.getTypes(packageUnitId);

        model.addAttribute("environments",environments);
        model.addAttribute("temperature", MedicineEnvironmentTypeResponse.from(environments, EnvironmentType.TEMPERATURE));
        model.addAttribute("humidity",MedicineEnvironmentTypeResponse.from(environments,EnvironmentType.HUMIDITY));
        model.addAttribute("illuminance",MedicineEnvironmentTypeResponse.from(environments,EnvironmentType.ILLUMINANCE));

        model.addAttribute("inboundMedicineRequest",InboundMedicineRequest.from(packageUnitId,zoneId));


       return INBOUND_VIEW;
    }







}
