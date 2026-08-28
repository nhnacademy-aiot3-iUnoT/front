package com.nhnacademy.front.inventory.client;

import com.nhnacademy.front.global.client.GatewayClient;
import com.nhnacademy.front.inventory.dto.response.ReviewHistoryDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class EnvironmentApiClient {
    private final GatewayClient gatewayClient;
    private static final String CORE_SERVICE = "/api/core";

    public ReviewHistoryDetailResponse getReviewDetail(Long environmentReviewId){
        String uri = String.format("%s/environment-reviews/%d", CORE_SERVICE, environmentReviewId);

        return gatewayClient.get(
                uri,
                ReviewHistoryDetailResponse.class
        );
    }
}