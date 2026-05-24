package com.example.demo.controller;

import com.example.demo.entity.Patient;
import com.example.demo.entity.Ward;
import com.example.demo.repository.PatientRepository;
import com.example.demo.repository.WardRepository;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/hospital")
@CrossOrigin(origins = "*")
public class PatientController {

    private final PatientRepository repo;
    private final WardRepository wardRepo;

    public PatientController(PatientRepository repo, WardRepository wardRepo) {
        this.repo = repo;
        this.wardRepo = wardRepo;
    }

    @PostMapping("/admit")
    public Patient admitPatient(@RequestBody Patient patient) {

        String type = patient.getType().toUpperCase();

        switch (type) {
            case "HEART":
                setPatientDetails(patient, "Cardiology Ward", 50000);
                break;

            case "ICU":
                setPatientDetails(patient, "ICU Ward", 20000);
                break;

            case "ORTHO":
                setPatientDetails(patient, "Orthopedic Ward", 15000);
                break;

            case "NEURO":
                setPatientDetails(patient, "Neurology Ward", 40000);
                break;

            case "PEDIATRIC":
                setPatientDetails(patient, "Pediatric Ward", 8000);
                break;

            case "GENERAL":
                setPatientDetails(patient, "General Ward", 5000);
                break;

            case "MATERNITY":
                setPatientDetails(patient, "Maternity Ward", 25000);
                break;

            case "EMERGENCY":
                setPatientDetails(patient, "Emergency Ward", 10000);
                break;

            case "BASIC":
                patient.setWardName("No Ward");
                patient.setBedNumber(null);
                patient.setFee(1000);
                patient.setStatus("CHECKUP_DONE");
                break;

            default:
                throw new RuntimeException("Invalid patient type");
        }

        return repo.save(patient);
    }

    private void setPatientDetails(Patient patient, String wardName, int fee) {
        patient.setWardName(wardName);
        patient.setFee(fee);
        patient.setStatus("ADMITTED");
        patient.setBedNumber(assignBed(wardName));
    }

    private int assignBed(String wardName) {
        Ward ward = wardRepo.findByWardName(wardName);

        if (ward == null) {
            throw new RuntimeException(wardName + " not found in database");
        }

        if (ward.getOccupiedBeds() >= ward.getTotalBeds()) {
            throw new RuntimeException(wardName + " is full");
        }

        List<Patient> admitted = repo.findByWardNameAndStatus(wardName, "ADMITTED");

        for (int i = 1; i <= ward.getTotalBeds(); i++) {
            boolean occupied = false;

            for (Patient p : admitted) {
                if (p.getBedNumber() != null && p.getBedNumber() == i) {
                    occupied = true;
                    break;
                }
            }

            if (!occupied) {
                ward.setOccupiedBeds(ward.getOccupiedBeds() + 1);
                wardRepo.save(ward);
                return i;
            }
        }

        throw new RuntimeException("No bed available in " + wardName);
    }

    @GetMapping("/patients")
    public List<Patient> getAllPatients() {
        return repo.findAll();
    }

    @GetMapping("/status")
    public Map<String, Object> hospitalStatus() {
        Map<String, Object> status = new HashMap<>();

        status.put("wards", wardRepo.findAll());

        String[] wardNames = {
                "Cardiology Ward",
                "ICU Ward",
                "Orthopedic Ward",
                "Neurology Ward",
                "Pediatric Ward",
                "General Ward",
                "Maternity Ward",
                "Emergency Ward"
        };

        for (String ward : wardNames) {
            status.put(ward, repo.findByWardNameAndStatus(ward, "ADMITTED"));
        }

        return status;
    }

    @GetMapping("/wards")
    public List<Ward> getWards() {
        return wardRepo.findAll();
    }

    @DeleteMapping("/discharge/{id}")
    public String dischargePatient(@PathVariable Long id) {
        Patient patient = repo.findById(id).orElse(null);

        if (patient == null) {
            return "Patient not found";
        }

        if (patient.getWardName() != null && !patient.getWardName().equals("No Ward")) {
            Ward ward = wardRepo.findByWardName(patient.getWardName());

            if (ward != null && ward.getOccupiedBeds() > 0) {
                ward.setOccupiedBeds(ward.getOccupiedBeds() - 1);
                wardRepo.save(ward);
            }
        }

        patient.setStatus("DISCHARGED");
        patient.setBedNumber(null);
        repo.save(patient);

       return patient.getName() + " discharged successfully. Bed is now available.";
    }
}