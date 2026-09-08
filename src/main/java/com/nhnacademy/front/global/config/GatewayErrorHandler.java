package com.nhnacademy.front.global.config;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.front.global.dto.ApiResponse;
import com.nhnacademy.front.global.dto.ErrorDetail;
import com.nhnacademy.front.global.error.ApiException;
import com.nhnacademy.front.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class GatewayErrorHandler implements RestClient.ResponseSpec.ErrorHandler {
    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpRequest request, ClientHttpResponse response) throws IOException {
        HttpStatusCode status = response.getStatusCode();
        String body = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);

        throw toApiException(status, body);
    }

    private ApiException toApiException(HttpStatusCode status, String body) {
        ErrorDetail error = readErrorDetail(body);

        if (error == null) {
            return unrecognized(status, body);
        }

        logFailure(status, error.code(), error.message());

        return new ApiException(ErrorCode.from(error.code()), error.message());
    }

    private ErrorDetail readErrorDetail(String body) {
        try {
            ApiResponse<Void> response = objectMapper.readValue(body, new TypeReference<>() {});

            return (response == null) ?  null : response.error();
        } catch (JacksonException e) {
            return null;
        }
    }

    private ApiException unrecognized(HttpStatusCode status, String body) {
        log.error("게이트웨이 통신 에러: status={}, body={}", status, body);

        return new ApiException(ErrorCode.UNKNOWN, "서버 요청 처리 중 에러가 발생했습니다.");
    }

    private void logFailure(HttpStatusCode status, String code, String message) {
        if (status.is5xxServerError()) {
            log.error("게이트웨이 요청 실패: status={}, code={}, message={}", status, code, message);
        } else {
            log.warn("게이트웨이 요청 실패: status={}, code={}, message={}", status, code, message);
        }
    }
}
