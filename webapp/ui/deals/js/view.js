const params = new URLSearchParams(window.location.search);
const dealId = params.get("id");

/* ---------- Utils ---------- */

function formatDate(d) {
    if (!d) return "-";
    if (typeof d === "string") return d;
    return `${String(d.dayOfMonth).padStart(2, "0")}-${String(d.monthValue).padStart(2, "0")}-${d.year}`;
}

/* ---------- Load Deal ---------- */

function loadDealDetail() {

    fetch(`/mini_crm_Web_exploded/deal-view.action?id=${dealId}`)
        .then(res => res.json())
        .then(res => {

            if (!res.success) {
                alert(res.message);
                return;
            }

            const data = res.data;
            const deal = data.deal;

            /* Header */
            document.getElementById("dealTitle").innerText = deal.title;
            document.getElementById("dealId").innerText = deal.id;

            /* Deal Info */
            document.getElementById("dealAmount").innerText = deal.amount;
            document.getElementById("dealStatus").innerText = deal.status;
            document.getElementById("assignedUser").innerText =
                deal.assignedUserName ?? "-";
            document.getElementById("createdAt").innerText =
                formatDate(deal.createdAt);

            document.getElementById("companyName").innerText =
                data.company?.name ?? "-";

            document.getElementById("contactName").innerText =
                data.contact?.name ?? "-";

            /* Activities */
            const activityList = document.getElementById("activityList");
            activityList.innerHTML = "";

            if (!data.activities || data.activities.length === 0) {
                activityList.innerHTML =
                    `<li class="list-group-item text-muted">No activities found</li>`;
                return;
            }

            data.activities.forEach(a => {
                activityList.innerHTML += `
                    <li class="list-group-item">
                        <div class="d-flex justify-content-between align-items-center">
                            <div>
                                <strong>${a.type}</strong> - ${a.description}
                                <br>
                                <small class="text-muted">
                                    Due: ${formatDate(a.dueDate)}
                                </small>
                            </div>
                            <span class="badge bg-secondary">
                                ${a.statusCode}
                            </span>
                        </div>
                    </li>
                `;
            });

            /* Status dropdown */
            const statusSelect = document.getElementById("statusSelect");
            statusSelect.innerHTML = "";

            (data.allowedNextStatuses || []).forEach(s => {
                statusSelect.innerHTML +=
                    `<option value="${s}">${s}</option>`;
            });
        });
}

loadDealDetail();
