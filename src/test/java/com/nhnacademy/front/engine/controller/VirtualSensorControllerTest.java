package com.nhnacademy.front.engine.controller;

import com.nhnacademy.front.engine.client.RuleEngineApiClient;
import com.nhnacademy.front.engine.dto.GenerationMode;
import com.nhnacademy.front.engine.dto.SensorType;
import com.nhnacademy.front.engine.dto.SensorValue;
import com.nhnacademy.front.engine.dto.VirtualSensorStatus;
import com.nhnacademy.front.engine.dto.VirtualSensorValues;
import com.nhnacademy.front.engine.dto.response.VirtualSensorInfoResponse;
import com.nhnacademy.front.organization.client.OrganizationApiClient;
import com.nhnacademy.front.organization.dto.OrganizationRole;
import com.nhnacademy.front.admin.dto.OrganizationStatus;
import com.nhnacademy.front.organization.dto.response.OrgDetailResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(VirtualSensorController.class)
class VirtualSensorControllerTest {

    private static final Long ORGANIZATION_ID = 7L;

    private static final String DEVICE_EUI = "virtual-device-1";

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    private RuleEngineApiClient ruleEngineApiClient;

    @MockitoBean
    private OrganizationApiClient organizationApiClient;

    @BeforeEach
    void setUp() {
        given(organizationApiClient.getOrgInfo()).willReturn(orgInfo());
    }

    @Test
    @DisplayName("가상센서 관리 화면에 조직의 가상 센서가 보인다")
    void virtualSensors() throws Exception {
        given(ruleEngineApiClient.getVirtualSensors(ORGANIZATION_ID)).willReturn(List.of(
                virtualSensor(DEVICE_EUI, 11L)
        ));

        mockMvc.perform(get("/virtual-sensors"))
                .andExpect(status().isOk())
                .andExpect(view().name("organization/virtualsensor-list"))
                .andExpect(content().string(containsString(DEVICE_EUI)))
                .andExpect(content().string(containsString("Zone 11")));
    }

    @Test
    @DisplayName("구역에 등록되지 않은 가상 센서는 미등록으로 보인다")
    void virtualSensorsShowsUnregistered() throws Exception {
        given(ruleEngineApiClient.getVirtualSensors(ORGANIZATION_ID)).willReturn(List.of(
                virtualSensor(DEVICE_EUI, null)
        ));

        mockMvc.perform(get("/virtual-sensors"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("미등록")));
    }

    @Test
    @DisplayName("가상 센서가 없으면 안내 문구만 보인다")
    void virtualSensorsWhenEmpty() throws Exception {
        given(ruleEngineApiClient.getVirtualSensors(ORGANIZATION_ID)).willReturn(List.of());

        mockMvc.perform(get("/virtual-sensors"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("등록된 가상 센서가 없습니다.")));
    }

    @Test
    @DisplayName("생성 화면은 구역을 고르지 않고 deviceEui만 받는다")
    void showCreateForm() throws Exception {
        mockMvc.perform(get("/virtual-sensors/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("organization/virtualsensor-create"))
                .andExpect(content().string(containsString("디바이스 EUI")));
    }

    @Test
    @DisplayName("생성하면 목록으로 돌아간다")
    void createVirtualSensor() throws Exception {
        mockMvc.perform(post("/virtual-sensors")
                        .param("deviceEui", DEVICE_EUI)
                        .param("measurementIntervalSeconds", "10")
                        .param("virtualSensorValues.valueMap[TEMPERATURE].mode", "FIXED")
                        .param("virtualSensorValues.valueMap[TEMPERATURE].fixedValue", "21.5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/virtual-sensors"));

        verify(ruleEngineApiClient).createVirtualSensorData(eq(ORGANIZATION_ID), any());
    }

    @Test
    @DisplayName("입력이 잘못되면 생성하지 않고 폼을 다시 보여준다")
    void createVirtualSensorRejectsInvalidInput() throws Exception {
        mockMvc.perform(post("/virtual-sensors")
                        .param("deviceEui", "")
                        .param("measurementIntervalSeconds", "10"))
                .andExpect(status().isOk())
                .andExpect(view().name("organization/virtualsensor-create"));
    }

    @Test
    @DisplayName("설정이 없는 deviceEui의 수정 화면은 목록으로 돌려보낸다")
    void showUpdateFormRedirectsWhenNotRegistered() throws Exception {
        given(ruleEngineApiClient.getVirtualSensorData(ORGANIZATION_ID, DEVICE_EUI))
                .willReturn(new VirtualSensorInfoResponse(false, null, null, null, null, null));

        mockMvc.perform(get("/virtual-sensors/{deviceEui}/edit", DEVICE_EUI))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/virtual-sensors"));
    }

    private VirtualSensorInfoResponse virtualSensor(String deviceEui, Long zoneId) {
        return new VirtualSensorInfoResponse(
                true,
                deviceEui,
                10L,
                new VirtualSensorValues(Map.of(
                        SensorType.TEMPERATURE,
                        new SensorValue(GenerationMode.FIXED, null, null, 21.5, null)
                )),
                VirtualSensorStatus.ACTIVE,
                zoneId
        );
    }

    private OrgDetailResponse orgInfo() {
        return new OrgDetailResponse(
                ORGANIZATION_ID,
                "테스트조직",
                "도로명",
                "12345",
                "상세주소",
                "설명",
                OrganizationStatus.ACTIVE,
                LocalDateTime.now(),
                OrganizationRole.ORG_OWNER
        );
    }
}
