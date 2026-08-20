//package com.nhnacademy.front.engine.controller;
//
//import com.nhnacademy.front.engine.client.RuleEngineApiClient;
//import com.nhnacademy.front.engine.dto.GenerationMode;
//import com.nhnacademy.front.engine.dto.SensorType;
//import com.nhnacademy.front.engine.dto.SensorValue;
//import com.nhnacademy.front.engine.dto.VirtualSensorStatus;
//import com.nhnacademy.front.engine.dto.VirtualSensorValues;
//import com.nhnacademy.front.engine.dto.request.VirtualSensorCreateRequest;
//import com.nhnacademy.front.engine.dto.request.VirtualSensorUpdateRequest;
//import com.nhnacademy.front.engine.dto.response.VirtualSensorInfoResponse;
//import org.junit.jupiter.api.Test;
//import org.mockito.ArgumentCaptor;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.util.Map;
//
//import static org.hamcrest.Matchers.containsString;
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.BDDMockito.given;
//import static org.mockito.BDDMockito.then;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@WebMvcTest(RuleEngineController.class)
//class RuleEngineControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockitoBean
//    private RuleEngineApiClient ruleEngineApiClient;
//
//    @Test
//    void createFormRendersNestedDtoFieldNames() throws Exception {
//        mockMvc.perform(get("/organizations/1/storages/2/zones/3/virtual-sensors/create"))
//                .andExpect(status().isOk())
//                .andExpect(content().string(containsString(
//                        "name=\"virtualSensorValues.valueMap[TEMPERATURE].mode\""
//                )))
//                .andExpect(content().string(containsString(
//                        "name=\"virtualSensorValues.valueMap[DOOR].probability\""
//                )));
//    }
//
//    @Test
//    void editAndInfoFormsRenderVirtualSensorValues() throws Exception {
//        VirtualSensorInfoResponse response = new VirtualSensorInfoResponse(
//                "virtual-3",
//                60L,
//                new VirtualSensorValues(Map.of(
//                        SensorType.TEMPERATURE,
//                        new SensorValue(GenerationMode.FIXED, null, null, 24.5, null),
//                        SensorType.DOOR,
//                        new SensorValue(GenerationMode.PROBABILITY, null, null, null, 0.2)
//                )),
//                VirtualSensorStatus.ACTIVE
//        );
//        given(ruleEngineApiClient.getVirtualSensorData(1L, 2L, 3L)).willReturn(response);
//
//        mockMvc.perform(get("/organizations/1/storages/2/zones/3/virtual-sensors/edit"))
//                .andExpect(status().isOk())
//                .andExpect(content().string(containsString("value=\"24.5\"")));
//
//        mockMvc.perform(get("/organizations/1/storages/2/zones/3/virtual-sensors"))
//                .andExpect(status().isOk())
//                .andExpect(content().string(containsString("24.5 (고정)")))
//                .andExpect(content().string(containsString("열림 확률 0.2")));
//    }
//
//    @Test
//    void createVirtualSensorBindsNestedSensorValueMap() throws Exception {
//        mockMvc.perform(post("/organizations/1/storages/2/zones/3/virtual-sensors")
//                        .param("deviceEui", "virtual-3")
//                        .param("measurementIntervalSeconds", "60")
//                        .param("enabledSensorTypes", "TEMPERATURE", "DOOR")
//                        .param("virtualSensorValues.valueMap[TEMPERATURE].mode", "RANGE")
//                        .param("virtualSensorValues.valueMap[TEMPERATURE].min", "20")
//                        .param("virtualSensorValues.valueMap[TEMPERATURE].max", "30")
//                        .param("virtualSensorValues.valueMap[DOOR].mode", "PROBABILITY")
//                        .param("virtualSensorValues.valueMap[DOOR].probability", "0.2"))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(redirectedUrl("/organizations/1/storages/2/zones/3/virtual-sensors"));
//
//        ArgumentCaptor<VirtualSensorCreateRequest> captor =
//                ArgumentCaptor.forClass(VirtualSensorCreateRequest.class);
//        then(ruleEngineApiClient).should().createVirtualSensorData(
//                eq(1L), eq(2L), eq(3L), captor.capture()
//        );
//
//        assertThat(captor.getValue()).isEqualTo(new VirtualSensorCreateRequest(
//                "virtual-3",
//                60L,
//                new VirtualSensorValues(Map.of(
//                        SensorType.TEMPERATURE,
//                        new SensorValue(GenerationMode.RANGE, 20.0, 30.0, null, null),
//                        SensorType.DOOR,
//                        new SensorValue(GenerationMode.PROBABILITY, null, null, null, 0.2)
//                ))
//        ));
//    }
//
//    @Test
//    void updateVirtualSensorBindsFixedValues() throws Exception {
//        mockMvc.perform(put("/organizations/1/storages/2/zones/3/virtual-sensors")
//                        .param("measurementIntervalSeconds", "30")
//                        .param("virtualSensorValues.valueMap[HUMIDITY].mode", "FIXED")
//                        .param("virtualSensorValues.valueMap[HUMIDITY].fixedValue", "55"))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(redirectedUrl("/organizations/1/storages/2/zones/3/virtual-sensors"));
//
//        ArgumentCaptor<VirtualSensorUpdateRequest> captor =
//                ArgumentCaptor.forClass(VirtualSensorUpdateRequest.class);
//        then(ruleEngineApiClient).should().updateVirtualSensorData(
//                eq(1L), eq(2L), eq(3L), captor.capture()
//        );
//
//        assertThat(captor.getValue()).isEqualTo(new VirtualSensorUpdateRequest(
//                30L,
//                new VirtualSensorValues(Map.of(
//                        SensorType.HUMIDITY,
//                        new SensorValue(GenerationMode.FIXED, null, null, 55.0, null)
//                ))
//        ));
//    }
//}
