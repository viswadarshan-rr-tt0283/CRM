function loadNavbar(role) {

    const menuItems = [];

    if (role !== "SALES") {
        menuItems.push(`<a class="nav-link" href="/mini_crm_Web_exploded/ui/companies/list.html">Companies</a>`);
    }

    menuItems.push(`<a class="nav-link" href="/mini_crm_Web_exploded/ui/contacts/list.html">Contacts</a>`);
    menuItems.push(`<a class="nav-link" href="/mini_crm_Web_exploded/ui/deals/list.html">Deals</a>`);
    menuItems.push(`<a class="nav-link" href="/mini_crm_Web_exploded/ui/activities/list.html">Activities</a>`);

    if (role === "ADMIN") {
        menuItems.push(`<a class="nav-link" href="/mini_crm_Web_exploded/ui/users/list.html">Users</a>`);
    }

    document.getElementById("navbar").innerHTML = `
        <nav class="navbar navbar-expand-lg navbar-dark bg-dark">
            <div class="container-fluid">
                <a class="navbar-brand" href="/mini_crm_Web_exploded/ui/dashboard/index.html">CRM Dashboard</a>

                <div class="collapse navbar-collapse">
                    <div class="navbar-nav">
                        ${menuItems.join("")}
                    </div>
                </div>
            </div>
        </nav>
    `;
}
