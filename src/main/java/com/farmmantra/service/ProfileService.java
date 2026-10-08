package com.farmmantra.service;

import java.util.Locale;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.farmmantra.dto.ProfileDto;
import com.farmmantra.entity.Farmer;
import com.farmmantra.repository.FarmerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private static final Set<String> SOIL_TYPES =
            Set.of("BLACK", "RED", "ALLUVIAL", "LATERITE", "SANDY");

    private final FarmerRepository farmerRepo;
    private final PasswordEncoder encoder;

    public ProfileDto.View view(Farmer farmer) {
        return toView(farmer);
    }

    public ProfileDto.View update(Farmer farmer, ProfileDto.UpdateRequest req) {
        if (req.name() == null || req.name().isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (req.location() == null || req.location().isBlank()) {
            throw new IllegalArgumentException("Location is required");
        }
        String soil = req.soilType() == null ? "" : req.soilType().trim().toUpperCase(Locale.ROOT);
        if (!SOIL_TYPES.contains(soil)) {
            throw new IllegalArgumentException("Please choose a valid soil type");
        }

        farmer.setName(req.name().trim());
        farmer.setLocation(req.location().trim());
        farmer.setSoilType(soil);
        farmerRepo.save(farmer);

        return toView(farmer);
    }

    public void changePassword(Farmer farmer, ProfileDto.PasswordRequest req) {
        if (req.currentPassword() == null || req.currentPassword().isBlank()
                || !encoder.matches(req.currentPassword(), farmer.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        if (req.newPassword() == null || req.newPassword().length() < 6) {
            throw new IllegalArgumentException("New password must be at least 6 characters");
        }

        farmer.setPassword(encoder.encode(req.newPassword()));
        farmerRepo.save(farmer);
    }

    private ProfileDto.View toView(Farmer f) {
        return new ProfileDto.View(
                f.getId(),
                f.getName(),
                f.getPhone(),
                f.getRole(),
                f.getLocation(),
                f.getSoilType());
    }
}