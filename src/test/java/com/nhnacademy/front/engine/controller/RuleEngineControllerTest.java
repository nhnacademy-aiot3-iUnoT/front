package com.nhnacademy.front.engine.controller;

import com.nhnacademy.front.engine.client.RuleEngineApiClient;
import com.nhnacademy.front.organization.client.OrganizationApiClient;
import com.nhnacademy.front.organization.client.StorageApiClient;
import com.nhnacademy.front.organization.client.ZoneApiClient;
import com.nhnacademy.front.organization.dto.EnvStatus;
import com.nhnacademy.front.organization.dto.StorageStatus;
import com.nhnacademy.front.organization.dto.ZoneStatus;
import com.nhnacademy.front.organization.dto.response.StorageInfoResponse;
import com.nhnacademy.front.organization.dto.response.ZoneInfoResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(RuleEngineController.class)
class RuleEngineControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    private RuleEngineApiClient ruleEngineApiClient;

    @MockitoBean
    private StorageApiClient storageApiClient;

    @MockitoBean
    private ZoneApiClient zoneApiClient;

    @MockitoBean
    private OrganizationApiClient organizationApiClient;

    @Test
    @DisplayName("환경 관리 화면에 저장소와 그 저장소의 구역이 함께 보인다.")
    void environmentMonitoring_RendersStoragesWithZones() throws Exception {
        given(storageApiClient.getStorages()).willReturn(List.of(
                new StorageInfoResponse(1L, 7L, "테스트조직", "본관창고", StorageStatus.ACTIVE),
                new StorageInfoResponse(2L, 7L, "테스트조직", "별관창고", StorageStatus.ACTIVE)
        ));
        given(zoneApiClient.getZones(1L)).willReturn(List.of(
                new ZoneInfoResponse(11L, 1L, "냉장구역", ZoneStatus.ACTIVE, EnvStatus.NORMAL),
                new ZoneInfoResponse(12L, 1L, "상온구역", ZoneStatus.ACTIVE, EnvStatus.WARNING)
        ));
        given(zoneApiClient.getZones(2L)).willReturn(List.of());

        mockMvc.perform(get("/environmentMonitoring"))
                .andExpect(status().isOk())
                .andExpect(view().name("organization/environment-monitoring"))
                .andExpect(content().string(containsString("테스트조직")))
                .andExpect(content().string(containsString("본관창고")))
                .andExpect(content().string(containsString("별관창고")))
                .andExpect(content().string(containsString("냉장구역")))
                .andExpect(content().string(containsString("상온구역")))
                // 구역이 없는 저장소는 안내 문구를 보여준다.
                .andExpect(content().string(containsString("등록된 구역이 없습니다.")));
    }

    @Test
    @DisplayName("구역 항목은 그 구역의 센서 상세 화면으로 연결된다.")
    void environmentMonitoring_ZoneLinksToSensorInfo() throws Exception {
        given(storageApiClient.getStorages()).willReturn(List.of(
                new StorageInfoResponse(1L, 7L, "테스트조직", "본관창고", StorageStatus.ACTIVE)
        ));
        given(zoneApiClient.getZones(1L)).willReturn(List.of(
                new ZoneInfoResponse(11L, 1L, "냉장구역", ZoneStatus.ACTIVE, EnvStatus.CRITICAL)
        ));

        mockMvc.perform(get("/environmentMonitoring"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(
                        "/storages/1/zones/11/sensorInfo"
                )));
    }

    @Test
    @DisplayName("저장소가 없으면 안내 문구만 보인다.")
    void environmentMonitoring_WhenNoStorage_RendersEmptyMessage() throws Exception {
        given(storageApiClient.getStorages()).willReturn(List.of());

        mockMvc.perform(get("/environmentMonitoring"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("등록된 저장소가 없습니다.")));
    }
}
