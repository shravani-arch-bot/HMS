package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
public class HospitalOperation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long patientId;
    private Long doctorId;
    private String operationType;
    private LocalDate operationDate;
    private LocalTime operationTime;
    private String status;

    public Long getId() { return id; }
    public Long getPatientId() { return patientId; }
    public Long getDoctorId() { return doctorId; }
    public String getOperationType() { return operationType; }
    public LocalDate getOperationDate() { return operationDate; }
    public LocalTime getOperationTime() { return operationTime; }
    public String getStatus() { return status; }

    public void setId(Long id) { this.id = id; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }
    public void setOperationType(String operationType) { this.operationType = operationType; }
    public void setOperationDate(LocalDate operationDate) { this.operationDate = operationDate; }
    public void setOperationTime(LocalTime operationTime) { this.operationTime = operationTime; }
    public void setStatus(String status) { this.status = status; }
}