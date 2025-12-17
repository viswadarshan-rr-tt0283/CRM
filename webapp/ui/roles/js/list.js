const tableBody = document.getElementById("roleTableBody");

function loadRoles() {

    fetch("/mini_crm_Web_exploded/role.action")
        .then(res => res.json())
        .then(res => {

            if (!res.success) {
                alert(res.message);
                return;
            }

            renderRoles(res.data);
        });
}

function renderRoles(roles) {

    if (!roles || roles.length === 0) {
        tableBody.innerHTML = `
            <tr>
                <td colspan="3" class="text-center text-muted">
                    No roles found
                </td>
            </tr>`;
        return;
    }

    tableBody.innerHTML = "";

    roles.forEach(role => {

        const isAdmin = role.roleName === "ADMIN";

        tableBody.innerHTML += `
            <tr>
                <td>
                    <strong>${role.roleName}</strong>
                    ${isAdmin ? `<span class="badge badge-secondary ml-1">System</span>` : ""}
                </td>
                <td>
                    ${role.permissions.length}
                </td>
                <td>
                    <a href="view.html?id=${role.id}"
                       class="btn btn-sm btn-outline-primary">
                        View
                    </a>

                    <a href="${isAdmin ? "#" : `edit.html?id=${role.id}`}"
                       class="btn btn-sm btn-outline-secondary
                              ${isAdmin ? "disabled" : ""}"
                       ${isAdmin ? "onclick='return false'" : ""}>
                        Edit
                    </a>

                    <button class="btn btn-sm btn-outline-danger"
                            ${isAdmin ? "disabled" : ""}
                            onclick="deleteRole(${role.id}, '${role.roleName}')">
                        Delete
                    </button>
                </td>
            </tr>
        `;
    });
}

function deleteRole(roleId, roleName) {

    if (!confirm(`Delete role '${roleName}'? This cannot be undone.`)) {
        return;
    }

    const data = new URLSearchParams();
    data.append("roleId", roleId);

    fetch("/mini_crm_Web_exploded/role-delete.action", {
        method: "POST",
        body: data
    })
        .then(res => res.json())
        .then(res => {
            if (!res.success) {
                alert(res.message);
                return;
            }

            alert("Role deleted");
            loadRoles();
        });
}

// Init
loadRoles();
