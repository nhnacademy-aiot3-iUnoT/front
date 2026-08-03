document.addEventListener("DOMContentLoaded", function () {

    const form = document.querySelector("form");

    if (!form) {
        return;
    }

    form.addEventListener("submit", function (event) {

        let valid = true;

        clearErrors();

        const businessNumber = document.getElementById("businessNumber");
        const orgName = document.getElementById("orgName");
        const email = document.getElementById("email");


        // 사업자번호
        if (!businessNumber.value.trim()) {
            setError(
                businessNumber,
                "사업자 번호는 필수 입력입니다."
            );

            valid = false;
        } else if (!/^\d{10}$/.test(businessNumber.value)) {
            setError(
                businessNumber,
                "사업자 번호는 숫자 10자리입니다."
            );
            valid = false;
        }

        // 조직명
        if (!orgName.value.trim()) {
            setError(
                orgName,
                "조직명은 필수 입력입니다."
            );
            valid = false;
        } else if (orgName.value.length > 50) {
            setError(
                orgName,
                "조직명은 50자 이내입니다."
            );
            valid = false;
        }

        // 이메일
        if (!email.value.trim()) {
            setError(
                email,
                "이메일은 필수 입력입니다."
            );
            valid = false;
        } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.value)) {
            setError(
                email,
                "올바른 이메일 형식이 아닙니다."
            );
            valid = false;
        }

        if (!valid) {
            event.preventDefault();
        }
    });

    function setError(input, message) {
        input.classList.add("is-invalid");
        input.nextElementSibling.textContent = message;
    }


    function clearErrors() {
        document
            .querySelectorAll(".form-control")
            .forEach(input => {

                input.classList.remove("is-invalid");

            });

        document
            .querySelectorAll(".invalid-feedback")
            .forEach(error => {
                error.textContent = "";
            });
    }
});
