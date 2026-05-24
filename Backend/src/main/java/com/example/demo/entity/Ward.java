package com.example.demo.entity;

import jakarta.persistence.*;

@Entity
public class Ward {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String wardName;
    private int totalBeds;
    private int occupiedBeds;

    public Long getId() { return id; }
    public String getWardName() { return wardName; }
    public int getTotalBeds() { return totalBeds; }
    public int getOccupiedBeds() { return occupiedBeds; }

    public void setId(Long id) { this.id = id; }
    public void setWardName(String wardName) { this.wardName = wardName; }
    public void setTotalBeds(int totalBeds) { this.totalBeds = totalBeds; }
    public void setOccupiedBeds(int occupiedBeds) { this.occupiedBeds = occupiedBeds; }
}