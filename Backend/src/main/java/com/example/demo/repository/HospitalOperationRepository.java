package com.example.demo.repository;

import com.example.demo.entity.HospitalOperation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface HospitalOperationRepository extends JpaRepository<HospitalOperation, Long> {

    List<HospitalOperation> findByDoctorIdAndOperationDateAndStatus(
            Long doctorId,
            LocalDate operationDate,
            String status
    );

    List<HospitalOperation> findByDoctorIdAndStatus(Long doctorId, String status);
}