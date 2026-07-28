package com.nhnacademy.front.global.client;

import com.nhnacademy.front.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GatewayClient {
    private static final String BASE_URL = "lb://team1-gateway";
    private final RestClient restClient;

    public <T> ApiResponse<T> get(String path, Class<T> dataType) {
        return restClient.get()
                .uri(BASE_URL + path)
                .retrieve()
                .body(responseTypeOf(dataType));
    }

    public <T> ApiResponse<List<T>> getList(String path, Class<T> dataType) {
        return restClient.get()
                .uri(BASE_URL + path)
                .retrieve()
                .body(listResponseTypeOf(dataType));
    }

    public <T> ApiResponse<T> post(String path, Object body, Class<T> dataType) {
        return restClient.post()
                .uri(BASE_URL + path)
                .body(body)
                .retrieve()
                .body(responseTypeOf(dataType));
    }

    public ApiResponse<Void> put(String path, Object body) {
        return restClient.put()
                .uri(BASE_URL + path)
                .body(body)
                .retrieve()
                .body(responseTypeOf(Void.class));
    }

    public ApiResponse<Void> delete(String path) {
        return restClient.delete()
                .uri(BASE_URL + path)
                .retrieve()
                .body(responseTypeOf(Void.class));
    }

    private <T> ParameterizedTypeReference<ApiResponse<T>> responseTypeOf(Class<T> dataType) {
        ResolvableType type = ResolvableType.forClassWithGenerics(ApiResponse.class, dataType);
        return ParameterizedTypeReference.forType(type.getType());
    }

    private <T> ParameterizedTypeReference<ApiResponse<List<T>>> listResponseTypeOf(Class<T> dataType) {
        ResolvableType listType = ResolvableType.forClassWithGenerics(List.class, dataType);
        ResolvableType type = ResolvableType.forClassWithGenerics(ApiResponse.class, listType);
        return ParameterizedTypeReference.forType(type.getType());
    }
}
