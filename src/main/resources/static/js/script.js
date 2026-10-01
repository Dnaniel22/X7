document.addEventListener("DOMContentLoaded", async function () {
  const menu = document.getElementById("authMenu");

  if (menu && typeof X7 !== "undefined") {
    await X7.renderAuthMenu(menu);
  }
});
