package com.nhnacademy.front.medicine.dto.response;

import com.nhnacademy.front.medicine.dto.EnvironmentType;

import java.math.BigDecimal;
import java.util.List;

public record MedicineEnvironmentTypeResponse(

        EnvironmentType type,
        BigDecimal min,
        BigDecimal max

) {


    public static MedicineEnvironmentTypeResponse from(List<MedicineEnvironmentTypeResponse> environments,EnvironmentType type){

        if(environments == null || environments.isEmpty()){
            return null;
        }

        for(MedicineEnvironmentTypeResponse response : environments){

            if(response.type() == type){
                return response;
            }

        }

        return null;

    }




}
