function fetchUnreadAlertCount() {
    fetch(`/api/core/alerts/unread-count`, {
        credentials: 'include'
    })
        .then(res => {
            if (!res.ok) throw new Error('알림 개수 조회 실패');
            return res.json();
        })
        .then(resData => {
            const count = resData.data || 0;
            const badge = document.getElementById('unread-notification-badge');

            if (!badge) return; // 배지 요소가 없는 페이지일 경우 방어 코드

            if (count > 0) {
                badge.innerText = count > 99 ? '99+' : count;
                badge.style.display = 'inline-block';
            } else {
                badge.style.display = 'none';
            }
        })
        .catch(err => {
            console.debug('알림 개수 연동 중단 또는 에러:', err);
        });
}

// 페이지가 로드되자마자 즉시 한번 조회 후 1분마다 반복
document.addEventListener('DOMContentLoaded', () => {
    fetchUnreadAlertCount();
    setInterval(fetchUnreadAlertCount, 60000);
});