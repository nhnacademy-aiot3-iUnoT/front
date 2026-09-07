(() => {
    const POLL_INTERVAL_MS = 30000;

    const trigger = document.getElementById('chatbot-trigger');
    const panel = document.getElementById('chatbot-panel');
    const container = document.getElementById('chatbot-notes');
    const emptyMessage = document.getElementById('chatbot-notes-empty');
    const tabBadge = document.getElementById('chatbot-notes-badge');
    const tabs = document.querySelectorAll('.chatbot-tab');

    if (!trigger || !panel || !container) {
        return;
    }

    // /chatbot/notes 는 프론트 자체 컨트롤러라 상대경로로 부른다.
    // apiFetch 는 게이트웨이 주소를 앞에 붙이므로 여기서는 쓰면 안 된다.
    const rendered = new Set();
    let triggerBadge = null;

    // 프론트 라우트가 저장소 하위로 중첩돼 있어 storageId 가 함께 필요하다.
    const ROUTES = {
        PACK_UNIT: (note) => `/storages/${note.targetStorageId}/pack-units/${note.targetId}`,
        ZONE: (note) => `/storages/${note.targetStorageId}/zones/${note.targetId}/sensorInfo`,
        STORAGE: (note) => `/storages/${note.targetId}`,
    };

    const isPanelOpen = () => panel.classList.contains('is-open');
    const isNotesTabActive = () =>
        document.querySelector('.chatbot-tab[data-tab="notes"]')?.classList.contains('is-active');

    const showTab = (name) => {
        tabs.forEach((tab) => tab.classList.toggle('is-active', tab.dataset.tab === name));
        panel.querySelectorAll('[data-panel]').forEach((element) => {
            element.hidden = element.dataset.panel !== name;
        });

        if (name === 'notes') {
            markVisibleRead();
        }
    };

    const updateBadges = (count) => {
        if (tabBadge) {
            tabBadge.textContent = count > 99 ? '99+' : String(count);
            tabBadge.hidden = count === 0;
        }

        if (count > 0 && !triggerBadge) {
            triggerBadge = document.createElement('span');
            triggerBadge.className = 'chatbot-badge';
            trigger.appendChild(triggerBadge);
        }

        if (triggerBadge) {
            triggerBadge.textContent = count > 99 ? '99+' : String(count);
            triggerBadge.hidden = count === 0;
        }
    };

    const hrefOf = (note) => {
        const route = ROUTES[note.targetType];

        if (!route || !note.targetId || !note.targetStorageId) {
            return null;
        }

        return route(note);
    };

    const SEVERITY_LABELS = {CRITICAL: '조치 필요', WARN: '주의', INFO: '참고'};
    const OPERATION_LABELS = {INBOUND: '입고', OUTBOUND: '출고'};

    const CATEGORY_LABELS = {
        STORAGE_CONDITION: '보관 조건',
        EXPIRY_ORDER: '유통기한',
        EXPIRING_STOCK: '유통기한',
        SCATTERED_STORAGE: '보관 위치',
        LOW_STOCK: '재고 수량',
    };

    const chip = (className, text) => {
        const element = document.createElement('span');
        element.className = className;
        element.textContent = text;

        return element;
    };

    const renderNote = (note) => {
        const href = hrefOf(note);
        const severity = (note.severity || 'INFO').toUpperCase();

        const card = document.createElement('div');
        card.className = `chatbot-note severity-${severity.toLowerCase()}`;
        card.dataset.noteId = note.noteId;

        const header = document.createElement('div');
        header.className = 'chatbot-note-header';
        header.appendChild(chip('chatbot-note-operation', OPERATION_LABELS[note.operation] || '점검'));
        header.appendChild(chip('chatbot-note-badge', SEVERITY_LABELS[severity] || '참고'));
        header.appendChild(chip('chatbot-note-category', CATEGORY_LABELS[note.findingType] || '재고 점검'));
        header.appendChild(chip('chatbot-note-time', formatTime(note.createdAt)));
        card.appendChild(header);

        // 품목 정보는 서버가 준 값을 그대로 그린다. LLM 을 거치지 않은 부분이다.
        const subject = document.createElement(href ? 'a' : 'div');
        subject.className = 'chatbot-note-subject';
        subject.appendChild(chip('chatbot-note-subject-title', note.subject || ''));

        if (note.subjectDetail) {
            subject.appendChild(chip('chatbot-note-subject-detail', note.subjectDetail));
        }

        if (href) {
            subject.href = href;
            subject.appendChild(chip('chatbot-note-subject-go', '재고 상세 보기 →'));
        }

        card.appendChild(subject);

        const body = document.createElement('p');
        body.className = 'chatbot-note-body';
        body.textContent = note.message;
        card.appendChild(body);

        // 최신 알림이 위로 오게 한다.
        container.insertBefore(card, container.firstChild);
    };

    const formatTime = (createdAt) => {
        if (!createdAt) {
            return '';
        }

        return String(createdAt).replace('T', ' ').slice(5, 16);
    };

    const markRead = async (noteId) => {
        try {
            await fetch(`/chatbot/notes/${noteId}/read`, {method: 'POST'});
        } catch (error) {
            // 다음 폴링에서 다시 시도된다.
        }
    };

    const markVisibleRead = () => {
        updateBadges(0);
        container.querySelectorAll('.chatbot-note').forEach((note) => markRead(note.dataset.noteId));
    };

    const poll = async () => {
        try {
            const response = await fetch('/chatbot/notes?unread=false');

            if (!response.ok) {
                return;
            }

            const body = await response.json();
            const notes = body.notes || [];

            notes.filter((note) => !rendered.has(note.noteId)).forEach((note) => {
                rendered.add(note.noteId);
                renderNote(note);
            });

            if (emptyMessage) {
                emptyMessage.hidden = notes.length > 0;
            }

            if (isPanelOpen() && isNotesTabActive()) {
                markVisibleRead();
            } else {
                updateBadges(body.unreadCount || 0);
            }
        } catch (error) {
            // 네트워크 오류로 폴링이 멈추면 안 된다.
        }
    };

    tabs.forEach((tab) => tab.addEventListener('click', () => showTab(tab.dataset.tab)));

    poll();
    setInterval(poll, POLL_INTERVAL_MS);
})();
