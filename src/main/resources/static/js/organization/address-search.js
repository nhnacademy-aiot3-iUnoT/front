function goPopup() {
    const width = 570;
    const height = 420;

    const left = Math.floor((window.screen.width - width) / 2);
    const top = Math.floor((window.screen.height - height) / 2);

    window.open(
        "/juso/popup",
        "jusoPopup",
        `width=${width},height=${height},left=${left},top=${top},scrollbars=yes,resizable=yes`
    );
}

// juso-popup에서 호출
function jusoCallBack(roadAddrPart1, addrDetail, zipNo) {
    document.getElementById("zipCode").value = zipNo;
    document.getElementById("roadAddress").value = roadAddrPart1;

    const detailAddress = document.getElementById("addressDetail");
    if (detailAddress) {
        detailAddress.value = addrDetail || "";
    }
}
