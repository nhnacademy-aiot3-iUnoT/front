package com.nhnacademy.front.inventory.dto.request;

import com.nhnacademy.front.medicine.dto.request.MedicineEnvironmentRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record InboundMedicineRequest(


        @NotNull(message = "의약품을 선택해주세요.")
        Long medicinePackageUnitId,
        @NotNull(message = "보관 구역을 선택해주세요.")
        Long zoneId,
        @NotBlank(message = "제조번호를 입력해주세요.")
        @Size(max=50, message = "제조번호는 50자 이하여야 합니다.")
        String lotNumber,
        @NotNull(message = "유통기한을 입력해주세요.")
        @FutureOrPresent(message = "실제 유통기한은 현재 날짜 이후여야 합니다.")
        LocalDate expirationDate,
        @NotNull(message = "입고 수량을 입력해주세요.")
        @Positive(message = "수량은 양수여야 합니다.")
        Integer quantity,
        @Valid
        MedicineEnvironmentRequest medicineEnvironmentRequest





) {

        public static InboundMedicineRequest from(Long packageUnitId,Long zoneId){

                return new InboundMedicineRequest(
                        packageUnitId,
                        zoneId,
                        null,
                        null,
                        null,
                        new MedicineEnvironmentRequest(
                                null,
                                null,
                                null,
                                null,
                                null,
                                null
                        )
                );

        }

}
