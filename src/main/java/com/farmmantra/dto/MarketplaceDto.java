package com.farmmantra.dto;

import java.time.LocalDateTime;

public class MarketplaceDto {

    public record CreateRequest(String cropName, double quantityKg, double pricePerKg,
                                String location, String description) {}

    public record ListingView(Long id, String cropName, double quantityKg, double pricePerKg,
                              String location, String description, String status,
                              String sellerName, String sellerPhone, boolean mine,
                              LocalDateTime createdAt) {}
}