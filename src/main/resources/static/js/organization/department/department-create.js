document.addEventListener("DOMContentLoaded", function () {
    const form = document.querySelector("#departmentCreateForm");

    if(!form) {
        return;
    }

    form.addEventListener("submit", function (event) {
        clearErrors(form);

        const departmentName = form.querySelector("#name");
        const description = form.querySelector("#description");

        const nameValid = validateDepartmentNameField(departmentName);
        const descValid = validateDescription(description);

        if(!(nameValid && descValid)) {
            event.preventDefault();
        }
    });
});
