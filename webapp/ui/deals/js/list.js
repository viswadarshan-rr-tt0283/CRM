function loadDeals(keyword = "") {
    let url = "/mini_crm_Web_exploded/deal.action";
    if (keyword.trim() !== "") {
        url += "?keyword=" + encodeURIComponent(keyword);
    }

    fetch(url)
        .then(res => res.json())
        .then(res => {
            if (!res.success) {
                alert(res.message);
                return;
            }

            const tbody = document.getElementById("dealTable");
            tbody.innerHTML = "";

            if (!res.data || res.data.length === 0) {
                tbody.innerHTML = `
                    <tr>
                        <td colspan="7" class="text-center text-muted">
                            No deals found
                        </td>
                    </tr>
                `;
                return;
            }

            res.data.forEach(d => {
                tbody.innerHTML += `
                    <tr>
                        <td>${d.id}</td>
                        <td>
                            <a href="view.html?id=${d.id}"
                               class="fw-semibold text-decoration-none">
                                ${d.title}
                            </a>
                        </td>
                        <td>₹ ${Number(d.amount).toLocaleString()}</td>
                        <td>
                            <span class="badge bg-info">${d.status}</span>
                        </td>
                        <td>${d.companyName ?? "-"}</td>
                        <td>${d.assignedUserName ?? "-"}</td>
                        <td>
                            <a href="view.html?id=${d.id}"
                               class="btn btn-sm btn-outline-primary me-1">
                               View
                            </a>
                            <a href="edit.html?id=${d.id}"
                               class="btn btn-sm btn-primary me-1">
                               Edit
                            </a>
                            <button onclick="deleteDeal(${d.id})"
                                    class="btn btn-sm btn-danger">
                                Delete
                            </button>
                        </td>
                    </tr>
                `;
            });
        });
}

function searchDeals() {
    const keyword = document.getElementById("keyword").value;
    loadDeals(keyword);
}

function deleteDeal(id) {
    if (!confirm("Delete this deal?")) return;

    fetch("/mini_crm_Web_exploded/deal-delete.action?id=" + id)
        .then(res => res.json())
        .then(res => {
            alert(res.message);
            loadDeals();
        });
}

// Initial load
loadDeals();
