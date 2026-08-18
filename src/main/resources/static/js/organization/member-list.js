// 역할 변경 (누르면 바로)
document.querySelectorAll(".member-role-select").forEach((roleSelect) => {
    roleSelect.addEventListener("change", () => {
        roleSelect.form.submit();
    });
});

// 부서 배정
document.querySelectorAll(".assign-department-button").forEach((button) => {
    button.addEventListener("click", async () => {
        button.disabled = true;
        // 회원 정보
        document.getElementById("assign-member-id").value = button.dataset.memberId;
        document.getElementById("assign-member-email").textContent = button.dataset.memberEmail;

        // 기존 체크박스 초기화
        document.querySelectorAll(".member-department-checkbox").forEach((checkbox) => {
            checkbox.checked = false;
        });

        try {
            // 회원 부서 조회
            const response = await fetch(button.dataset.departmentsUrl);
            if (!response.ok) {
                throw new Error("부서 조회 실패");
            }

            // 회원의 부서 ID 추출
            const departments = await response.json();
            const selectedDepartmentIds = new Set(departments.map((department) => String(department.id)));

            // check 표시
            document.querySelectorAll(".member-department-checkbox").forEach((checkbox) => {
                checkbox.checked = selectedDepartmentIds.has(checkbox.value);
            });

            bootstrap.Modal.getOrCreateInstance(document.getElementById("assign-department-modal")).show();
        } catch (error) {
            alert("부서 정보를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.");
        } finally {
            button.disabled = false;
        }
    });
});
