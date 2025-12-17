// Load roles for dropdown
fetch("/mini_crm_Web_exploded/user-add.action")
    .then(res => res.json())
    .then(res => {
        if (!res.success) {
            alert(res.message);
            return;
        }

        const roles = res.data;
        const dropdown = document.getElementById("roleId");

        dropdown.innerHTML = "";

        Object.keys(roles).forEach(roleId => {
            dropdown.innerHTML += `<option value="${roleId}">${roles[roleId]}</option>`;
        });
    });

document.getElementById("createForm").addEventListener("submit", function (e) {
    e.preventDefault();

    const data = new URLSearchParams();
    data.append("username", document.getElementById("username").value);
    data.append("email", document.getElementById("email").value);
    data.append("password", document.getElementById("password").value);
    data.append("fullName", document.getElementById("fullName").value);
    data.append("roleId", document.getElementById("roleId").value);

    fetch("/mini_crm_Web_exploded/user-save.action", {
        method: "POST",
        body: data
    })
        .then(res => res.json())
        .then(res => {
            alert(res.message);
            if (res.success) window.location.href = "list.html";
        });
});
