package com.farmmantra.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.farmmantra.dto.MarketplaceDto;
import com.farmmantra.entity.Farmer;
import com.farmmantra.entity.Listing;
import com.farmmantra.repository.FarmerRepository;
import com.farmmantra.repository.ListingRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MarketplaceService {

    private static final String AVAILABLE = "AVAILABLE";
    private static final String SOLD = "SOLD";

    private final ListingRepository listingRepo;
    private final FarmerRepository farmerRepo;

    public MarketplaceDto.ListingView create(Farmer farmer, MarketplaceDto.CreateRequest req) {
        if (!"FARMER".equalsIgnoreCase(farmer.getRole())) {
            throw new SecurityException("Only farmer accounts can post listings");
        }
        if (req.cropName() == null || req.cropName().isBlank()) {
            throw new IllegalArgumentException("Crop name is required");
        }
        if (req.quantityKg() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        if (req.pricePerKg() <= 0) {
            throw new IllegalArgumentException("Price must be greater than 0");
        }
        if (req.location() == null || req.location().isBlank()) {
            throw new IllegalArgumentException("Location is required");
        }

        Listing l = new Listing();
        l.setSellerId(farmer.getId());
        l.setCropName(req.cropName().trim());
        l.setQuantityKg(req.quantityKg());
        l.setPricePerKg(req.pricePerKg());
        l.setLocation(req.location().trim());
        l.setDescription(req.description() == null ? null : req.description().trim());
        l.setStatus(AVAILABLE);
        l.setCreatedAt(LocalDateTime.now());
        listingRepo.save(l);

        return toViews(List.of(l), farmer).get(0);
    }

    /** Available listings only, optionally filtered by crop and location. */
    public List<MarketplaceDto.ListingView> browse(Farmer viewer, String crop, String location) {
        String c = crop == null ? "" : crop.trim().toLowerCase(Locale.ROOT);
        String loc = location == null ? "" : location.trim().toLowerCase(Locale.ROOT);

        List<Listing> filtered = listingRepo.findByStatusOrderByCreatedAtDesc(AVAILABLE).stream()
                .filter(l -> c.isEmpty() || l.getCropName().toLowerCase(Locale.ROOT).contains(c))
                .filter(l -> loc.isEmpty() || l.getLocation().toLowerCase(Locale.ROOT).contains(loc))
                .toList();

        return toViews(filtered, viewer);
    }

    /** All listings posted by this farmer, including sold ones. */
    public List<MarketplaceDto.ListingView> mine(Farmer farmer) {
        return toViews(listingRepo.findBySellerIdOrderByCreatedAtDesc(farmer.getId()), farmer);
    }

    public MarketplaceDto.ListingView setStatus(Farmer farmer, Long id, String status) {
        if (!AVAILABLE.equals(status) && !SOLD.equals(status)) {
            throw new IllegalArgumentException("Status must be AVAILABLE or SOLD");
        }
        Listing l = owned(farmer, id);
        l.setStatus(status);
        listingRepo.save(l);
        return toViews(List.of(l), farmer).get(0);
    }

    public void delete(Farmer farmer, Long id) {
        listingRepo.delete(owned(farmer, id));
    }

    private Listing owned(Farmer farmer, Long id) {
        Listing l = listingRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Listing not found"));
        if (!l.getSellerId().equals(farmer.getId())) {
            throw new SecurityException("You can only change your own listings");
        }
        return l;
    }

    /** Converts listings to views, loading all sellers in one query. */
    private List<MarketplaceDto.ListingView> toViews(List<Listing> listings, Farmer viewer) {
        Map<Long, Farmer> sellers = farmerRepo.findAllById(
                        listings.stream().map(Listing::getSellerId).distinct().toList())
                .stream()
                .collect(Collectors.toMap(Farmer::getId, Function.identity()));

        return listings.stream()
                .map(l -> toView(l, viewer, sellers.get(l.getSellerId())))
                .toList();
    }

    private MarketplaceDto.ListingView toView(Listing l, Farmer viewer, Farmer seller) {
        return new MarketplaceDto.ListingView(
                l.getId(),
                l.getCropName(),
                l.getQuantityKg(),
                l.getPricePerKg(),
                l.getLocation(),
                l.getDescription(),
                l.getStatus(),
                seller != null ? seller.getName() : "Unknown",
                seller != null ? seller.getPhone() : "",
                l.getSellerId().equals(viewer.getId()),
                l.getCreatedAt());
    }
}