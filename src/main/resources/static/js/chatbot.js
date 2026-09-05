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

    trigger.addEventListener('click', () => togglePanel(!panel.classList.contains('is-open')));
    close.addEventListener('click', () => togglePanel(false));

    form.addEventListener('submit', async (event) => {
        event.preventDefault();
        const question = input.value.trim();
        if (!question) {
            return;
        }

        addMessage(question, 'user');
        input.value = '';
        try {
            const response = await fetch('/chatbot', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({message: question})
            });
            const contentType = response.headers.get('content-type') || '';
            if (!contentType.includes('application/json')) {
                throw new Error('서버 연결에 실패했습니다.');
            }
            const body = await response.json();
            if (!response.ok || !body.message) {
                throw new Error('챗봇 응답을 받을 수 없습니다.');
            }
            addMessage(body.message, 'assistant');
        } catch (error) {
            addMessage(error.message || '챗봇 연결 중 오류가 발생했습니다.', 'assistant');
        }
    });

    document.querySelectorAll('.chatbot-suggestions button').forEach((button) => {
        button.addEventListener('click', () => {
            input.value = button.textContent;
            form.requestSubmit();
        });
    });
})();
