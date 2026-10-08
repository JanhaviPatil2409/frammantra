package com.farmmantra.dto;

import java.util.List;

import com.farmmantra.entity.Scheme;

public class AdvisoryDto {

    public record Current(double temperature, int humidity, double windSpeed) {}

    public record Day(String date, double tempMax, double tempMin,
                      double rainMm, int rainChance, double windMax) {}

    public record Weather(Current current, List<Day> days) {}

    public record Tip(String category, String severity, String message) {}

    public record Response(
            String location,
            Weather weather,
            String soilType,
            List<String> suitableCrops,
            List<Tip> advisories,
            List<Scheme> schemes) {}
}