fetch("/mini_crm_Web_exploded/dashboard.action")
    .then(res => res.json())
    .then(res => {

        if (!res.success) {
            window.location.href = "/mini_crm_Web_exploded/ui/login.html";
            return;
        }

        const data = res.data;
        const user = data.userContext;

        // Load Navbar
        if (typeof loadNavbar === "function") {
            loadNavbar(user.role);
        }

        // Set User Context
        document.getElementById("welcomeText").innerText = `Welcome, ${user.fullName}`;
        document.getElementById("roleText").innerText = user.role;

        // Render Components
        renderSummary(data.summary, user.role);
        renderDealWidget(data.dealWidget);
        renderActivityWidget(data.activityWidget);
        renderRecent(data.recent);
    })
    .catch(err => console.error("Dashboard Load Error:", err));


function renderSummary(summary, role) {

    const cards = [];

    // Always visible for all roles
    cards.push(card(
        "Deals",
        summary.totalDeals,
        "fa-handshake",
        "icon-blue"
    ));

    cards.push(card(
        "Pending Activities",
        summary.pendingActivities,
        "fa-clock",
        "icon-orange"
    ));

    // MANAGER & ADMIN only
    if (role === "ADMIN") {

        cards.push(card(
            "Companies",
            summary.totalCompanies,
            "fa-building",
            "icon-purple"
        ));

        cards.push(card(
            "Contacts",
            summary.totalContacts,
            "fa-address-book",
            "icon-cyan"
        ));

        cards.push(card(
            "Users",
            summary.totalUsers,
            "fa-users",
            "icon-green"
        ));
    }

    document.getElementById("summaryCards").innerHTML = cards.join("");
}


// Helper to generate the Professional Card HTML
function card(title, value, icon, colorClass) {
    return `
        <div class="col-md-6 col-xl">
            <div class="card h-100 border-0 shadow-sm">
                <div class="card-body d-flex align-items-center">
                    <div class="summary-card-icon ${colorClass}">
                        <i class="fa-solid ${icon}"></i>
                    </div>
                    <div>
                        <div class="summary-value">${value}</div>
                        <div class="summary-label">${title}</div>
                    </div>
                </div>
            </div>
        </div>
    `;
}

function renderDealWidget(d) {
    document.getElementById("dealWidget").innerHTML = `
        <div class="d-flex justify-content-between align-items-end mb-3 w-100 px-2">
            <div>
                <small class="text-muted text-uppercase fw-bold" style="font-size:0.7rem;">Total Pipeline</small>
                <h2 class="mb-0 text-dark">₹${d.totalPipelineAmount.toLocaleString()}</h2>
            </div>
        </div>
        <div style="position: relative; height: 250px; width: 100%;">
            <canvas id="dealChart"></canvas>
        </div>
    `;

    const ctx = document.getElementById("dealChart");

    new Chart(ctx, {
        type: "bar",
        data: {
            labels: ["New", "Qualified", "In Progress", "Delivered", "Closed"],
            datasets: [{
                label: "Deals",
                data: [d.newDeals, d.qualifiedDeals, d.inProgressDeals, d.deliveredDeals, d.closedDeals],
                backgroundColor: ["#3b82f6", "#8b5cf6", "#f97316", "#10b981", "#14b8a6"],
                borderRadius: 4,
                barThickness: 30
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { display: false } },
            scales: {
                y: { beginAtZero: true, grid: { color: "#f1f5f9" } },
                x: { grid: { display: false } }
            }
        }
    });
}

function renderActivityWidget(a) {
    document.getElementById("activityWidget").innerHTML = `
        <div style="position: relative; height: 220px; width: 100%;">
            <canvas id="activityChart"></canvas>
        </div>
    `;

    const ctx = document.getElementById("activityChart");

    new Chart(ctx, {
        type: "doughnut",
        data: {
            labels: ["Pending", "Overdue", "Completed"],
            datasets: [{
                data: [a.pendingActivities, a.overdueActivities, a.completedActivities],
                backgroundColor: ["#f59e0b", "#ef4444", "#10b981"],
                borderWidth: 0,
                hoverOffset: 4
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            cutout: "70%",
            plugins: {
                legend: { position: 'bottom', labels: { usePointStyle: true, padding: 20 } }
            }
        }
    });
}

function renderRecent(recent) {
    const merged = [...recent.recentDeals, ...recent.recentContacts, ...recent.recentActivities];

    merged.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));

    document.getElementById("recentFeed").innerHTML = merged.length
        ? merged.map(item => feedItem(item)).join("")
        : `<li class="list-group-item text-muted text-center py-4">No recent activity found</li>`;
}

function feedItem(item) {
    let icon = 'fa-circle';
    let colorClass = 'text-secondary';

    if(item.type === 'DEAL') { icon = 'fa-handshake'; colorClass = 'text-primary'; }
    if(item.type === 'CONTACT') { icon = 'fa-user'; colorClass = 'text-info'; }
    if(item.type === 'CALL') { icon = 'fa-phone'; colorClass = 'text-warning'; }
    if(item.type === 'MEETING') { icon = 'fa-users'; colorClass = 'text-purple'; }

    return `
        <li class="list-group-item d-flex align-items-center justify-content-between py-3">
            <div class="d-flex align-items-center">
                <div class="me-3 ${colorClass}" style="width:30px; text-align:center;">
                    <i class="fa-solid ${icon} fs-5"></i>
                </div>
                <div>
                    <div class="fw-semibold text-dark" style="font-size:0.95rem;">${item.title || item.description}</div>
                    <small class="text-muted" style="font-size:0.8rem;">
                        ${item.type} <span class="mx-1">&bull;</span> ${formatDate(item.createdAt)}
                    </small>
                </div>
            </div>
            <div class="text-end">
                <i class="fa-solid fa-chevron-right text-light"></i>
            </div>
        </li>
    `;
}

function formatDate(d) {
    if (!d) return "-";
    if (typeof d === "string") return d;

    return `${String(d.dayOfMonth).padStart(2, "0")}-${String(d.monthValue).padStart(2, "0")}-${d.year}`;
}