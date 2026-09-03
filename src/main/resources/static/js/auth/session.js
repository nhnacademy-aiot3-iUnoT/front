const sessionControls = document.getElementById("session-controls");

if (sessionControls) {
    const timer = document.getElementById("session-timer");
    const extendButton = document.getElementById("session-extend-button");
    const extensionStatus = document.getElementById("session-extension-status");
    const logoutForm = document.getElementById("session-logout-form");
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

    extendButton.addEventListener("click", extendSession);
    renderTimer();
    window.setInterval(renderTimer, 1000);
}
