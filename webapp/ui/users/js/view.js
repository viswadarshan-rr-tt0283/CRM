const params = new URLSearchParams(window.location.search);
const userId = params.get("id");

fetch(`/mini_crm_Web_exploded/user-view-detail.action?id=${userId}`)
    .then(res => res.json())
    .then(res => {
        if (!res.success) {
            alert(res.message);
            return;
        }

        const u = res.data;

        // Basic details
        document.getElementById("username").textContent = u.username;
        document.getElementById("email").textContent = u.email;
        document.getElementById("fullName").textContent = u.fullName;
        document.getElementById("role").textContent = u.roleName;

        /* =========================
           ROLE BASED UI CONTROL
           ========================= */

        // Hide everything by default
        document.getElementById("dealsSection").style.display = "none";
        document.getElementById("managerRow").style.display = "none";
        document.getElementById("teamSection").style.display = "none";

        // SALES USER
        if (u.roleName === "SALES") {

            // Manager
            document.getElementById("managerRow").style.display = "block";
            document.getElementById("manager").textContent =
                u.managerName ?? "-";

            // Deals
            if (u.deals && u.deals.length > 0) {
                document.getElementById("dealsSection").style.display = "block";
                const dealTable = document.getElementById("dealTable");

                dealTable.innerHTML = "";
                u.deals.forEach(d => {
                    dealTable.innerHTML += `
                        <tr>
                            <td>${d.id}</td>
                            <td>${d.title}</td>
                            <td>${d.status}</td>
                            <td>${d.amount}</td>
                        </tr>
                    `;
                });
            }
        }

        // MANAGER USER
        if (u.roleName === "MANAGER") {

            if (u.teamMembers && u.teamMembers.length > 0) {
                document.getElementById("teamSection").style.display = "block";
                const teamTable = document.getElementById("teamTable");

                teamTable.innerHTML = "";
                u.teamMembers.forEach(t => {
                    teamTable.innerHTML += `
                        <tr>
                            <td>${t.id}</td>
                            <td>${t.fullName}</td>
                            <td>${t.username}</td>
                            <td>${t.email}</td>
                        </tr>
                    `;
                });
            }
        }

        // ADMIN USER
        // Nothing extra to show
    });
