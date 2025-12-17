const params = new URLSearchParams(window.location.search);
const companyId = params.get("id");

function formatDate(d) {
    if (!d) return "-";

    const day = String(d.dayOfMonth).padStart(2, "0");
    const month = String(d.monthValue).padStart(2, "0");
    const year = d.year;

    return `${day}-${month}-${year}`;
}

function formatCurrency(amount) {
    if (amount == null) return "-";

    return new Intl.NumberFormat("en-IN", {
        style: "currency",
        currency: "INR",
        maximumFractionDigits: 0
    }).format(amount);
}

fetch("/mini_crm_Web_exploded/company-view.action?id=" + companyId)
    .then(res => res.json())
    .then(res => {

        if (!res.success) {
            alert(res.message);
            return;
        }

        const data = res.data;
        const c = data.company;

        // Header
        document.getElementById("companyName").innerText = c.name;
        document.getElementById("companyMeta").innerText =
            `Created at: ${formatDate(c.createdAt)}`;

        document.getElementById("companyContact").innerText =
            `${c.email ?? ""} ${c.phone ?? ""}`;

        // Overview
        document.getElementById("overviewContent").innerHTML = `
            <p><strong>Address:</strong> ${c.address ?? "-"}</p>
            <p><strong>Total Contacts:</strong> ${data.contacts.length}</p>
            <p><strong>Total Deals:</strong> ${data.deals.length}</p>
        `;

        // Contacts
        const contactsTable = document.getElementById("contactsTable");
        data.contacts.forEach(ct => {
            contactsTable.innerHTML += `
                <tr>
                  <td>${ct.name}</td>
                  <td>${ct.email ?? "-"}</td>
                  <td>${ct.phone ?? "-"}</td>
                  <td>${ct.jobTitle ?? "-"}</td>
                </tr>
            `;
        });

        // Deals
        const dealsTable = document.getElementById("dealsTable");
        data.deals.forEach(d => {
            dealsTable.innerHTML += `
                <tr>
                  <td>${d.title}</td>
                    <td>${formatCurrency(d.amount)}</td>
                  <td>${d.status}</td>
                  <td>${d.assignedUserName ?? "-"}</td>
                </tr>
            `;
        });

        // Activities
        const activitiesList = document.getElementById("activitiesList");
        data.activities.forEach(a => {
            activitiesList.innerHTML += `
                <li class="list-group-item">
                    <strong>${a.type}</strong> - ${a.description}
                    <span class="text-muted float-end">${formatDate(a.dueDate)}</span>
                </li>
            `;
        });

        // Timeline
        const timelineList = document.getElementById("timelineList");
        data.timeline.forEach(t => {
            timelineList.innerHTML += `
                <li class="list-group-item">
                    <strong>${t.entityType}</strong>: ${t.title}
                    <div class="text-muted small">${t.subtitle}</div>
                    <span class="text-muted float-end">${formatDate(t.createdAt)}</span>
                </li>
            `;
        });
    });
