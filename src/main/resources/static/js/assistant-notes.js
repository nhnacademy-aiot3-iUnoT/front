(() => {
    const POLL_INTERVAL_MS = 30000;

    const trigger = document.getElementById('chatbot-trigger');
    const panel = document.getElementById('chatbot-panel');
    const messages = document.getElementById('chatbot-messages');

    if (!trigger || !panel || !messages) {
        return;
    }

    // 페이지를 이동하면 패널이 새로 그려지므로 읽은 것까지 최근 알림을 다시 받아 복원한다.
    // 이 Set 은 한 페이지 안에서 폴링이 같은 알림을 중복해 그리는 것만 막는다.
    const rendered = new Set();
    let badge = null;

    const isPanelOpen = () => panel.classList.contains('is-open');

    const updateBadge = (count) => {
        if (count > 0 && !badge) {
            badge = document.createElement('span');
            badge.className = 'chatbot-badge';
            trigger.appendChild(badge);
        }

        if (!badge) {
            return;
        }

        badge.textContent = count > 99 ? '99+' : String(count);
        badge.hidden = count === 0;
    };

    const renderNote = (note) => {
        const element = document.createElement('div');
        element.className = `chatbot-message assistant chatbot-note severity-${(note.severity || 'INFO').toLowerCase()}`;
        element.dataset.noteId = note.noteId;
        element.textContent = note.message;

        messages.appendChild(element);
        messages.scrollTop = messages.scrollHeight;
    };

    const markRead = async (noteId) => {
        try {
            await fetch(`/chatbot/notes/${noteId}/read`, {method: 'POST'});
        } catch (error) {
            // 읽음 처리 실패는 다음 폴링에서 다시 시도된다. 사용자에게 알릴 일은 아니다.
        }
    };

    const poll = async () => {
        try {
            const response = await fetch('/chatbot/notes?unread=false');

            if (!response.ok) {
                return;
            }

            const body = await response.json();
            const notes = body.notes || [];
            const fresh = notes.filter((note) => !rendered.has(note.noteId));

            fresh.reverse().forEach((note) => {
                rendered.add(note.noteId);
                renderNote(note);

                // 패널이 열려 있으면 사용자가 실제로 본 것이므로 바로 읽음 처리한다.
                if (isPanelOpen()) {
                    markRead(note.noteId);
                }
            });

            updateBadge(isPanelOpen() ? 0 : body.unreadCount || 0);
        } catch (error) {
            // 네트워크 오류로 폴링이 멈추면 안 된다.
        }
    };

    trigger.addEventListener('click', () => {
        if (!isPanelOpen()) {
            return;
        }

        updateBadge(0);
        messages.querySelectorAll('.chatbot-note').forEach((note) => markRead(note.dataset.noteId));
    });

    poll();
    setInterval(poll, POLL_INTERVAL_MS);
})();
