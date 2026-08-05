package com.nhnacademy.front.global.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.front.global.dto.ApiResponse;
import com.nhnacademy.front.global.error.ApiException;
import com.nhnacademy.front.global.error.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.util.function.Supplier;

// 추후 리팩토링 할 예정 (임시 진행)
@Slf4j
@Component
public class GatewayClient {
    private final String baseUrl;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public GatewayClient(
            @Value("${gateway.url}") String baseUrl,
            RestClient restClient,
            ObjectMapper objectMapper
    ) {
        this.baseUrl = baseUrl;
        this.restClient = restClient;
        this.objectMapper = objectMapper;
    }

    // 단건 DTO
    public <T> T get(String path, Class<T> dataType) {
        return execute(() ->
                restClient.get()
                        .uri(baseUrl + path)
                        .retrieve()
                        .body(responseTypeOf(dataType)));
    }

    // List, Page 등 제네릭 타입
    public <T> T get(String path, ParameterizedTypeReference<ApiResponse<T>> responseType) {
        return execute(() ->
                restClient.get()
                        .uri(URI.create(baseUrl + path))
                        .retrieve()
                        .body(responseType));
    }

    public <T> T post(String path, Object body, Class<T> dataType) {
        return execute(() ->
                restClient.post()
                        .uri(baseUrl + path)
                        .body(body)
                        .retrieve()
                        .body(responseTypeOf(dataType)));
    }

    public <T> T post(String path, Object body, ParameterizedTypeReference<ApiResponse<T>> responseType) {
        return execute(() ->
                restClient.post()
                        .uri(baseUrl + path)
                        .body(body)
                        .retrieve()
                        .body(responseType));
    }

    public <T> T put(String path, Object body, Class<T> dataType) {
        return execute(() ->
                restClient.put()
                        .uri(baseUrl + path)
                        .body(body)
                        .retrieve()
                        .body(responseTypeOf(dataType)));
    }

    public <T> T put(String path, Object body, ParameterizedTypeReference<ApiResponse<T>> responseType) {
        return execute(() ->
                restClient.put()
                        .uri(baseUrl + path)
                        .body(body)
                        .retrieve()
                        .body(responseType));
    }

    public void put(String path, Object body) {
        try {
            restClient.put()
                    .uri(baseUrl + path)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();

        } catch (HttpStatusCodeException e) {
            throw convertApiException(e);
        }
    }

    public <T> T delete(String path, ParameterizedTypeReference<ApiResponse<T>> responseType) {
        return execute(() ->
                restClient.delete()
                        .uri(baseUrl + path)
                        .retrieve()
                        .body(responseType));
    }

    public void delete(String path) {
        try {
            restClient.delete()
                    .uri(baseUrl + path)
                    .retrieve()
                    .toBodilessEntity();

        } catch (HttpStatusCodeException e) {
            throw convertApiException(e);
        }
    }

    private <T> T execute(Supplier<ApiResponse<T>> supplier) {
        try {
            ApiResponse<T> response = supplier.get();

            if (response == null) {
                throw new ApiException(
                        ErrorCode.UNKNOWN,
                        "응답이 없습니다."
                );
            }

            if (!response.success()) {
                throw new ApiException(
                        ErrorCode.from(response.error().code()),
                        response.error().message()
                );
            }

            return response.data();

        } catch (HttpStatusCodeException e) {
            throw convertApiException(e); // API Server가 준 JSON -> Front 예외 객체로 변환
        }
    }

    private ApiException convertApiException(HttpStatusCodeException e) {
        String responseBody = e.getResponseBodyAsString();

        log.error(
                "Gateway request failed. status={}, body={}",
                e.getStatusCode(),
                responseBody
        );


        try {
            ApiResponse<Void> response = objectMapper.readValue(
                    e.getResponseBodyAsString(),
                    new TypeReference<>() {}
            );

            return new ApiException(
                    ErrorCode.from(response.error().code()),
                    response.error().message()
            );

        } catch (Exception ex) {
            return new ApiException(
                    ErrorCode.UNKNOWN,
                    "서버 요청 처리 중 오류가 발생했습니다."
            );
        }
    }

    private <T> ParameterizedTypeReference<ApiResponse<T>> responseTypeOf(Class<T> dataType) {
        ResolvableType type = ResolvableType.forClassWithGenerics(ApiResponse.class, dataType);
        return ParameterizedTypeReference.forType(type.getType());
    }

}
