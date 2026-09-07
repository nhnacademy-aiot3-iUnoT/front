package com.nhnacademy.front.global.client;

import com.nhnacademy.front.global.dto.ApiResponse;
import com.nhnacademy.front.global.error.ApiException;
import com.nhnacademy.front.global.error.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.ResolvableType;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URI;

// 추후 리팩토링 할 예정 (임시 진행)
@Slf4j
@Component
public class GatewayClient {
    private final String baseUrl;
    private final RestClient restClient;

    public GatewayClient(
            @Value("${gateway.url}") String baseUrl,
            RestClient restClient
    ) {
        this.baseUrl = baseUrl;
        this.restClient = restClient;
    }

    // 단건 DTO
    public <T> T get(String path, Class<T> dataType) {
        return request(HttpMethod.GET, path, null, responseTypeOf(dataType));
    }

    // List, Page 등 제네릭 타입
    public <T> T get(String path, ParameterizedTypeReference<ApiResponse<T>> responseType) {
        return request(HttpMethod.GET, path, null, responseType);
    }

    public <T> T getRaw(String path, ParameterizedTypeReference<T> responseType) {
        T response = prepare(HttpMethod.GET, path, null)
                .retrieve()
                .body(responseType);

        if (response == null) {
            throw new ApiException(
                    ErrorCode.UNKNOWN,
                    "응답이 없습니다."
            );
        }

        return response;
    }

    public void get(String path) {
        requestVoid(HttpMethod.GET, path, null);
    }

    public <T> T post(String path, Object body, Class<T> dataType) {
        return request(HttpMethod.POST, path, body, responseTypeOf(dataType));
    }

    public <T> T post(String path, Object body, ParameterizedTypeReference<ApiResponse<T>> responseType) {
        return request(HttpMethod.POST, path, body, responseType);
    }

    public void post(String path, Object body) {
        requestVoid(HttpMethod.POST, path, body);
    }

    public void post(String path) {
        requestVoid(HttpMethod.POST, path, null);
    }

    public <T> T put(String path, Object body, Class<T> dataType) {
        return request(HttpMethod.PUT, path, body, responseTypeOf(dataType));
    }

    public <T> T put(String path, Object body, ParameterizedTypeReference<ApiResponse<T>> responseType) {
        return request(HttpMethod.PUT, path, body, responseType);
    }

    public void put(String path, Object body) {
        requestVoid(HttpMethod.PUT, path, body);
    }

    public <T> T delete(String path, ParameterizedTypeReference<ApiResponse<T>> responseType) {
        return request(HttpMethod.DELETE, path, null, responseType);
    }

    public void delete(String path) {
        requestVoid(HttpMethod.DELETE, path, null);
    }

    public void delete(String path, Object body) {
        requestVoid(HttpMethod.DELETE, path, body);
    }

    private <T> T request(HttpMethod method, String path, Object body, ParameterizedTypeReference<ApiResponse<T>> responseType) {
        ApiResponse<T> response = prepare(method, path, body)
                .retrieve()
                .body(responseType);

        if (response == null) {
            throw new ApiException(ErrorCode.UNKNOWN, "응답이 없습니다.");
        }

        if (!response.success()) {
            if (response.error() == null) {
                throw new ApiException(ErrorCode.UNKNOWN, "응답이 없습니다.");
            }

            String code = response.error().code();
            String message = response.error().message();

            throw new ApiException(ErrorCode.from(code), message);
        }

        return response.data();
    }

    private void requestVoid(HttpMethod method, String path, Object body) {
        prepare(method, path, body).retrieve()
                .toBodilessEntity();
    }

    private RestClient.RequestBodySpec prepare(HttpMethod method, String path, Object body) {
        RestClient.RequestBodySpec spec = restClient.method(method)
                .uri(URI.create(baseUrl + path));

        if (body != null) {
            spec.body(body);
        }

        return spec;
    }

    private <T> ParameterizedTypeReference<ApiResponse<T>> responseTypeOf(Class<T> dataType) {
        ResolvableType type = ResolvableType.forClassWithGenerics(ApiResponse.class, dataType);
        return ParameterizedTypeReference.forType(type.getType());
    }
}
