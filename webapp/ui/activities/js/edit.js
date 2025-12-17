const urlParams = new URLSearchParams(window.location.search);
const id = urlParams.get("id");

function loadData() {
    fetch(`/mini_crm_Web_exploded/activity-edit.action?id=` + id)
        .then(res => res.json())
        .then(res => {
            const a = res.data;

            document.getElementById("id").value = a.id;
            document.getElementById("description").value = a.description;
            document.getElementById("dueDate").value = a.dueDate;
        });
}

document.getElementById("editForm").addEventListener("submit", function (e) {
    e.preventDefault();

    const data = new URLSearchParams();
    data.append("id", id);
    data.append("description", document.getElementById("description").value);
    data.append("dueDate", document.getElementById("dueDate").value);

    fetch("/mini_crm_Web_exploded/activity-update.action", {
        method: "POST",
        body: data
    })
        .then(res => res.json())
        .then(res => {
            if (res.success) {
                alert("Updated Successfully");
                window.location.href = "list.html";
            } else {
                alert("Error: " + res.message);
            }
        });
});

loadData();
