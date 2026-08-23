package com.nhnacademy.front.medicine.client;

import com.nhnacademy.front.global.client.GatewayClient;
import com.nhnacademy.front.medicine.dto.request.MedicineEnvironmentRequest;
import com.nhnacademy.front.medicine.dto.response.MedicineEnvironmentTypeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MedicineEnvApiClient {

    private final GatewayClient gatewayClient;
    private static final String CORE_SERVICE = "/api/core";



    // ///api/core/medicine-environment/package-units/58581
    // 환경유형 조회
    public List<MedicineEnvironmentTypeResponse> getTypes(Long packageUnitId){

        return gatewayClient.get(CORE_SERVICE + "/package-units/"+ packageUnitId +"/medicine-environment-types", new ParameterizedTypeReference<>() {});

    }

    //환경유형 수정
    public void updateTypes(Long packageUnitId, MedicineEnvironmentRequest request){

        gatewayClient.put(CORE_SERVICE + "/package-units/"+ packageUnitId + "/medicine-environment-standards",request);

    }

    // 환경유형 삭제
    public void deleteTypes(Long standardId){

        gatewayClient.delete(CORE_SERVICE + "/medicine-environment-standards/"+ standardId);
    }



}
