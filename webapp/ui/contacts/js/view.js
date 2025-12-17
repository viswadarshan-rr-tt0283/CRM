const params = new URLSearchParams(window.location.search);
const contactId = params.get("id");

fetch(`/mini_crm_Web_exploded/contact-view.action?id=${contactId}`)
    .then(res => res.json())
    .then(res => {

        if (!res.success) {
            alert(res.message);
            return;
        }

        const data = res.data;

        renderHeader(data.contact);
        showInfo(data.contact);

        window.contactDetail = data;
    });

function showTab(event, tab) {

    document.querySelectorAll(".nav-link")
        .forEach(t => t.classList.remove("active"));

    event.target.classList.add("active");

    if (tab === "info") showInfo(contactDetail.contact);
    if (tab === "timeline") showTimeline(contactDetail.timeline);
    if (tab === "deals") showDeals(contactDetail.deals);
    if (tab === "activities") showActivities(contactDetail.activities);
}
function formatDate(d) {
    if (!d) return "-";
    if (typeof d === "string") return d;

    return `${String(d.dayOfMonth).padStart(2, "0")}-${String(d.monthValue).padStart(2, "0")}-${d.year}`;
}


function renderHeader(c) {
    document.getElementById("contactHeader").innerHTML = `
        <h4>${c.name}</h4>
        <p class="text-muted">
            ${c.jobTitle || ""} · ${c.companyName}
        </p>
        <div>
            📧 ${c.email || "-"} |
            📞 ${c.phone || "-"}
        </div>
    `;
}

function showTab(tab) {

    document.querySelectorAll(".nav-link")
        .forEach(t => t.classList.remove("active"));

    event.target.classList.add("active");

    if (tab === "info") showInfo(contactDetail.contact);
    if (tab === "timeline") showTimeline(contactDetail.timeline);
    if (tab === "deals") showDeals(contactDetail.deals);
    if (tab === "activities") showActivities(contactDetail.activities);
}

function showInfo(c) {
    document.getElementById("tabContent").innerHTML = `
        <p><strong>Company:</strong> ${c.companyName}</p>
        <p><strong>Email:</strong> ${c.email || "-"}</p>
        <p><strong>Phone:</strong> ${c.phone || "-"}</p>
        <p><strong>Job Title:</strong> ${c.jobTitle || "-"}</p>
    `;
}

function showDeals(deals) {
    document.getElementById("tabContent").innerHTML =
        deals.length
            ? deals.map(d => `
                <div class="card mb-2">
                  <div class="card-body">
                    <strong>${d.title}</strong>
                    <span class="badge bg-info">${d.status}</span>
                    <div>₹${d.amount}</div>
                  </div>
                </div>
              `).join("")
            : `<p class="text-muted">No deals found</p>`;
}

function showActivities(acts = []) {
    document.getElementById("tabContent").innerHTML =
        acts.length
            ? acts.map(a => `
                <div class="list-group-item mb-2">
                  <strong>${a.type}</strong> - ${a.statusCode}
                  <div>${a.description}</div>
                </div>
              `).join("")
            : `<p class="text-muted">No activities found</p>`;
}

function showTimeline(items) {
    document.getElementById("tabContent").innerHTML =
        items.map(i => `
            <div class="border-start ps-3 mb-3">
              <small class="text-muted">${formatDate(i.createdAt)}</small>
              <div><strong>${i.type}</strong> - ${i.title}</div>
            </div>
        `).join("");
}
