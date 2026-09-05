document.addEventListener('DOMContentLoaded', () => {
    const recipientInput = document.querySelector('#telegram-recipient');
    const enabledInput = document.querySelector('#telegram-enabled');
    const saveButton = document.querySelector('#channel-save');

    async function errorMessage(response, fallback) {
        try {
            const body = await response.json();
            return body.message || fallback;
        } catch (error) {
            return fallback;
        }
    }

    if (saveButton) {
        saveButton.addEventListener('click', async () => {
            const recipient = recipientInput.value.trim();

            recipientInput.classList.remove('is-invalid');
            // 켜두고 chat id가 비면 서버가 거절하므로 미리 막는다.
            if (enabledInput.checked && !recipient) {
                recipientInput.classList.add('is-invalid');
                recipientInput.parentElement.querySelector('.invalid-feedback').textContent =
                    '알림을 사용하려면 chat id를 입력해주세요.';
                return;
            }

            saveButton.disabled = true;
            try {
                const response = await fetch('/mypage/notifications/channels', {
                    method: 'PUT',
                    headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify({recipient, enabled: enabledInput.checked})
                });

                if (!response.ok) {
                    alert(await errorMessage(response, '알림 채널 저장에 실패했습니다.'));
                    return;
                }

                alert('저장되었습니다.');
            } finally {
                saveButton.disabled = false;
            }
        });
    }

    // 저장소 스위치: 그 저장소에 속한 구역까지 전부 같은 값이 된다.
    document.querySelectorAll('[data-storage-toggle]').forEach((toggle) => {
        toggle.addEventListener('change', async () => {
            const storageId = Number(toggle.dataset.storageId);
            const enabled = toggle.checked;
            const explicitZoneIds = (toggle.dataset.explicitZoneIds || '')
                .split(',')
                .filter((id) => id.length > 0)
                .map(Number);

            toggle.disabled = true;
            try {
                const response = await fetch(`/mypage/notifications/scopes/storages/${storageId}`, {
                    method: 'PUT',
                    headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify({enabled, zoneIds: explicitZoneIds})
                });

                if (!response.ok) {
                    toggle.checked = !enabled;
                    alert(await errorMessage(response, '알림 범위 저장에 실패했습니다.'));
                    return;
                }

                // 구역 스위치들도 같이 바뀌므로 다시 그린다.
                window.location.reload();
            } catch (error) {
                toggle.checked = !enabled;
                alert('알림 범위 저장에 실패했습니다.');
            } finally {
                toggle.disabled = false;
            }
        });
    });

    // 구역 스위치: 그 구역만 바뀐다.
    document.querySelectorAll('[data-zone-toggle]').forEach((toggle) => {
        toggle.addEventListener('change', async () => {
            const storageId = Number(toggle.dataset.storageId);
            const zoneId = Number(toggle.dataset.zoneId);
            const enabled = toggle.checked;

            toggle.disabled = true;
            try {
                const response = await fetch('/mypage/notifications/scopes', {
                    method: 'PUT',
                    headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify({storageId, zoneId, enabled})
                });

                if (!response.ok) {
                    // 실패하면 스위치를 원래대로 되돌린다. 화면과 서버 상태가 어긋나면 안 된다.
                    toggle.checked = !enabled;
                    alert(await errorMessage(response, '알림 범위 저장에 실패했습니다.'));
                }
            } catch (error) {
                toggle.checked = !enabled;
                alert('알림 범위 저장에 실패했습니다.');
            } finally {
                toggle.disabled = false;
            }
        });
    });
});
