package com.farmmantra.dto;

import java.time.LocalDateTime;
import java.util.List;

public class DiseaseDto {

    public record Result(Long detectionId, String cropName, boolean healthy,
                         String diseaseName, double confidence, String description,
                         List<String> chemical, List<String> organic, List<String> prevention,
                         String warning, LocalDateTime createdAt) {}

    public record HistoryItem(Long id, String cropName, boolean healthy,
                              String diseaseName, double confidence, LocalDateTime createdAt) {}
}