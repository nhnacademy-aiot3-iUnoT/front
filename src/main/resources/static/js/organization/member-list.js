// 역할 변경 (누르면 바로)
document.querySelectorAll(".member-role-select").forEach((roleSelect) => {
    roleSelect.addEventListener("change", () => {
        roleSelect.form.submit();
    });
});

// 부서 배정
document.querySelectorAll(".assign-department-button").forEach((button) => {
    button.addEventListener("click", () => {
        const memberId = button.dataset.memberId;
        const assignForm = document.getElementById("assign-department-form");

        assignForm.action = `/organizations/me/members/${memberId}/departments`;

        document.getElementById("assign-member-email").textContent =
            button.dataset.memberEmail;

        document.querySelectorAll(".member-department-checkbox").forEach((checkbox) => {
            checkbox.checked = false;
        });

        bootstrap.Modal
            .getOrCreateInstance(
                document.getElementById("assign-department-modal")
            )
            .show();
    });
});
