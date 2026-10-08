package com.farmmantra.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "crop")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Crop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String soilTypes;   // comma-separated, e.g. "BLACK,ALLUVIAL"

    private int sowFromMonth;   // 1-12

    private int sowToMonth;     // 1-12, may wrap around the year

    private int durationDays;

    private String waterNeed;   // LOW, MEDIUM, HIGH

    @Column(length = 500)
    private String notes;
}