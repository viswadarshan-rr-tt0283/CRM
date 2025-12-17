function loadContacts(keyword = "", companyId = null) {

    let url = "/mini_crm_Web_exploded/contact.action";

    if (keyword && keyword.trim() !== "") {
        url += "?keyword=" + encodeURIComponent(keyword);
    } else if (companyId) {
        url += "?companyId=" + companyId;
    }

    fetch(url)
        .then(res => res.json())
        .then(res => {

            if (!res.success) {
                alert(res.message);
                return;
            }

            const table = document.getElementById("contactTable");
            table.innerHTML = "";

            res.data.forEach(c => {
                table.innerHTML += `
                    <tr>
                        <td>
                          <a href="view.html?id=${c.id}"
                             class="fw-semibold text-decoration-none">
                             ${c.name}
                          </a>
                        </td>
                        <td>${c.companyName || "-"}</td>
                        <td>${c.email || "-"}</td>
                        <td>${c.phone || "-"}</td>
                        <td>${c.jobTitle || "-"}</td>
                        <td>
                            <a href="view.html?id=${c.id}"
                               class="btn btn-sm btn-outline-primary">
                               View
                            </a>
                            <a href="edit.html?id=${c.id}"
                               class="btn btn-sm btn-outline-secondary">
                               Edit
                            </a>
                            <button class="btn btn-sm btn-outline-danger"
                                    onclick="deleteContact(${c.id})">
                               Delete
                            </button>
                        </td>
                    </tr>
                `;
            });
        });
}

function searchContacts() {
    const keyword = document.getElementById("keyword").value;
    loadContacts(keyword);
}

function deleteContact(id) {
    if (!confirm("Delete this contact?")) return;

    fetch("/mini_crm_Web_exploded/contact-delete.action?id=" + id)
        .then(res => res.json())
        .then(res => {
            alert(res.message);
            loadContacts();
        });
}

loadContacts();
