package com.farmmantra.dto;

import java.time.LocalDate;
import java.util.List;

public class PlannerDto {

    public record CropOption(Long id, String name, String sowingWindow,
                             int durationDays, String waterNeed, boolean suitableForSoil) {}

    public record Milestone(LocalDate date, String stage, String action) {}

    public record Plan(String cropName, LocalDate sowingDate, LocalDate harvestDate,
                       List<Milestone> milestones, List<String> warnings) {}
}