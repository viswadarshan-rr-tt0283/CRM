function loadCompanies(keyword = "") {
    let url = "/mini_crm_Web_exploded/company.action";

    if (keyword && keyword.trim() !== "") {
        url += "?keyword=" + encodeURIComponent(keyword);
    }

    fetch(url)
        .then(res => res.json())
        .then(res => {
            if (!res.success) {
                alert("Error: " + res.message);
                return;
            }

            const table = document.getElementById("companyTable");
            table.innerHTML = "";

            res.data.forEach(c => {
                table.innerHTML += `
                    <tr>
                        <td>${c.id}</td>

                        <!-- Clickable company name -->
                        <td>
                            <a href="view.html?id=${c.id}"
                               class="fw-semibold text-decoration-none">
                               ${c.name}
                            </a>
                        </td>

                        <td>${c.email ?? "-"}</td>
                        <td>${c.phone ?? "-"}</td>
                        <td>${c.address ?? "-"}</td>

                        <td>
                            <a href="view.html?id=${c.id}"
                               class="btn btn-sm btn-outline-secondary me-1">
                               View
                            </a>

                            <a href="edit.html?id=${c.id}"
                               class="btn btn-sm btn-primary me-1">
                               Edit
                            </a>

                            <button class="btn btn-sm btn-danger"
                                    onclick="deleteCompany(${c.id})">
                                Delete
                            </button>
                        </td>
                    </tr>
                `;
            });
        });
}

function searchCompanies() {
    const keyword = document.getElementById("keyword").value;
    loadCompanies(keyword);
}

function deleteCompany(id) {
    if (!confirm("Delete this company?")) return;

    fetch("/mini_crm_Web_exploded/company-delete.action?id=" + id)
        .then(res => res.json())
        .then(res => {
            alert(res.message);
            loadCompanies();
        });
}

// Initial load
loadCompanies();
