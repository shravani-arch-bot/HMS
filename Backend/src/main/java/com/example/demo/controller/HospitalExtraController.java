package com.example.demo.controller;

import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class HospitalExtraController {

    private final WardRepository wardRepo;
    private final DoctorRepository doctorRepo;
    private final HospitalOperationRepository operationRepo;

    public HospitalExtraController(
            WardRepository wardRepo,
            DoctorRepository doctorRepo,
            HospitalOperationRepository operationRepo
    ) {
        this.wardRepo = wardRepo;
        this.doctorRepo = doctorRepo;
        this.operationRepo = operationRepo;
    }

    @GetMapping("/wards")
    public List<Ward> getWards() {
        return wardRepo.findAll();
    }

    @GetMapping("/doctors")
    public List<Doctor> getDoctors() {
        return doctorRepo.findAll();
    }

    @GetMapping("/doctors/free")
    public List<Doctor> getFreeDoctors() {
        return doctorRepo.findByStatus("FREE");
    }


    @PostMapping("/operations")
    public Map<String, Object> addOperation(@RequestBody HospitalOperation operation) {

    Map<String, Object> response = new HashMap<>();

    Doctor doctor = doctorRepo.findById(operation.getDoctorId()).orElse(null);

    if (doctor == null) {
        response.put("message", "Doctor not found");
        return response;
    }

    List<HospitalOperation> sameDayOperations =
            operationRepo.findByDoctorIdAndOperationDateAndStatus(
                    operation.getDoctorId(),
                    operation.getOperationDate(),
                    "SCHEDULED"
            );

    for (HospitalOperation op : sameDayOperations) {
        if (op.getOperationTime().equals(operation.getOperationTime())) {
            response.put("message",
                    doctor.getName() + " is not available at " +
                    operation.getOperationTime() +
                    ". Already scheduled for " +
                    op.getOperationType() +
                    " at " + op.getOperationTime()
            );
            return response;
        }
    }

    int operationHours = 3;
    int totalHours = sameDayOperations.size() * operationHours;

    if (totalHours + operationHours > 12) {
        response.put("message",
                doctor.getName() + " is not available. Daily operation limit exceeded. Prefer not to schedule more than 12 hours."
        );
        return response;
    }

    operation.setStatus("SCHEDULED");
    HospitalOperation saved = operationRepo.save(operation);

    doctor.setStatus("ALLOTTED");
    doctorRepo.save(doctor);

    response.put("message",
            "Operation scheduled successfully. " +
            doctor.getName() + " is allotted at " +
            operation.getOperationTime()
    );
    response.put("operation", saved);

    return response;
}
    @GetMapping("/operations")
    public List<HospitalOperation> getOperations() {
        return operationRepo.findAll();
    }


@PutMapping("/operations/complete/{operationId}")
public String completeOperation(@PathVariable Long operationId) {

    HospitalOperation operation = operationRepo.findById(operationId).orElse(null);

    if (operation == null) {
        return "Operation not found";
    }

    operation.setStatus("COMPLETED");
    operationRepo.save(operation);

    Doctor doctor = doctorRepo.findById(operation.getDoctorId()).orElse(null);

    if (doctor != null) {
        doctor.setStatus("FREE");
        doctorRepo.save(doctor);
    }

    return "Operation completed successfully. Doctor is now FREE.";


}
}