function loadActivities() {
    fetch("/mini_crm_Web_exploded/activity.action")
        .then(res => res.json())
        .then(res => {
            if (!res.success) {
                alert(res.message);
                return;
            }

            const container = document.getElementById("activityGroups");
            container.innerHTML = "";

            // Group activities by type
            const grouped = {};
            res.data.forEach(a => {
                if (!grouped[a.type]) {
                    grouped[a.type] = [];
                }
                grouped[a.type].push(a);
            });

            Object.keys(grouped).forEach(type => {
                container.innerHTML += renderActivityTable(type, grouped[type]);
            });
        });
}

function formatDate(d) {
    if (!d) return "-";

    const day = String(d.dayOfMonth).padStart(2, "0");
    const month = String(d.monthValue).padStart(2, "0");
    const year = d.year;

    return `${day}-${month}-${year}`;
}

function renderActivityTable(type, activities) {
    let rows = "";

    activities.forEach(a => {
        rows += `
            <tr>
                <td>${a.dealTitle ?? "-"}</td>
                <td>${a.description}</td>
                <td>${formatDate(a.createdAt)}</td>
                <td>${a.statusCode}</td>
                <td>${formatDate(a.dueDate)}</td>
                <td width="220">
                    <a href="view.html?id=${a.id}"
                       class="btn btn-sm btn-outline-primary">
                        View
                    </a>
                    <a href="edit.html?id=${a.id}"
                       class="btn btn-sm btn-outline-secondary">
                        Edit
                    </a>
                    <button class="btn btn-sm btn-outline-danger"
                            onclick="deleteActivity(${a.id})">
                        Delete
                    </button>
                </td>
            </tr>
        `;
    });

    return `
        <div class="card mb-4">
            <div class="card-header bg-dark text-white">
                ${type}
            </div>
            <div class="card-body p-0">
                <table class="table table-hover mb-0">
                    <thead class="table-light">
                    <tr>
                        <th>Deal</th>
                        <th>Description</th>
                        <th>Start Date</th>
                        <th>Status</th>
                        <th>Due Date</th>
                        <th>Actions</th>
                    </tr>
                    </thead>
                    <tbody>
                        ${rows}
                    </tbody>
                </table>
            </div>
        </div>
    `;
}

function deleteActivity(id) {
    if (!confirm("Delete this activity?")) return;

    fetch(`/mini_crm_Web_exploded/activity-delete.action?id=${id}`)
        .then(res => res.json())
        .then(() => loadActivities());
}

loadActivities();
