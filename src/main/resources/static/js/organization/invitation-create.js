document.addEventListener("DOMContentLoaded", function () {
    const form = document.querySelector("#invitationForm");

    if(!form) {
        return;
    }

    form.addEventListener("submit", function (event) {
        clearErrors(form);

        const email = form.querySelector("#email");

        const emailValid = validateEmailField(email);

        if(!emailValid) {
            event.preventDefault();
        }
    })
})
