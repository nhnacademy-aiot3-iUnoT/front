package com.nhnacademy.front.medicine.controller;

import com.nhnacademy.front.global.dto.PageResponse;
import com.nhnacademy.front.medicine.client.MedicineEnvApiClient;
import com.nhnacademy.front.medicine.client.MedicineInfoApiClient;

import com.nhnacademy.front.medicine.dto.request.MedicineEnvironmentRequest;
import com.nhnacademy.front.medicine.dto.request.MedicineSearchRequest;
import com.nhnacademy.front.medicine.dto.response.MedicineEnvironmentTypeResponse;
import com.nhnacademy.front.medicine.dto.response.MedicineSearchResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/pack-units")
public class MedicineEnvController {

    private final MedicineEnvApiClient medicineEnvApiClient;
    private final MedicineInfoApiClient medicineInfoApiClient;
    private static final String ENV_VIEW = "inventory/medicine-env-list";
    private static final String REDIRECT_STANDARD = "redirect:/pack-units/environment-standards";


    @GetMapping("/environment-standards")
    public String standardForm(){

        return ENV_VIEW;
    }

    // 환경 기준 조회용  의약품 검색
    @GetMapping
    public String getMedicines(@Valid @ModelAttribute MedicineSearchRequest request,
                               BindingResult bindingResult,
                               @RequestParam(name = "page", defaultValue = "0")int page,
                               @RequestParam(name = "size", defaultValue = "10")int size,
                               Model model
    ){

        if(bindingResult.hasErrors()){
            return REDIRECT_STANDARD;
        }


        PageResponse<MedicineSearchResponse> medicines = medicineInfoApiClient.getMedicines(request,page,size);

        model.addAttribute("medicineSearchRequest",request);
        model.addAttribute("medicines",medicines.content());
        model.addAttribute("currentPage",medicines.page());
        model.addAttribute("totalPages",medicines.totalPages());
        model.addAttribute("pageSize",medicines.size());

        return ENV_VIEW;
    }


    // 선택 의약품 정보/환경기준 조회
    @GetMapping("/{pack-unit-id}/environment-standards")
    public String getStandards(@PathVariable(name="pack-unit-id")Long packUnitId, Model model){


        List<MedicineEnvironmentTypeResponse> environmentTypes = medicineEnvApiClient.getTypes(packUnitId);


        model.addAttribute("medicine",medicineInfoApiClient.getMedicine(packUnitId));
        model.addAttribute("environmentTypes",environmentTypes);

        return ENV_VIEW;
    }



    //환경기준 생성
    @PostMapping("/{pack-unit-id}/environment-standards")
    public String createStandards(@PathVariable(name = "pack-unit-id") Long packUnitId,
                                  @Valid @ModelAttribute MedicineEnvironmentRequest request,
                                  BindingResult bindingResult
                                  ){

        if(bindingResult.hasErrors()){
            return REDIRECT_STANDARD;
        }

        medicineEnvApiClient.createTypes(packUnitId,request);

        return REDIRECT_STANDARD;

    }


    //환경기준 수정
    @PutMapping("/{pack-unit-id}/environment-standards")
    public String updateStandards(@PathVariable(name = "pack-unit-id")Long packUnitId,
                                  @Valid @ModelAttribute MedicineEnvironmentRequest request,
                                  BindingResult bindingResult
                                  ){

        if(bindingResult.hasErrors()){
            return REDIRECT_STANDARD;
        }

        medicineEnvApiClient.updateTypes(packUnitId,request);

        return REDIRECT_STANDARD;

    }


    // 환경기준 삭제
    @DeleteMapping("/environment-standards/{environment-standard-id}")
    public String deleteStandards(@PathVariable(name = "environment-standard-id") Long standardId){

        medicineEnvApiClient.deleteTypes(standardId);

        return REDIRECT_STANDARD;
    }





}
