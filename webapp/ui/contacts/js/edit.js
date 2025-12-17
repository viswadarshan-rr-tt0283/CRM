const urlParams = new URLSearchParams(window.location.search);
const id = urlParams.get("id");

function loadContact() {
    fetch("/mini_crm_Web_exploded/contact-edit.action?id=" + id)
        .then(res => res.json())
        .then(res => {
            if (!res.success) {
                alert(res.message);
                return;
            }

            const c = res.data;

            document.getElementById("id").value = c.id;
            document.getElementById("companyId").value = c.companyId;
            document.getElementById("name").value = c.name;
            document.getElementById("email").value = c.email;
            document.getElementById("phone").value = c.phone;
            document.getElementById("jobTitle").value = c.jobTitle;
        });
}

document.getElementById("editForm").addEventListener("submit", function (e) {
    e.preventDefault();

    const data = new URLSearchParams();
    data.append("id", id);
    data.append("name", document.getElementById("name").value);
    data.append("email", document.getElementById("email").value);
    data.append("phone", document.getElementById("phone").value);
    data.append("jobTitle", document.getElementById("jobTitle").value);

    fetch("/mini_crm_Web_exploded/contact-save.action", {
        method: "POST",
        body: data
    })
        .then(res => res.json())
        .then(res => {
            if (res.success) {
                alert("Contact updated!");
                window.location.href = "list.html";
            } else {
                alert("Error: " + res.message);
            }
        });
});

loadContact();
