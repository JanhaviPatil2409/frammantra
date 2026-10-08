package com.farmmantra.config;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.farmmantra.entity.Crop;
import com.farmmantra.repository.CropRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CropSeeder implements CommandLineRunner {

    private final CropRepository repo;

    @Override
    public void run(String... args) {
        if (repo.count() > 0) return;

        repo.saveAll(List.of(
            crop("Cotton", "BLACK,RED", 6, 7, 160, "HIGH",
                 "Needs a long frost-free season. Watch for bollworm."),
            crop("Soybean", "BLACK", 6, 7, 100, "MEDIUM",
                 "Sensitive to waterlogging. Inoculate seed with Rhizobium."),
            crop("Groundnut", "RED,SANDY", 6, 7, 110, "LOW",
                 "Needs loose soil for pegging. Calcium helps pod filling."),
            crop("Rice (Kharif)", "ALLUVIAL,LATERITE", 6, 7, 120, "HIGH",
                 "Needs standing water during the vegetative stage."),
            crop("Ragi", "RED,LATERITE,SANDY", 6, 7, 100, "LOW",
                 "Drought tolerant. Good for dry, low-input fields."),
            crop("Wheat", "ALLUVIAL,BLACK", 10, 11, 120, "MEDIUM",
                 "Rabi crop. Irrigate at crown root and flowering stages."),
            crop("Sugarcane", "ALLUVIAL,BLACK", 2, 3, 365, "HIGH",
                 "Long duration crop. Needs regular irrigation and heavy feeding.")
        ));
    }

    private Crop crop(String name, String soils, int from, int to,
                      int days, String water, String notes) {
        Crop c = new Crop();
        c.setName(name);
        c.setSoilTypes(soils);
        c.setSowFromMonth(from);
        c.setSowToMonth(to);
        c.setDurationDays(days);
        c.setWaterNeed(water);
        c.setNotes(notes);
        return c;
    }
}