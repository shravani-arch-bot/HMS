import { useEffect, useState } from "react";
import { HeartPulse, Users, Bed, UserRound, CalendarCheck, RefreshCw, LogOut } from "lucide-react";
import "./App.css";

const API = "https://hms-z0kr.onrender.com";

export default function App() {
  const [tab, setTab] = useState("patients");
  const [outputTitle, setOutputTitle] = useState("Dashboard");
  const [output, setOutput] = useState([]);
  const [message, setMessage] = useState("");

  const request = async (url, options = {}, title = "Output") => {
    try {
      const res = await fetch(API + url, options);
      const text = await res.text();

      let data;
      try {
        data = JSON.parse(text);
      } catch (error) {
  setOutputTitle("Error");
  setMessage("Backend connection failed: " + error.message);
  setOutput([]);
}

      setOutputTitle(title);
      setOutput(Array.isArray(data) ? data : [data]);
      setMessage(typeof data === "string" ? data : data.message || "");
    } catch {
      setMessage("Backend connection failed. Run Spring Boot first.");
    }
  };

  return (
    <div className="app">
      <header className="hero">
        <h1><HeartPulse /> CityCare Super Specialty Hospital</h1>
        
      </header>

      <div className="tabs">
        <button onClick={() => setTab("patients")} className={tab === "patients" ? "active" : ""}><Users /> Patients</button>
        <button onClick={() => setTab("wards")} className={tab === "wards" ? "active" : ""}><Bed /> Wards</button>
        <button onClick={() => setTab("doctors")} className={tab === "doctors" ? "active" : ""}><UserRound /> Doctors</button>
        <button onClick={() => setTab("operations")} className={tab === "operations" ? "active" : ""}><CalendarCheck /> Operations</button>
      </div>

      {tab === "patients" && <Patients request={request} />}
      {tab === "wards" && <Wards request={request} />}
      {tab === "doctors" && <Doctors request={request} />}
      {tab === "operations" && <Operations request={request} />}

      <section className="card output">
        <h2>{outputTitle}</h2>
        {message && <div className="message">{message}</div>}
        <DataTable data={output} />
      </section>
    </div>
  );
}

function Patients({ request }) {
  const [form, setForm] = useState({
    patientId: "", name: "", age: "", gender: "", disease: "", phone: "", type: ""
  });
  const [dischargeId, setDischargeId] = useState("");

  const update = e => setForm({ ...form, [e.target.name]: e.target.value });

  const admit = e => {
    e.preventDefault();
    request("/api/hospital/admit", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ ...form, age: Number(form.age) })
    }, "Patient Saved");
  };

  return (
    <section className="card">
      <h2>Patient Admission & Discharge</h2>
      <form className="grid" onSubmit={admit}>
        <input name="patientId" placeholder="Patient ID" onChange={update} />
        <input name="name" placeholder="Patient Name" onChange={update} />
        <input name="age" placeholder="Age" type="number" onChange={update} />
        <input name="gender" placeholder="Gender" onChange={update} />
        <input name="disease" placeholder="Disease / Problem" onChange={update} />
        <input name="phone" placeholder="Phone Number" onChange={update} />

        <select name="type" onChange={update}>
          <option value="">Select Patient Type</option>
          {["HEART","ICU","ORTHO","NEURO","PEDIATRIC","GENERAL","MATERNITY","EMERGENCY","BASIC"].map(t =>
            <option key={t}>{t}</option>
          )}
        </select>

        <button className="primary">Admit / Checkup Patient</button>
      </form>

      <div className="actions">
        <button onClick={() => request("/api/hospital/patients", {}, "Patients")}><RefreshCw /> Show All Patients</button>
        <input placeholder="DB Patient ID to Discharge" value={dischargeId} onChange={e => setDischargeId(e.target.value)} />
        <button className="danger" onClick={() => request(`/api/hospital/discharge/${dischargeId}`, { method: "DELETE" }, "Discharge Status")}>
          <LogOut /> Discharge & Free Bed
        </button>
      </div>
    </section>
  );
}

function Wards({ request }) {
  return (
    <section className="card">
      <h2>Ward & Bed Management</h2>
      <div className="actions">
        <button onClick={() => request("/api/hospital/wards", {}, "Ward Bed Availability")}>Show Ward Bed Availability</button>
        <button onClick={() => request("/api/hospital/status", {}, "Hospital Status")}>Show Complete Hospital Status</button>
      </div>
    </section>
  );
}

function Doctors({ request }) {
  return (
    <section className="card">
      <h2>Doctor Availability</h2>
      <div className="actions">
        <button onClick={() => request("/api/doctors", {}, "Doctors")}>Show All Doctors</button>
        <button onClick={() => request("/api/doctors/free", {}, "Free Doctors")}>Show Free Doctors</button>
      </div>
    </section>
  );
}

function Operations({ request }) {
  const [form, setForm] = useState({
    patientId: "", doctorId: "", operationType: "", operationDate: "", operationTime: ""
  });
  const [completeId, setCompleteId] = useState("");

  const update = e => setForm({ ...form, [e.target.name]: e.target.value });

  const schedule = e => {
    e.preventDefault();
    request("/api/operations", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        ...form,
        patientId: Number(form.patientId),
        doctorId: Number(form.doctorId)
      })
    }, "Operation Status");
  };

  return (
    <section className="card">
      <h2>Operation Scheduling</h2>
      <form className="grid" onSubmit={schedule}>
        <input name="patientId" placeholder="DB Patient ID" onChange={update} />
        <input name="doctorId" placeholder="Doctor ID" onChange={update} />
        <input name="operationType" placeholder="Operation Type" onChange={update} />
        <input name="operationDate" type="date" onChange={update} />
        <input name="operationTime" type="time" step="1" onChange={update} />
        <button className="primary">Schedule Operation</button>
      </form>

      <div className="actions">
        <button onClick={() => request("/api/operations", {}, "Operations")}>Show Operations</button>
        <input placeholder="Operation ID to Complete" value={completeId} onChange={e => setCompleteId(e.target.value)} />
        <button className="danger" onClick={() => request(`/api/operations/complete/${completeId}`, { method: "PUT" }, "Operation Completed")}>
          Complete / Free Doctor
        </button>
      </div>
    </section>
  );
}

function DataTable({ data }) {
  if (!data || data.length === 0) {
    return <p className="empty">No data to show.</p>;
  }

  let rows = data;

  if (data.length === 1 && data[0]?.wards) {
    rows = data[0].wards;
  }

  const validRows = rows.filter(
    item => item !== null && typeof item === "object" && !Array.isArray(item)
  );

  if (validRows.length === 0) {
    return <pre>{JSON.stringify(data, null, 2)}</pre>;
  }

  const keys = [...new Set(validRows.flatMap(obj => Object.keys(obj)))];

  return (
    <div className="table-wrap">
      <table>
        <thead>
          <tr>
            {keys.map(key => (
              <th key={key}>{key}</th>
            ))}
          </tr>
        </thead>

        <tbody>
          {validRows.map((item, index) => (
            <tr key={index}>
              {keys.map(key => (
                <td key={key}>
                  {item[key] === null || item[key] === undefined
                    ? "-"
                    : typeof item[key] === "object"
                    ? JSON.stringify(item[key])
                    : String(item[key])}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}