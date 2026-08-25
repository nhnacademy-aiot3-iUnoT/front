(() => {
    const trigger = document.getElementById('chatbot-trigger');
    const panel = document.getElementById('chatbot-panel');
    const close = document.getElementById('chatbot-close');
    const form = document.getElementById('chatbot-form');
    const input = document.getElementById('chatbot-input');
    const messages = document.getElementById('chatbot-messages');

    if (!trigger || !panel || !close || !form || !input || !messages) {
        return;
    }

    const togglePanel = (isOpen) => {
        panel.classList.toggle('is-open', isOpen);
        panel.setAttribute('aria-hidden', String(!isOpen));
        if (isOpen) {
            input.focus();
        }
    };

    const addMessage = (text, type) => {
        const message = document.createElement('div');
        message.className = `chatbot-message ${type}`;
        message.textContent = text;
        messages.appendChild(message);
        messages.scrollTop = messages.scrollHeight;
    };

    const reply = (question) => {
        if (question.includes('부족')) {
            return '현재 재고 부족 품목은 4개입니다.\n상세 목록은 재고 관리에서 확인할 수 있어요.';
        }

        if (question.includes('출고')) {
            return '최근 출고 내역을 준비 중입니다.\n출고 화면에서 상세 내역을 확인할 수 있어요.';
        }

        return '질문을 확인했습니다. 재고·저장소 관련 기능이 연결되면 더 정확하게 안내해드릴게요.';
    };

    trigger.addEventListener('click', () => togglePanel(!panel.classList.contains('is-open')));
    close.addEventListener('click', () => togglePanel(false));

    form.addEventListener('submit', (event) => {
        event.preventDefault();
        const question = input.value.trim();
        if (!question) {
            return;
        }

        addMessage(question, 'user');
        input.value = '';
        window.setTimeout(() => addMessage(reply(question), 'assistant'), 250);
    });

    document.querySelectorAll('.chatbot-suggestions button').forEach((button) => {
        button.addEventListener('click', () => {
            input.value = button.textContent;
            form.requestSubmit();
        });
    });
})();
