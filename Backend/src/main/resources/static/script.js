const baseUrl = "http://localhost:8080/api/hospital";

const form = document.getElementById("patientForm");
const patientList = document.getElementById("patientList");
const wardStatus = document.getElementById("wardStatus");

form.addEventListener("submit", function(e) {
    e.preventDefault();

    const patient = {
        patientId: document.getElementById("patientId").value,
        name: document.getElementById("name").value,
        age: Number(document.getElementById("age").value),
        gender: document.getElementById("gender").value,
        disease: document.getElementById("disease").value,
        phone: document.getElementById("phone").value,
        type: document.getElementById("type").value
    };

    fetch(`${baseUrl}/admit`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(patient)
    })
    .then(res => {
        if (!res.ok) {
            throw new Error("Ward full or server error");
        }
        return res.json();
    })
    .then(data => {
        showToast(`Done! Fee: Rs. ${data.fee}`);
        form.reset();
        loadEverything();
    })
    .catch(err => {
        showToast(err.message);
    });
});

function loadEverything() {
    loadPatients();
    loadStatus();
}

function loadPatients() {
    fetch(`${baseUrl}/patients`)
        .then(res => res.json())
        .then(data => {
            patientList.innerHTML = "";

            document.getElementById("totalCount").innerText =
                `${data.length} Records`;

            if (data.length === 0) {
                patientList.innerHTML = "<p>No patients found.</p>";
                return;
            }

            data.forEach(p => {
                const card = document.createElement("div");
                card.className = "patient-card";

                card.innerHTML = `
                    <h3>${p.name}</h3>
                    <p><b>Patient ID:</b> ${p.patientId}</p>
                    <p><b>Age:</b> ${p.age}</p>
                    <p><b>Gender:</b> ${p.gender}</p>
                    <p><b>Disease:</b> ${p.disease}</p>
                    <p><b>Type:</b> <span class="badge">${p.type}</span></p>
                    <p><b>Ward:</b> ${p.wardName}</p>
                    <p><b>Bed:</b> ${p.bedNumber ?? "No Bed"}</p>
                    <p><b>Fee:</b> Rs. ${p.fee}</p>
                    <p><b>Status:</b> ${p.status}</p>
                    ${
                        p.status === "ADMITTED"
                        ? `<button class="danger" onclick="dischargePatient(${p.id})">Discharge</button>`
                        : ""
                    }
                `;

                patientList.appendChild(card);
            });
        });
}

function loadStatus() {
    fetch(`${baseUrl}/status`)
        .then(res => res.json())
        .then(data => {
            const heart = data.heartWard || [];
            const icu = data.icuWard || [];

            document.getElementById("heartCount").innerText =
                `${heart.length} / 2 Beds Occupied`;

            document.getElementById("icuCount").innerText =
                `${icu.length} / 2 Beds Occupied`;

            wardStatus.innerHTML = `
                <div class="ward-box">
                    <h3>Heart Ward</h3>
                    ${renderBeds(heart)}
                </div>

                <div class="ward-box">
                    <h3>ICU Ward</h3>
                    ${renderBeds(icu)}
                </div>
            `;
        });
}

function renderBeds(patients) {
    let html = "";

    for (let i = 1; i <= 2; i++) {
        const found = patients.find(p => p.bedNumber === i);

        if (found) {
            html += `<div class="bed occupied">Bed ${i}: ${found.name}</div>`;
        } else {
            html += `<div class="bed">Bed ${i}: Empty</div>`;
        }
    }

    return html;
}

function dischargePatient(id) {
    fetch(`${baseUrl}/discharge/${id}`, {
        method: "DELETE"
    })
    .then(res => res.text())
    .then(msg => {
        showToast(msg);
        loadEverything();
    });
}

function showToast(message) {
    const toast = document.getElementById("toast");
    toast.innerText = message;
    toast.style.display = "block";

    setTimeout(() => {
        toast.style.display = "none";
    }, 2500);
}

loadEverything();