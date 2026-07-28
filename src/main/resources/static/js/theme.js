// dark, light mode
const html = document.documentElement;
const button = document.getElementById("theme-toggle");

button?.addEventListener("click", () => {

    const next =
        html.getAttribute("data-bs-theme") === "dark"
            ? "light"
            : "dark";

    html.setAttribute("data-bs-theme", next);

    localStorage.setItem("theme", next);

});
