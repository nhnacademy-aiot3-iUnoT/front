(() => {
    const examples = {
        expiry: {
            question: '30일 이내에 유통기한이 끝나는 재고 알려줘',
            answer: '30일 이내 만료 예정인 재고를 확인했어요. 의약품과 보관 위치를 함께 살펴보세요.',
            category: '유통기한 임박 재고',
            result: '예시 의약품 A · 냉장 저장소',
            detail: '남은 유통기한 14일 · 현재 수량 24개'
        },
        stock: {
            question: '부족한 의약품 재고가 있는지 확인해줘',
            answer: '재고 기준보다 수량이 부족한 품목을 확인했어요. 현재 수량을 살펴보고 보충을 준비해 보세요.',
            category: '부족 재고 확인',
            result: '예시 의약품 B · 일반 저장소',
            detail: '현재 수량 12개 · 재고 기준 30개'
        },
        order: {
            question: '다음 주에 어떤 의약품을 발주해야 해?',
            answer: '최근 출고 이력을 바탕으로 다음 주 발주가 필요한 품목과 권장 수량을 살펴봤어요.',
            category: '발주 추천',
            result: '예시 의약품 C · 권장 발주 40개',
            detail: '최근 4주 출고 이력 기반 · 발주 준비를 위한 참고 수량'
        }
    };
    const buttons = document.querySelectorAll('[data-landing-chat]');
    buttons.forEach((button) => {
        button.addEventListener('click', () => {
            const example = examples[button.dataset.landingChat];
            if (!example) return;
            Object.entries(example).forEach(([field, value]) => {
                const target = document.getElementById(`landing-chat-${field}`);
                if (target) target.textContent = value;
            });
            buttons.forEach((item) => item.setAttribute('aria-pressed', String(item === button)));
        });
    });
})();
