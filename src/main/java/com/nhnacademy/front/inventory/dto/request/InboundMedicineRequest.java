package com.nhnacademy.front.inventory.dto.request;

import com.nhnacademy.front.inventory.dto.TransactionType;
import com.nhnacademy.front.medicine.dto.request.MedicineEnvironmentRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InboundMedicineRequest {

    @NotNull(message = "의약품을 선택해주세요.")
    private Long medicinePackageUnitId;

    @NotNull(message = "보관 구역을 선택해주세요.")
    private Long zoneId;

    @NotBlank(message = "제조번호를 입력해주세요.")
    @Size(
            min = 5,
            max = 50,
            message = "제조번호는 5자에서 50자 이하여야 합니다."
    )
    @Pattern(
            regexp = "^\\S+$",
            message = "제조번호에는 공백을 입력할 수 없습니다."
    )
    private String lotNumber;

    @NotNull(message = "유통기한을 입력해주세요.")
    @FutureOrPresent(
            message = "실제 유통기한은 현재 날짜 이후여야 합니다."
    )
    private LocalDate expirationDate;

    @NotNull(message = "입고 수량을 입력해주세요.")
    @Positive(message = "수량은 양수여야 합니다.")
    private Integer quantity;

    @NotNull(message = "입고 유형을 선택해주세요.")
    private TransactionType transactionType;
    private boolean overwriteExpirationDate;

    @Size(
            max = 255,
            message = "메모는 255자 이하여야 합니다."
    )
    private String memo;



    public static InboundMedicineRequest from(Long packageUnitId,Long zoneId) {

        InboundMedicineRequest request = new InboundMedicineRequest();

        request.setMedicinePackageUnitId(packageUnitId);
        request.setZoneId(zoneId);

        return request;
    }



}
