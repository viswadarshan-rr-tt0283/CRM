function loadUsers() {
    fetch("/mini_crm_Web_exploded/user.action")
        .then(res => res.json())
        .then(res => {
            if (!res.success) {
                alert(res.message);
                return;
            }

            document.getElementById("adminTable").innerHTML = "";
            document.getElementById("managerTable").innerHTML = "";
            document.getElementById("salesTable").innerHTML = "";

            res.data.forEach(u => {
                const row = buildRow(u);

                if (u.roleName === "ADMIN") {
                    document.getElementById("adminTable").innerHTML += row;
                } else if (u.roleName === "MANAGER") {
                    document.getElementById("managerTable").innerHTML += row;
                } else if (u.roleName === "SALES") {
                    document.getElementById("salesTable").innerHTML += buildSalesRow(u);
                }
            });
        });
}

function buildRow(u) {
    return `
        <tr>
            <td>${u.id}</td>
            <td>${u.username}</td>
            <td>${u.email}</td>
            <td>${u.fullName}</td>
            <td>
                <a href="view.html?id=${u.id}" class="btn btn-sm btn-primary">View</a>
                <button onclick="deleteUser(${u.id})" class="btn btn-sm btn-danger">Delete</button>
            </td>
        </tr>
    `;
}

function buildSalesRow(u) {
    return `
        <tr>
            <td>${u.id}</td>
            <td>${u.username}</td>
            <td>${u.email}</td>
            <td>${u.fullName}</td>
            <td>${u.managerName ?? "-"}</td>
            <td>
                <a href="view.html?id=${u.id}" class="btn btn-sm btn-primary">View</a>
                <button onclick="deleteUser(${u.id})" class="btn btn-sm btn-danger">Delete</button>
            </td>
        </tr>
    `;
}

function deleteUser(id) {
    if (!confirm("Delete this user?")) return;

    fetch("/mini_crm_Web_exploded/user-delete.action?id=" + id)
        .then(res => res.json())
        .then(res => {
            alert(res.message);
            if (res.success) loadUsers();
        });
}

loadUsers();
