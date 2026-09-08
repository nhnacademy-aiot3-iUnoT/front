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

    const showTab = (name) => {
        tabs.forEach((tab) => tab.classList.toggle('is-active', tab.dataset.tab === name));
        panel.querySelectorAll('[data-panel]').forEach((element) => {
            element.hidden = element.dataset.panel !== name;
        });

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

        const dismiss = document.createElement('button');
        dismiss.type = 'button';
        dismiss.className = 'chatbot-note-dismiss';
        dismiss.setAttribute('aria-label', '알림 확인');
        dismiss.textContent = '\u00d7';
        dismiss.addEventListener('click', () => markRead(card));
        card.appendChild(dismiss);

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

    // 서버 처리가 실패하면 카드를 남겨 사용자가 다시 시도할 수 있게 한다.
    const markRead = async (card) => {
        try {
            const response = await fetch(`/chatbot/notes/${card.dataset.noteId}/read`, {method: 'POST'});

            if (response.ok) {
                removeCard(card);
            }
        } catch (error) {
            // 카드를 그대로 두는 것으로 충분하다.
        }
    };

    const removeCard = (card) => {
        rendered.delete(Number(card.dataset.noteId));
        card.remove();

        const remaining = container.querySelectorAll('.chatbot-note').length;

        updateBadges(remaining);

        if (emptyMessage) {
            emptyMessage.hidden = remaining > 0;
        }
    };

    const poll = async () => {
        try {
            const response = await fetch('/chatbot/notes?unread=true');

            if (!response.ok) {
                return;
            }

            const body = await response.json();
            const notes = body.notes || [];

            notes.filter((note) => !rendered.has(note.noteId)).forEach((note) => {
                rendered.add(note.noteId);
                renderNote(note);
            });

            // 다른 화면에서 확인된 알림은 응답에 없으므로 여기서도 치운다.
            const ids = new Set(notes.map((note) => note.noteId));

            container.querySelectorAll('.chatbot-note').forEach((card) => {
                if (!ids.has(Number(card.dataset.noteId))) {
                    removeCard(card);
                }
            });

            updateBadges(notes.length);

            if (emptyMessage) {
                emptyMessage.hidden = notes.length > 0;
            }
        } catch (error) {
            // 네트워크 오류로 폴링이 멈추면 안 된다.
        }
    };

    tabs.forEach((tab) => tab.addEventListener('click', () => showTab(tab.dataset.tab)));

    poll();
    setInterval(poll, POLL_INTERVAL_MS);
})();
