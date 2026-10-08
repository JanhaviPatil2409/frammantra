package com.farmmantra.entity;

import java.time.LocalDateTime;

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
@Table(name = "detection")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Detection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long farmerId;

    private String cropName;

    private boolean healthy;

    private String diseaseName;

    private double confidence;     // 0.0 to 1.0

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String chemical;       // one item per line

    @Column(columnDefinition = "TEXT")
    private String organic;        // one item per line

    @Column(columnDefinition = "TEXT")
    private String prevention;     // one item per line

    @Column(columnDefinition = "DATETIME")
    private LocalDateTime createdAt;
}