const params = new URLSearchParams(window.location.search);
const id = params.get("id");

fetch(`/mini_crm_Web_exploded/activity-view.action?id=${id}`)
    .then(res => res.json())
    .then(res => {
        if (!res.success) {
            alert(res.message);
            return;
        }

        const data = res.data;

        const activity = data.activity;
        const deal = data.deal;
        const company = data.company;
        const contacts = data.contacts || [];

        // Header
        document.getElementById("activityTitle").innerText =
            `${activity.type} • ${activity.statusLabel}`;

        document.getElementById("activityMeta").innerText =
            `Due: ${formatDate(activity.dueDate)} | Created: ${formatDate(activity.createdAt)}`;

        document.getElementById("activityDescription").innerText =
            activity.description || "-";

        // Default tab
        showDeal(deal);
        window.activityView = { deal, company, contacts };
    });

/* ---------- Tabs ---------- */

function showTab(event, tab) {
    document.querySelectorAll(".nav-link")
        .forEach(b => b.classList.remove("active"));

    event.target.classList.add("active");

    if (tab === "deal") showDeal(activityView.deal);
    if (tab === "company") showCompany(activityView.company);
    if (tab === "contacts") showContacts(activityView.contacts);
}

/* ---------- Renderers ---------- */

function showDeal(deal) {
    document.getElementById("tabContent").innerHTML = deal
        ? `
            <h5>${deal.title}</h5>
            <p>Status: <strong>${deal.status}</strong></p>
            <p>Amount: ₹${deal.amount}</p>
            <a href="/mini_crm_Web_exploded/ui/deals/view.html?id=${deal.id}">
                View Deal
            </a>
          `
        : `<p class="text-muted">No deal linked</p>`;
}

function showCompany(company) {
    document.getElementById("tabContent").innerHTML = company
        ? `
            <h5>${company.name}</h5>
            <a href="/mini_crm_Web_exploded/ui/companies/view.html?id=${company.id}">
                View Company
            </a>
          `
        : `<p class="text-muted">No company available</p>`;
}

function showContacts(contacts) {
    document.getElementById("tabContent").innerHTML =
        contacts.length
            ? `
                <ul class="list-group">
                    ${contacts.map(c => `
                        <li class="list-group-item">
                            <strong>${c.name}</strong><br>
                            ${c.jobTitle}<br>
                            📧 ${c.email}<br>
                            📞 ${c.phone}
                        </li>
                    `).join("")}
                </ul>
              `
            : `<p class="text-muted">No contacts linked</p>`;
}

/* ---------- Utils ---------- */

function formatDate(d) {
    if (!d) return "-";
    return `${String(d.dayOfMonth).padStart(2, "0")}-${String(d.monthValue).padStart(2, "0")}-${d.year}`;
}
