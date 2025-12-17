const permissionContainer = document.getElementById("permissionContainer");

let permissions = [];

function loadPermissions() {

    fetch("/mini_crm_Web_exploded/role-permissions.action")
        .then(res => res.json())
        .then(res => {

            if (!res.success) {
                alert(res.message);
                return;
            }

            permissions = res.data;
            renderPermissions();
        });
}

function renderPermissions() {

    const groups = {};

    permissions.forEach(p => {
        const group = p.permissionCode.split("_")[0];
        if (!groups[group]) {
            groups[group] = [];
        }
        groups[group].push(p);
    });

    permissionContainer.innerHTML = "";

    Object.keys(groups).forEach(group => {

        const section = document.createElement("div");
        section.className = "mb-4";

        section.innerHTML = `
            <h5 class="mb-2">${group} Permissions</h5>
            <div class="row">
                ${groups[group].map(p => `
                    <div class="col-md-4">
                        <div class="form-check">
                            <input class="form-check-input permission-checkbox"
                                   type="checkbox"
                                   value="${p.id}"
                                   id="perm_${p.id}">
                            <label class="form-check-label" for="perm_${p.id}">
                                ${p.permissionCode}
                            </label>
                        </div>
                    </div>
                `).join("")}
            </div>
        `;

        permissionContainer.appendChild(section);
    });
}

function createRole() {

    const roleName = document.getElementById("roleName").value.trim();

    if (!roleName) {
        alert("Role name is required");
        return;
    }

    const selectedPermissions = Array.from(
        document.querySelectorAll(".permission-checkbox:checked")
    ).map(cb => Number(cb.value));

    if (selectedPermissions.length === 0) {
        alert("Select at least one permission");
        return;
    }

    const data = new URLSearchParams();
    data.append("roleName", roleName);
    selectedPermissions.forEach(id =>
        data.append("permissionIds", id)
    );

    fetch("/mini_crm_Web_exploded/role-create.action", {
        method: "POST",
        body: data
    })
        .then(res => res.json())
        .then(res => {
            if (!res.success) {
                alert(res.message);
                return;
            }

            alert("Role created successfully");
            window.location.href = "role-list.html";
        });
}

// Init
loadPermissions();
