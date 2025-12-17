const params = new URLSearchParams(window.location.search);
const roleId = params.get("roleId") || params.get("id");

if (!roleId) {
    alert("Role ID is required");
    history.back();
}

let assignedPermissions = new Set();

document.addEventListener("DOMContentLoaded", () => {
    loadRole();
});

function loadRole() {
    fetch(`/mini_crm_Web_exploded/role-view.action?roleId=${roleId}`)
        .then(res => res.json())
        .then(data => {
            if (!data.success) {
                alert(data.message);
                return;
            }

            document.getElementById("roleName").textContent =
                data.data.roleName;

            assignedPermissions.clear();
            data.data.permissions.forEach(code =>
                assignedPermissions.add(code)
            );

            loadPermissions();
        })
        .catch(err => {
            console.error(err);
            alert("Failed to load role");
        });
}

function loadPermissions() {
    fetch("/mini_crm_Web_exploded/role-permissions.action")
        .then(res => res.json())
        .then(data => {
            if (!data.success) {
                alert(data.message);
                return;
            }

            const container = document.getElementById("permissions");
            container.innerHTML = "";

            data.data.forEach(p => {
                const checked =
                    assignedPermissions.has(p.permissionCode) ? "checked" : "";

                container.innerHTML += `
                    <label class="perm-item">
                        <input type="checkbox" value="${p.id}" ${checked}>
                        <div><strong>${p.permissionCode}</strong></div>
                        <div>${p.description || ""}</div>
                    </label>
                `;
            });
        });
}

function saveRole() {
    const selected = Array.from(
        document.querySelectorAll("#permissions input:checked")
    ).map(cb => cb.value);

    if (selected.length === 0) {
        alert("Select at least one permission");
        return;
    }

    const formData = new URLSearchParams();
    formData.append("roleId", roleId);
    selected.forEach(id => formData.append("permissionIds", id));

    fetch("/mini_crm_Web_exploded/role-update.action", {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body: formData
    })
        .then(res => res.json())
        .then(data => {
            if (!data.success) {
                alert(data.message);
                return;
            }
            alert("Role updated successfully");
            window.location.href =
                `/mini_crm_Web_exploded/ui/roles/view.html?roleId=${roleId}`;
        });
}
