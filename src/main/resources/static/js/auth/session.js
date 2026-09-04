const sessionControls = document.getElementById("session-controls");

if (sessionControls) {
    const timer = document.getElementById("session-timer");
    const extendButton = document.getElementById("session-extend-button");
    const extensionStatus = document.getElementById("session-extension-status");
    const logoutForm = document.getElementById("session-logout-form");
    const expirationStorageKey = "iunot.session.access-token-expires-at";
    const configuredDuration = Number(sessionControls.dataset.durationSeconds);
    const durationSeconds = Number.isFinite(configuredDuration) && configuredDuration > 0
        ? configuredDuration
        : 30 * 60;
    const durationMilliseconds = durationSeconds * 1000;
    const configuredExpiration = Number(sessionControls.dataset.expiresAt);
    let expiresAt = Number.isFinite(configuredExpiration) && configuredExpiration > 0
        ? configuredExpiration
        : Date.now() + durationMilliseconds;
    let extensionInProgress = false;
    let logoutStarted = false;

    function readSharedExpiration() {
        try {
            const sharedExpiration = Number(localStorage.getItem(expirationStorageKey));

            return Number.isFinite(sharedExpiration) && sharedExpiration > Date.now()
                ? sharedExpiration
                : null;
        } catch {
            return null;
        }
    }

    function publishExpiration() {
        try {
            localStorage.setItem(expirationStorageKey, String(expiresAt));
        } catch {
            // 저장소를 사용할 수 없으면 현재 탭의 타이머만 유지한다.
        }
    }

    function clearSharedExpiration() {
        try {
            localStorage.removeItem(expirationStorageKey);
        } catch {
            // 저장소를 사용할 수 없더라도 로그아웃은 계속 진행한다.
        }
    }

    function applySharedExpiration(sharedExpiration) {
        if (!Number.isFinite(sharedExpiration) || sharedExpiration <= expiresAt) {
            return;
        }

        expiresAt = sharedExpiration;
        extensionStatus.textContent = "다른 탭에서 로그인 시간이 연장되었습니다.";
        renderTimer();
    }

    function remainingSeconds() {
        return Math.max(0, Math.ceil((expiresAt - Date.now()) / 1000));
    }

    function renderTimer() {
        const remaining = remainingSeconds();
        const minutes = Math.floor(remaining / 60);
        const seconds = remaining % 60;

        timer.textContent = `${String(minutes).padStart(2, "0")}:${String(seconds).padStart(2, "0")}`;
        timer.classList.toggle("text-danger", remaining <= 60);

        if (remaining === 0 && !extensionInProgress) {
            forceLogout();
        }
    }

    function forceLogout() {
        if (logoutStarted) {
            return;
        }

        logoutStarted = true;
        extendButton.disabled = true;
        extensionStatus.textContent = "로그인 시간이 만료되어 로그아웃합니다.";

        if (logoutForm) {
            logoutForm.requestSubmit();
            return;
        }

        window.location.assign(sessionControls.dataset.loginUrl);
    }

    async function extendSession() {
        extensionInProgress = true;
        extendButton.disabled = true;
        extensionStatus.textContent = "로그인 연장 중입니다.";

        try {
            const response = await fetch(sessionControls.dataset.refreshUrl, {
                method: "POST",
                credentials: "same-origin",
                headers: {
                    "Accept": "application/json"
                }
            });

            if (response.status === 401) {
                forceLogout();
                return;
            }

            if (!response.ok) {
                throw new Error("Session refresh failed");
            }

            expiresAt = Date.now() + durationMilliseconds;
            publishExpiration();
            extensionStatus.textContent = "로그인 시간이 연장되었습니다.";
            renderTimer();
        } catch (error) {
            extensionStatus.textContent = "로그인 연장에 실패했습니다.";
            alert("로그인 연장에 실패했습니다. 잠시 후 다시 시도해주세요.");
        } finally {
            extensionInProgress = false;

            if (!logoutStarted) {
                extendButton.disabled = false;
                renderTimer();
            }
        }
    }

    const sharedExpiration = readSharedExpiration();

    if (sharedExpiration !== null && sharedExpiration > expiresAt) {
        applySharedExpiration(sharedExpiration);
    } else {
        publishExpiration();
    }

    window.addEventListener("storage", event => {
        if (event.key !== expirationStorageKey || event.newValue === null) {
            return;
        }

        applySharedExpiration(Number(event.newValue));
    });

    logoutForm?.addEventListener("submit", clearSharedExpiration);
    extendButton.addEventListener("click", extendSession);
    renderTimer();
    window.setInterval(renderTimer, 1000);
}
