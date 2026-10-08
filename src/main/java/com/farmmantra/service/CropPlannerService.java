package com.farmmantra.service;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.farmmantra.dto.PlannerDto;
import com.farmmantra.entity.Crop;
import com.farmmantra.entity.Farmer;
import com.farmmantra.repository.CropRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CropPlannerService {

    /** Farm activities placed at a percentage of the crop's duration. */
    private record Stage(int percent, String name, String action) {}

    private static final List<Stage> STAGES = List.of(
        new Stage(0,   "Sowing",              "Prepare the field, treat seed, and sow at recommended spacing."),
        new Stage(5,   "Basal fertilizer",    "Apply basal dose of fertilizer and give the first irrigation."),
        new Stage(25,  "Weeding",             "Weed the field and apply the first top dressing of nitrogen."),
        new Stage(45,  "Pest scouting",       "Check for pests and diseases weekly. Spray only if the threshold is crossed."),
        new Stage(60,  "Critical irrigation", "Irrigate at the flowering or critical growth stage."),
        new Stage(90,  "Pre-harvest check",   "Check maturity signs and plan labour, transport and storage."),
        new Stage(100, "Harvest",             "Harvest at maturity and dry the produce before storage.")
    );

    private final CropRepository cropRepo;

    public List<PlannerDto.CropOption> cropsFor(Farmer farmer) {
        return cropRepo.findAll().stream()
                .map(c -> new PlannerDto.CropOption(
                        c.getId(),
                        c.getName(),
                        windowText(c),
                        c.getDurationDays(),
                        c.getWaterNeed(),
                        soilMatches(c, farmer.getSoilType())))
                .sorted(Comparator.comparing(PlannerDto.CropOption::suitableForSoil).reversed()
                        .thenComparing(PlannerDto.CropOption::name))
                .toList();
    }

    public PlannerDto.Plan plan(Farmer farmer, Long cropId, LocalDate sowingDate) {
        Crop crop = cropRepo.findById(cropId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown crop"));

        List<String> warnings = new ArrayList<>();

        if (!soilMatches(crop, farmer.getSoilType())) {
            warnings.add(crop.getName() + " is not usually recommended for your soil type ("
                    + farmer.getSoilType() + "). Check with your local agriculture office.");
        }

        if (!inWindow(crop, sowingDate.getMonthValue())) {
            String month = sowingDate.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
            warnings.add("Sowing in " + month + " is outside the usual window for "
                    + crop.getName() + " (" + windowText(crop) + ").");
        }

        List<PlannerDto.Milestone> milestones = STAGES.stream()
                .map(s -> new PlannerDto.Milestone(
                        sowingDate.plusDays(Math.round(crop.getDurationDays() * s.percent() / 100.0)),
                        s.name(),
                        s.action()))
                .toList();

        return new PlannerDto.Plan(
                crop.getName(),
                sowingDate,
                sowingDate.plusDays(crop.getDurationDays()),
                milestones,
                warnings);
    }

    private boolean soilMatches(Crop crop, String soil) {
        if (soil == null) return false;
        return Arrays.stream(crop.getSoilTypes().split(","))
                .map(String::trim)
                .anyMatch(soil::equalsIgnoreCase);
    }

    private boolean inWindow(Crop crop, int month) {
        int from = crop.getSowFromMonth();
        int to = crop.getSowToMonth();
        // Windows that cross the new year, such as Nov to Jan
        return from <= to ? (month >= from && month <= to) : (month >= from || month <= to);
    }

    private String windowText(Crop crop) {
        return Month.of(crop.getSowFromMonth()).getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
                + " to "
                + Month.of(crop.getSowToMonth()).getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
    }
}