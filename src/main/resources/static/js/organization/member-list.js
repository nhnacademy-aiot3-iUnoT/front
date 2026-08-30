// 역할 변경 (누르면 바로)
document.querySelectorAll(".member-role-select").forEach((roleSelect) => {
    roleSelect.addEventListener("change", () => {
        roleSelect.form.submit();
    });
});

// 부서 배정
const assignForm = document.getElementById("assign-department-form");
const assignError = document.getElementById("assign-department-error");

assignForm?.addEventListener("submit", (event) => {
    const selectedDepartments = assignForm.querySelectorAll(
        ".member-department-checkbox:checked"
    );

    if (selectedDepartments.length === 0) {
        event.preventDefault();
        assignError.textContent = "지정할 부서를 하나 이상 선택해주세요.";
        assignError.classList.add("d-block");
    } else {
        assignError.textContent = "";
        assignError.classList.remove("d-block");
    }
});

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

        if (assignError) {
            assignError.textContent = "";
            assignError.classList.remove("d-block");
        }

        bootstrap.Modal
            .getOrCreateInstance(
                document.getElementById("assign-department-modal")
            )
            .show();
    });
});
