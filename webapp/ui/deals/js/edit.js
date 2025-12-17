const id = new URLSearchParams(window.location.search).get("id");

if (!id) {
    alert("Invalid deal id");
    window.location.href = "list.html";
}

/* =====================================================
   LOAD DROPDOWNS
   ===================================================== */
function loadDropdowns() {
    return fetch("/mini_crm_Web_exploded/deal-add.action")
        .then(res => res.json())
        .then(res => {
            if (!res.success) {
                throw new Error(res.message);
            }

            fillDropdown(
                "companyId",
                res.data.companies,
                "id",
                "name"
            );

            fillDropdown(
                "assignedUserId",
                res.data.users,
                "userId",
                "username"
            );
        });
}

function fillDropdown(id, list, valueKey, labelKey) {
    const el = document.getElementById(id);
    el.innerHTML = list
        .map(v => `<option value="${v[valueKey]}">${v[labelKey]}</option>`)
        .join("");
}

/* =====================================================
   LOAD DEAL
   ===================================================== */
function loadDeal() {
    return fetch("/mini_crm_Web_exploded/deal-edit.action?id=" + id)
        .then(res => res.json())
        .then(res => {
            if (!res.success) {
                throw new Error(res.message);
            }

            const d = res.data;

            document.getElementById("id").value = d.id;
            document.getElementById("title").value = d.title;
            document.getElementById("amount").value = d.amount;

            document.getElementById("companyId").value = d.companyId;
            document.getElementById("assignedUserId").value = d.assignedUserId;
        });
}

/* =====================================================
   SUBMIT UPDATE
   ===================================================== */
document.getElementById("editForm").addEventListener("submit", function (e) {
    e.preventDefault();

    const data = new URLSearchParams();
    data.append("id", id);
    data.append("title", document.getElementById("title").value.trim());
    data.append("amount", document.getElementById("amount").value);
    data.append(
        "assignedUserId",
        document.getElementById("assignedUserId").value
    );

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
        })
        .catch(err => {
            alert("Update failed: " + err.message);
        });
});

/* =====================================================
   INIT
   ===================================================== */
loadDropdowns()
    .then(loadDeal)
    .catch(err => {
        alert(err.message);
        window.location.href = "list.html";
    });
