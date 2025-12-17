document.getElementById("createForm").addEventListener("submit", function (e) {
    e.preventDefault();

    const data = new URLSearchParams();
    data.append("name", document.getElementById("name").value);
    data.append("email", document.getElementById("email").value);
    data.append("phone", document.getElementById("phone").value);
    data.append("address", document.getElementById("address").value);

    fetch("/mini_crm_Web_exploded/company-save.action", {
        method: "POST",
        body: data
    })
        .then(res => res.json())
        .then(res => {
            if (res.success) {
                alert("Company created!");
                window.location.href = "list.html";
            } else {
                alert("Error: " + res.message);
            }
        });
});
