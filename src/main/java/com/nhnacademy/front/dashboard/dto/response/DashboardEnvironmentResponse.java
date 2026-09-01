package com.nhnacademy.front.dashboard.dto.response;

import com.nhnacademy.front.engine.dto.SensorType;
import com.nhnacademy.front.organization.dto.EnvStatus;
import com.nhnacademy.front.organization.dto.StorageStatus;
import com.nhnacademy.front.organization.dto.ZoneStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 저장소 환경 현황. 인벤토리가 임계값과 실측값을 합쳐서 내려준다.
 * 화면에 그대로 뿌릴 수 있도록 표기용 메서드만 함께 둔다.
 */
public record DashboardEnvironmentResponse(
        Long storageId,
        String storageName,
        StorageStatus storageStatus,
        LocalDate summaryDate,
        List<ZoneEnvironmentResponse> zones
) {

    public long countByStatus(EnvStatus status) {
        return zones == null ? 0L : zones.stream()
                .filter(zone -> zone.status() == status)
                .count();
    }

    public record ZoneEnvironmentResponse(
            Long zoneId,
            String name,
            ZoneStatus zoneStatus,
            EnvStatus envStatus,
            List<SensorEnvironmentResponse> sensors,

            // 문 센서가 없거나 측정값이 없으면 null
            Boolean doorOpened
    ) {
        /** 구역 상태는 서버 판정을 그대로 쓴다. 값이 없으면 정상으로 본다. */
        public EnvStatus status() {
            return envStatus == null ? EnvStatus.NORMAL : envStatus;
        }

        public boolean doorMeasured() {
            return doorOpened != null;
        }

        public String doorText() {
            return Boolean.TRUE.equals(doorOpened) ? "열림" : "닫힘";
        }

        public String doorIcon() {
            return "🚪";
        }

        public boolean hasSensor() {
            return (sensors != null && !sensors.isEmpty()) || doorMeasured();
        }

        public long measuredCount() {
            long measured = sensors == null ? 0L : sensors.stream()
                    .filter(SensorEnvironmentResponse::hasValue)
                    .count();

            return doorMeasured() ? measured + 1L : measured;
        }

        public long sensorCount() {
            long count = sensors == null ? 0L : sensors.size();

            return doorMeasured() ? count + 1L : count;
        }
    }

    public record SensorEnvironmentResponse(
            String sensorType,
            String unit,

            // 임계값이 등록되지 않았으면 null
            BigDecimal thresholdMin,
            BigDecimal thresholdMax,

            // 룰엔진의 현재 측정값. 측정 기록이 없으면 null
            Double currentValue,

            // 현재값이 없을 때 대신 보여줄 어제 평균
            Double avgValue,

            boolean sensorRegistered
    ) {
        /** 현재값이 없으면 어제 평균이라도 보여준다. */
        public Double displayValue() {
            return currentValue != null ? currentValue : avgValue;
        }

        public boolean hasValue() {
            return displayValue() != null;
        }

        public boolean hasThreshold() {
            return thresholdMin != null || thresholdMax != null;
        }

        /** 룰엔진 표준 타입이면 한글 이름을, 아니면 원래 이름을 그대로 쓴다. */
        public String label() {
            return SensorType.findByValue(sensorType)
                    .map(SensorType::ko)
                    .orElseGet(() -> sensorType == null || sensorType.isBlank() ? "센서" : sensorType);
        }

        public String icon() {
            return SensorType.findByValue(sensorType)
                    .map(type -> switch (type) {
                        case TEMPERATURE -> "🌡";
                        case HUMIDITY -> "💧";
                        case ILLUMINATION -> "💡";
                        case DOOR -> "🚪";
                    })
                    .orElse("📈");
        }

        public String valueText() {
            Double value = displayValue();

            if (value == null) {
                return "—";
            }

            String text = value % 1 == 0
                    ? String.valueOf(value.longValue())
                    : String.format("%.1f", value);

            return unit == null || unit.isBlank() ? text : text + unit;
        }

        public String rangeText() {
            if (!hasThreshold()) {
                return "임계값 미설정";
            }

            String min = thresholdMin == null ? "제한없음" : trim(thresholdMin);
            String max = thresholdMax == null ? "제한없음" : trim(thresholdMax);

            return "기준 " + min + " ~ " + max + (unit == null ? "" : unit);
        }

        /** 임계 범위 안에서 현재값의 위치(%). 색으로 판정하지 않고 위치만 나타낸다. */
        public int gaugePercent() {
            Double value = displayValue();

            if (value == null || thresholdMin == null || thresholdMax == null) {
                return 0;
            }

            double span = thresholdMax.doubleValue() - thresholdMin.doubleValue();

            if (span <= 0.0) {
                return 0;
            }

            double ratio = (value - thresholdMin.doubleValue()) / span * 100.0;

            return (int) Math.max(4.0, Math.min(100.0, ratio));
        }

        private String trim(BigDecimal value) {
            return value.stripTrailingZeros().toPlainString();
        }
    }
}
