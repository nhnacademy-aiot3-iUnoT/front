// 화면에 에러 출력, 기존 에러 메시지 삭제 담당

function setError(input, message) {

    input.classList.add("is-invalid");

    const feedback = input
        .closest(".mb-3")
        .querySelector(".invalid-feedback");

    if (feedback) {
        feedback.textContent = message;
    }
}


function clearErrors(form) {

    form.querySelectorAll(".form-control")
        .forEach(input => {
            input.classList.remove("is-invalid");
        });


    form.querySelectorAll(".invalid-feedback")
        .forEach(feedback => {
            feedback.textContent = "";
        });
}
