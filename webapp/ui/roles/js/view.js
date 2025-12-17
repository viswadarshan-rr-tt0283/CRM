const roleId =
    new URLSearchParams(window.location.search).get("id");

if (!roleId) {
    alert("Invalid role");
    window.location.href = "list.html";
}

const roleNameEl = document.getElementById("roleName");
const badgeEl = document.getElementById("roleBadge");
const permissionCountEl = document.getElementById("permissionCount");
const permissionContainer = document.getElementById("permissionContainer");
const editBtn = document.getElementById("editBtn");

function loadRole() {

    fetch(`/mini_crm_Web_exploded/role-view.action?roleId=${roleId}`)
        .then(res => res.json())
        .then(res => {

            if (!res.success) {
                alert(res.message);
                return;
            }

            renderRole(res.data);
        });
}

function renderRole(role) {

    roleNameEl.innerText = role.roleName;
    permissionCountEl.innerText = role.permissions.length;

    editBtn.href = `edit.html?id=${role.id}`;

    if (role.roleName === "ADMIN") {
        badgeEl.innerText = "SYSTEM ROLE";
        badgeEl.className = "badge bg-danger";
        editBtn.style.display = "none";
    } else {
        badgeEl.innerText = "CUSTOM ROLE";
        badgeEl.className = "badge bg-success";
    }

    renderPermissions(role.permissions);
}

function renderPermissions(permissions) {

    if (!permissions || permissions.length === 0) {
        permissionContainer.innerHTML =
            "<p class='text-muted'>No permissions assigned</p>";
        return;
    }

    const groups = groupPermissions(permissions);
    let html = "";

    Object.keys(groups).forEach(group => {

        html += `
            <div class="mb-3">
                <h6 class="text-primary">${group}</h6>
                <ul class="list-group">
                    ${groups[group]
            .map(p => `<li class="list-group-item">${p}</li>`)
            .join("")}
                </ul>
            </div>
        `;
    });

    permissionContainer.innerHTML = html;
}

function groupPermissions(permissions) {

    const map = {
        DEAL: [],
        CONTACT: [],
        ACTIVITY: [],
        COMPANY: [],
        USER: []
    };

    permissions.forEach(p => {
        const prefix = p.split("_")[0];
        if (map[prefix]) {
            map[prefix].push(p);
        }
    });

    return map;
}

// Init
loadRole();
