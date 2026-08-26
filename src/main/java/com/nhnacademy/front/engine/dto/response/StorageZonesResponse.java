package com.nhnacademy.front.engine.dto.response;

import com.nhnacademy.front.organization.dto.StorageStatus;
import com.nhnacademy.front.organization.dto.response.ZoneInfoResponse;

import java.util.List;

// 환경 관리 화면에서 저장소 하나와 그 저장소의 구역 목록을 묶어서 보여준다.
public record StorageZonesResponse(
        Long storageId,
        String storageName,
        StorageStatus storageStatus,
        List<ZoneInfoResponse> zones
) {
}
