package com.nhnacademy.front.dashboard.dto.response;

/**
 * 대시보드 상단 KPI 4장. 네 장이 모두 같은 모양이라 지표 하나로 통일한다.
 */
public record DashboardSummaryResponse(
        MetricResponse inbound,
        MetricResponse outbound,
        MetricResponse expiring,
        MetricResponse envAlert
) {
    /**
     * KPI 카드 한 장.
     *
     * @param value    큰 숫자
     * @param subValue 숫자 옆 보조 수치 (거래 건수, 미확인 알림 수 …)
     * @param diff     전일 대비 증감
     * @param diffRate 전일 대비 증감률(%). 전일 값이 0이면 null
     */
    public record MetricResponse(
            long value,
            long subValue,
            long diff,
            Double diffRate
    ) {
        public boolean isUp() {
            return diff > 0L;
        }

        public boolean isDown() {
            return diff < 0L;
        }

        public boolean isFlat() {
            return diff == 0L;
        }

        /** 화면에는 부호 없이 절대값만 찍고 방향은 화살표로 보여준다 */
        public long absDiff() {
            return Math.abs(diff);
        }

        public boolean hasRate() {
            return diffRate != null;
        }

        public String rateText() {
            return diffRate == null ? "" : String.format("%.1f%%", Math.abs(diffRate));
        }
    }
}
