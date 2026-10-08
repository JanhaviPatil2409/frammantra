package com.farmmantra.config;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.farmmantra.entity.Scheme;
import com.farmmantra.repository.SchemeRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SchemeSeeder implements CommandLineRunner {

    private final SchemeRepository repo;

    @Override
    public void run(String... args) {
        if (repo.count() > 0) return;

        repo.saveAll(List.of(
            scheme("PM-KISAN", "Income support",
                "Direct income support of Rs 6,000 per year in three instalments to eligible farmer families.",
                "Land-owning farmer families, subject to the scheme's exclusion criteria.",
                "https://pmkisan.gov.in"),

            scheme("PMFBY", "Crop insurance",
                "Pradhan Mantri Fasal Bima Yojana provides insurance cover against crop loss from natural calamities, pests and diseases.",
                "Farmers growing notified crops in notified areas. Enrolment is usually needed before the sowing deadline.",
                "https://pmfby.gov.in"),

            scheme("Soil Health Card", "Soil testing",
                "Free soil testing with a card that recommends the fertilizer doses your field needs.",
                "All farmers. Samples are collected by the agriculture department.",
                "https://soilhealth.dac.gov.in"),

            scheme("Kisan Credit Card", "Credit",
                "Short-term credit for crop cultivation and farm needs at subsidised interest rates.",
                "Farmers, tenant farmers and sharecroppers. Apply through a bank or cooperative society.",
                "https://www.myscheme.gov.in")
        ));
    }

    private Scheme scheme(String name, String category, String description,
                          String eligibility, String link) {
        Scheme s = new Scheme();
        s.setName(name);
        s.setCategory(category);
        s.setDescription(description);
        s.setEligibility(eligibility);
        s.setLink(link);
        return s;
    }
}