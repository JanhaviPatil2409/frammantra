package com.farmmantra.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.farmmantra.dto.AdvisoryDto;
import com.farmmantra.entity.Farmer;
import com.farmmantra.entity.Scheme;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdvisoryService {

    private final WeatherService weatherService;

    public AdvisoryDto.Response build(Farmer farmer, List<Scheme> schemes) {
        WeatherService.Coordinates coords = weatherService.geocode(farmer.getLocation());
        AdvisoryDto.Weather weather = weatherService.getForecast(coords);

        String soil = farmer.getSoilType() == null ? "UNKNOWN" : farmer.getSoilType();

        List<AdvisoryDto.Tip> tips = new ArrayList<>();
        tips.addAll(weatherTips(weather));
        tips.addAll(soilTips(soil));

        return new AdvisoryDto.Response(
                coords.resolvedName(),
                weather,
                soil,
                suitableCrops(soil),
                tips,
                schemes);
    }

    private List<AdvisoryDto.Tip> weatherTips(AdvisoryDto.Weather w) {
        List<AdvisoryDto.Tip> tips = new ArrayList<>();
        List<AdvisoryDto.Day> days = w.days();
        if (days.isEmpty()) return tips;

        // Rain in the next 3 days
        double rain3 = days.stream().limit(3).mapToDouble(AdvisoryDto.Day::rainMm).sum();
        if (rain3 > 20) {
            tips.add(new AdvisoryDto.Tip("Irrigation & fertilizer", "warning",
                    "About " + Math.round(rain3) + " mm of rain is expected in the next 3 days. "
                    + "Postpone fertilizer application and check field drainage."));
        }

        // Spraying conditions today
        AdvisoryDto.Day today = days.get(0);
        if (today.rainChance() >= 60) {
            tips.add(new AdvisoryDto.Tip("Pest control", "warning",
                    "Rain chance today is " + today.rainChance() + "%. Hold off on pesticide or fungicide spraying."));
        }
        if (today.windMax() >= 25) {
            tips.add(new AdvisoryDto.Tip("Pest control", "warning",
                    "Winds up to " + Math.round(today.windMax()) + " km/h. Avoid spraying to prevent drift."));
        }

        // Heat and cold
        double maxTemp = days.stream().mapToDouble(AdvisoryDto.Day::tempMax).max().orElse(0);
        double minTemp = days.stream().mapToDouble(AdvisoryDto.Day::tempMin).min().orElse(0);
        if (maxTemp >= 38) {
            tips.add(new AdvisoryDto.Tip("Heat stress", "danger",
                    "Temperatures reach " + Math.round(maxTemp) + " °C. Irrigate early morning or evening "
                    + "and mulch around plants to conserve moisture."));
        }
        if (minTemp <= 10) {
            tips.add(new AdvisoryDto.Tip("Cold protection", "warning",
                    "Nights drop to " + Math.round(minTemp) + " °C. Protect vegetables and young seedlings from frost."));
        }

        if (tips.isEmpty()) {
            tips.add(new AdvisoryDto.Tip("General", "info",
                    "Weather is favourable for routine field work this week."));
        }
        return tips;
    }

    private List<AdvisoryDto.Tip> soilTips(String soil) {
        String message = switch (soil) {
            case "BLACK" -> "Black soil holds moisture well. Keep drainage channels clear before the monsoon to avoid waterlogging.";
            case "RED" -> "Red soil is often low in nitrogen and organic matter. Add farmyard manure or compost each season.";
            case "ALLUVIAL" -> "Alluvial soil is fertile. Balance NPK use and test the soil every season or two.";
            case "LATERITE" -> "Laterite soil drains quickly and tends to be acidic. Apply lime as per your soil test and add organic matter.";
            case "SANDY" -> "Sandy soil loses water quickly. Use light, frequent irrigation and mulch the surface.";
            default -> "Set your soil type in your profile to receive soil-specific advice.";
        };
        return List.of(new AdvisoryDto.Tip("Soil care", "info", message));
    }

    private List<String> suitableCrops(String soil) {
        return switch (soil) {
            case "BLACK" -> List.of("Cotton", "Soybean", "Sugarcane", "Jowar");
            case "RED" -> List.of("Groundnut", "Ragi", "Millets", "Pulses");
            case "ALLUVIAL" -> List.of("Wheat", "Rice", "Sugarcane", "Maize");
            case "LATERITE" -> List.of("Cashew", "Tapioca", "Ragi", "Coconut");
            case "SANDY" -> List.of("Groundnut", "Watermelon", "Bajra", "Vegetables");
            default -> List.of();
        };
    }
}