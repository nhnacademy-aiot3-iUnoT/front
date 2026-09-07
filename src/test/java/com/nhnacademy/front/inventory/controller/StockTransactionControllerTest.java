package com.nhnacademy.front.inventory.controller;

import com.nhnacademy.front.global.dto.PageResponse;
import com.nhnacademy.front.inventory.client.StockTransactionApiClient;
import com.nhnacademy.front.inventory.dto.TransactionType;
import com.nhnacademy.front.inventory.dto.response.StockTransactionResponse;
import com.nhnacademy.front.organization.client.StorageApiClient;
import com.nhnacademy.front.organization.client.ZoneApiClient;
import com.nhnacademy.front.organization.dto.EnvStatus;
import com.nhnacademy.front.organization.dto.StorageStatus;
import com.nhnacademy.front.organization.dto.ZoneStatus;
import com.nhnacademy.front.organization.dto.response.StorageInfoResponse;
import com.nhnacademy.front.organization.dto.response.ZoneInfoResponse;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(StockTransactionController.class)
class StockTransactionControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    private StockTransactionApiClient stockTransactionApiClient;

    @MockitoBean
    private StorageApiClient storageApiClient;

    @MockitoBean
    private ZoneApiClient zoneApiClient;

    @Test
    @DisplayName("구역 탭과 변동 내역이 함께 렌더링된다.")
    void stockTransactions_RendersZonesAndTransactions() throws Exception {
        givenStoragesAndZones();
        givenTransactions();

        mockMvc.perform(get("/stock-transactions"))
                .andExpect(status().isOk())
                .andExpect(view().name("inventory/stock-transaction-list"))
                .andExpect(content().string(Matchers.containsString("입출고 내역")))
                .andExpect(content().string(Matchers.containsString("냉장 보관실")))
                .andExpect(content().string(Matchers.containsString("실온 보관실")))
                .andExpect(content().string(Matchers.containsString("타이레놀정")));
    }

    @Test
    @DisplayName("입고는 +로, 폐기는 -로 수량을 표시한다.")
    void stockTransactions_RendersSignedQuantity() throws Exception {
        givenStoragesAndZones();
        givenTransactions();

        mockMvc.perform(get("/stock-transactions"))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("+120")))
                .andExpect(content().string(Matchers.containsString("-8")));
    }

    @Test
    @DisplayName("상세 모달이 읽을 data 속성이 행에 포함된다.")
    void stockTransactions_RendersDetailDataAttributes() throws Exception {
        givenStoragesAndZones();
        givenTransactions();

        mockMvc.perform(get("/stock-transactions"))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("stock-transaction-row")))
                .andExpect(content().string(Matchers.containsString("data-memo")))
                .andExpect(content().string(Matchers.containsString("data-processed-by")))
                .andExpect(content().string(Matchers.containsString("김약사")))
                .andExpect(content().string(Matchers.containsString("유통기한 만료")));
    }

    @Test
    @DisplayName("처리자 이름을 표에 보여주고, 없으면 -로 표시한다.")
    void stockTransactions_RendersProcessorName() throws Exception {
        givenStoragesAndZones();
        givenTransactions();

        mockMvc.perform(get("/stock-transactions"))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("처리자")))
                .andExpect(content().string(Matchers.containsString("김약사")));
    }

    @Test
    @DisplayName("사유 코드를 한글로 바꿔 보여주고, 모르는 값은 그대로 둔다.")
    void stockTransactions_TranslatesReason() throws Exception {
        givenStoragesAndZones();
        given(stockTransactionApiClient.search(
                eq(1L), any(), any(), any(), any(), anyInt(), anyInt()))
                .willReturn(new PageResponse<>(List.of(
                        transaction("STORAGE_TRANSFER"), transaction("직접 입력한 사유")), 0, 20, 2L, 1, true));

        mockMvc.perform(get("/stock-transactions"))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("저장소 이동")))
                .andExpect(content().string(Matchers.containsString("직접 입력한 사유")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("STORAGE_TRANSFER"))));
    }

    private StockTransactionResponse transaction(String reason) {
        return new StockTransactionResponse(
                1L, "타이레놀정", "10정/PTP", TransactionType.OUTBOUND, 10,
                reason, null, UUID.randomUUID(), "김약사", LocalDateTime.of(2026, 8, 10, 9, 20));
    }

    @Test
    @DisplayName("저장소가 없으면 안내 문구만 보여준다.")
    void stockTransactions_WhenNoStorage_ShowsGuide() throws Exception {
        given(storageApiClient.getStorages()).willReturn(List.of());

        mockMvc.perform(get("/stock-transactions"))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("조회할 수 있는 저장소가 없습니다")))
                .andExpect(model().attribute("selectedStorageId", Matchers.nullValue()));
    }

    @Test
    @DisplayName("구역이 없으면 구역 없음 안내를 보여준다.")
    void stockTransactions_WhenNoZone_ShowsGuide() throws Exception {
        given(storageApiClient.getStorages()).willReturn(storages());
        given(zoneApiClient.getZones(anyLong())).willReturn(List.of());

        mockMvc.perform(get("/stock-transactions"))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("이 저장소에는 등록된 구역이 없습니다")));
    }

    @Test
    @DisplayName("목록 조회에 실패해도 검색 조건 화면은 그대로 보여준다.")
    void stockTransactions_WhenSearchFails_StillRenders() throws Exception {
        givenStoragesAndZones();
        given(stockTransactionApiClient.search(
                anyLong(), any(), any(), any(), any(), anyInt(), anyInt()))
                .willThrow(new RuntimeException("gateway down"));

        mockMvc.perform(get("/stock-transactions"))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("조회된 재고 변동 내역이 없습니다")))
                .andExpect(content().string(Matchers.containsString("냉장 보관실")));
    }

    @Test
    @DisplayName("목록에 없는 구역을 요청하면 첫 번째 구역으로 되돌린다.")
    void stockTransactions_WhenUnknownZone_FallsBackToFirst() throws Exception {
        givenStoragesAndZones();
        givenTransactions();

        mockMvc.perform(get("/stock-transactions").param("zone-id", "999"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("selectedZoneId", 1L));
    }

    @Test
    @DisplayName("검색 조건이 화면에 유지된다.")
    void stockTransactions_KeepsSearchCondition() throws Exception {
        givenStoragesAndZones();
        givenTransactions();

        mockMvc.perform(get("/stock-transactions")
                        .param("medicineName", "타이레놀")
                        .param("transactionType", "DISPOSAL")
                        .param("startDate", "2026-08-10")
                        .param("endDate", "2026-08-16"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("medicineName", "타이레놀"))
                .andExpect(model().attribute("transactionType", TransactionType.DISPOSAL))
                .andExpect(model().attribute("startDate", LocalDate.of(2026, 8, 10)))
                .andExpect(content().string(Matchers.containsString("타이레놀")));
    }

    private void givenStoragesAndZones() {
        given(storageApiClient.getStorages()).willReturn(storages());
        given(zoneApiClient.getZones(anyLong())).willReturn(zones());
    }

    private void givenTransactions() {
        given(stockTransactionApiClient.search(
                eq(1L), any(), any(), any(), any(), anyInt(), anyInt()))
                .willReturn(new PageResponse<>(transactions(), 0, 20, 2L, 1, true));
    }

    private List<StorageInfoResponse> storages() {
        return List.of(new StorageInfoResponse(1L, 1L, "테스트 조직", "본원 창고", StorageStatus.ACTIVE));
    }

    private List<ZoneInfoResponse> zones() {
        return List.of(
                new ZoneInfoResponse(1L, 1L, "냉장 보관실", ZoneStatus.ACTIVE, EnvStatus.NORMAL),
                new ZoneInfoResponse(2L, 1L, "실온 보관실", ZoneStatus.ACTIVE, EnvStatus.NORMAL));
    }

    private List<StockTransactionResponse> transactions() {
        return List.of(
                new StockTransactionResponse(
                        1L, "타이레놀정", "10정/PTP", TransactionType.INBOUND, 120,
                        "DISPENSING", "8월 정기 발주분",
                        UUID.randomUUID(), "김약사", LocalDateTime.of(2026, 8, 10, 9, 20)),
                new StockTransactionResponse(
                        2L, "아모크라정", "20정/병", TransactionType.DISPOSAL, 8,
                        "EXPIRED", null,
                        UUID.randomUUID(), null, LocalDateTime.of(2026, 8, 12, 17, 30)));
    }
}
