package com.example.demo.entity;

import jakarta.persistence.*;

@Entity
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String patientId;
    private String name;
    private int age;
    private String gender;
    private String disease;
    private String phone;

    private String type;
    private String wardName;
    private Integer bedNumber;
    private int fee;
    private String status;

    public Patient() {}

    public Long getId() { return id; }
    public String getPatientId() { return patientId; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getGender() { return gender; }
    public String getDisease() { return disease; }
    public String getPhone() { return phone; }
    public String getType() { return type; }
    public String getWardName() { return wardName; }
    public Integer getBedNumber() { return bedNumber; }
    public int getFee() { return fee; }
    public String getStatus() { return status; }

    public void setId(Long id) { this.id = id; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    public void setName(String name) { this.name = name; }
    public void setAge(int age) { this.age = age; }
    public void setGender(String gender) { this.gender = gender; }
    public void setDisease(String disease) { this.disease = disease; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setType(String type) { this.type = type; }
    public void setWardName(String wardName) { this.wardName = wardName; }
    public void setBedNumber(Integer bedNumber) { this.bedNumber = bedNumber; }
    public void setFee(int fee) { this.fee = fee; }
    public void setStatus(String status) { this.status = status; }
}