const GATEWAY_URL = 'https://iunot.cloud';

// 임계값 삭제 API 호출
function deleteZoneThreshold(zoneId, zoneThresholdId) {
    if (!confirm('정말 이 임계값 설정을 삭제하시겠습니까?')) {
        return;
    }

    // API 명세에 맞는 엔드포인트로 DELETE 요청
    fetch(`${GATEWAY_URL}/api/core/zones/${zoneId}/zone-thresholds/${zoneThresholdId}`, {
        method: 'DELETE',
        credentials: 'include'
    })
        .then(response => {
            if (response.ok || response.status === 204) {
                alert('임계값 설정이 삭제되었습니다.');
                // 삭제 성공 시 구역 상세 페이지(또는 이전 페이지)로 이동
                location.href = `/storages/zones/${zoneId}`; // 필요에 따라 경로 조정 가능
            } else {
                return response.json().then(err => {
                    alert(err.message || '임계값 삭제에 실패했습니다.');
                }).catch(() => {
                    alert('임계값 삭제에 실패했습니다.');
                });
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('서버 통신 중 오류가 발생했습니다.');
        });
}