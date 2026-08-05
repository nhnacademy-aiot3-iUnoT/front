document.addEventListener("DOMContentLoaded", function () {
   const form = document.querySelector("#orgSetupForm");

   if(!form) {
       return;
   }

   form.addEventListener("submit", function (event) {
       clearErrors(form);

       const zipCode = form.querySelector("#zipCode");
       const roadAddress = form.querySelector("#roadAddress");
       const addressDetail = form.querySelector("#addressDetail");
       const description = form.querySelector("#description");

       const zipCodeValid = validateZipCodeField(zipCode);
       const roadAddressValid = validateRoadAddress(roadAddress);
       const addressDetailValid = validateAddressDetail(addressDetail);
       const descriptionValid = validateDescription(description);

        if(!(zipCodeValid && roadAddressValid && addressDetailValid && descriptionValid)) {
            event.preventDefault();
        }
   });
});
