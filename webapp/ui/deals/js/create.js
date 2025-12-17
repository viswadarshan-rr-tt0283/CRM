// Load dropdowns
fetch("/mini_crm_Web_exploded/deal-add.action")
    .then(res => res.json())
    .then(res => {
        if (!res.success) {
            alert(res.message);
            return;
        }

        const data = res.data;

        fillDropdownObj(
            "companyId",
            data.companies,
            "id",
            "name",
            "Select company"
        );

        fillDropdownObj(
            "assignedUserId",
            data.users,
            "userId",
            "username",
            "Assign to user"
        );

        clearContacts();
    });

function fillDropdownObj(id, list, valKey, labelKey, placeholder) {
    const el = document.getElementById(id);

    let html = `<option value="">${placeholder}</option>`;
    html += list.map(v =>
        `<option value="${v[valKey]}">${v[labelKey]}</option>`
    ).join("");

    el.innerHTML = html;
}

function clearContacts() {
    const el = document.getElementById("contactId");
    el.innerHTML = `<option value="">Select contact</option>`;
}

function loadContacts() {
    const companyId = document.getElementById("companyId").value;

    if (!companyId) {
        clearContacts();
        return;
    }

    fetch("/mini_crm_Web_exploded/contact-by-company.action?companyId=" + companyId)
        .then(res => res.json())
        .then(res => {
            if (!res.success) {
                alert(res.message);
                return;
            }

            fillDropdownObj(
                "contactId",
                res.data,
                "id",
                "name",
                "Select contact"
            );
        });
}

// Save deal
document.getElementById("createForm").addEventListener("submit", function (e) {
    e.preventDefault();

    const title = document.getElementById("title").value.trim();
    const amount = document.getElementById("amount").value;
    const companyId = document.getElementById("companyId").value;
    const assignedUserId = document.getElementById("assignedUserId").value;
    const contactId = document.getElementById("contactId").value;

    if (!companyId || !assignedUserId) {
        alert("Company and Assigned User are required");
        return;
    }

    const data = new URLSearchParams();
    data.append("title", title);
    data.append("amount", amount);
    data.append("companyId", companyId);
    data.append("assignedUserId", assignedUserId);

    if (contactId) {
        data.append("contactId", contactId);
    }

    fetch("/mini_crm_Web_exploded/deal-save.action", {
        method: "POST",
        body: data
    })
        .then(res => res.json())
        .then(res => {
            alert(res.message);
            if (res.success) {
                window.location.href = "list.html";
            }
        });
});
