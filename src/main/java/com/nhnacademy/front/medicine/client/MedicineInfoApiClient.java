package com.nhnacademy.front.medicine.client;

import com.nhnacademy.front.global.client.GatewayClient;
import com.nhnacademy.front.global.dto.PageResponse;
import com.nhnacademy.front.medicine.dto.request.MedicineSearchRequest;
import com.nhnacademy.front.medicine.dto.response.MedicineDetailResponse;
import com.nhnacademy.front.medicine.dto.response.MedicineSearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;


@Component
@RequiredArgsConstructor
@Slf4j
public class MedicineInfoApiClient {

    private final GatewayClient gatewayClient;
    private static final String CORE_SERVICE = "/api/core";

    // 의약품 조회
    public PageResponse<MedicineSearchResponse> getMedicines(MedicineSearchRequest request, int page, int size){

//        log.info("search: {}",request.search());
//        String path = CORE_SERVICE + "?searchType="+request.searchType() + "&search="+ request.search() + "&page="+page + "&size="+ size;


        String path = UriComponentsBuilder
                .fromPath(CORE_SERVICE + "/medicines")
                .queryParam(
                        "searchType",
                        request.searchType()
                )
                .queryParam(
                        "search",
                        request.search()
                )
                .queryParam("page", page)
                .queryParam("size", size)
                .encode(StandardCharsets.UTF_8)
                .build()
                .toUriString();

        log.info("Front API 요청 path: {}", path);


        return gatewayClient.get(path,new ParameterizedTypeReference<>() {});

    }

    // 특정 의약품 조회
    public MedicineDetailResponse getMedicine(Long packageUnitId){

        return gatewayClient.get(CORE_SERVICE + "/medicines/package-units/" + packageUnitId, MedicineDetailResponse.class);

    }





}
