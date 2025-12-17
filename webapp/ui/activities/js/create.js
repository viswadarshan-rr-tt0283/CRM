document.getElementById("createForm").addEventListener("submit", function (e) {
    e.preventDefault();

    const data = new URLSearchParams();
    data.append("dealId", document.getElementById("dealId").value);
    data.append("type", document.getElementById("type").value);
    data.append("description", document.getElementById("description").value);
    data.append("dueDate", document.getElementById("dueDate").value);

    fetch("/mini_crm_Web_exploded/activity-save.action", {
        method: "POST",
        body: data
    })
        .then(res => res.json())
        .then(res => {
            if (res.success) {
                alert("Created successfully");
                window.location.href = "list.html";
            } else {
                alert("Error: " + res.message);
            }
        });
});
