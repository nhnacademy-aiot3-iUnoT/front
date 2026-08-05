document.addEventListener("DOMContentLoaded", function () {
    const form = document.querySelector("#orgCreateForm");

    if (!form) {
        return;
    }

    form.addEventListener("submit", function(event) {
        clearErrors(form);

        const businessNumber = form.querySelector("#businessNumber");
        const orgName = form.querySelector("#name");
        const email = form.querySelector("#email");

        const businessValid = validateBusinessNumberField(businessNumber);
        const orgNameValid = validateOrganizationNameField(orgName);
        const emailValid = validateEmailField(email);


        if (!(businessValid && orgNameValid && emailValid)) {
            event.preventDefault();
        }
    });
});
