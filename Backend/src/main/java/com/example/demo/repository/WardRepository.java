package com.example.demo.repository;

import com.example.demo.entity.Ward;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WardRepository extends JpaRepository<Ward, Long> {
    Ward findByWardName(String wardName);
}