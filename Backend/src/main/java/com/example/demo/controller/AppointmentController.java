package com.example.demo.controller;

import com.example.demo.entity.Appointment;
import com.example.demo.repository.AppointmentRepository;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@CrossOrigin(origins = "*")
public class AppointmentController {
    private final AppointmentRepository repo;

    public AppointmentController(AppointmentRepository repo) {
        this.repo = repo;
    }

    @PostMapping
    public Appointment addAppointment(@RequestBody Appointment appointment) {
        return repo.save(appointment);
    }

    @GetMapping
    public List<Appointment> getAppointments() {
        return repo.findAll();
    }
}