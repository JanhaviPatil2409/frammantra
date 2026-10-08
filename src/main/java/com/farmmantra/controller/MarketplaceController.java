package com.farmmantra.controller;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.farmmantra.dto.MarketplaceDto;
import com.farmmantra.entity.Farmer;
import com.farmmantra.repository.FarmerRepository;
import com.farmmantra.service.MarketplaceService;
import com.farmmantra.util.JwtUtil;

import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/marketplace")
@RequiredArgsConstructor
public class MarketplaceController {

    private final JwtUtil jwtUtil;
    private final FarmerRepository farmerRepo;
    private final MarketplaceService service;

    @GetMapping("/listings")
    public ResponseEntity<?> browse(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) String crop,
            @RequestParam(required = false) String location) {
        return withFarmer(authHeader, f -> service.browse(f, crop, location));
    }

    @GetMapping("/mine")
    public ResponseEntity<?> mine(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return withFarmer(authHeader, service::mine);
    }

    @PostMapping("/listings")
    public ResponseEntity<?> create(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody MarketplaceDto.CreateRequest request) {
        return withFarmer(authHeader, f -> service.create(f, request));
    }

    @PatchMapping("/listings/{id}/status")
    public ResponseEntity<?> setStatus(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestParam String value) {
        return withFarmer(authHeader, f -> service.setStatus(f, id, value));
    }

    @DeleteMapping("/listings/{id}")
    public ResponseEntity<?> delete(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        return withFarmer(authHeader, f -> {
            service.delete(f, id);
            return Map.of("deleted", true);
        });
    }

    /** Validates the JWT, loads the farmer, runs the action, and maps errors to HTTP codes. */
    private ResponseEntity<?> withFarmer(String authHeader, Function<Farmer, Object> action) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Missing token");
        }

        String phone;
        try {
            phone = jwtUtil.extractPhone(authHeader.substring(7));
        } catch (JwtException | IllegalArgumentException e) {
            return ResponseEntity.status(401).body("Invalid or expired token");
        }

        Farmer farmer = farmerRepo.findByPhone(phone).orElse(null);
        if (farmer == null) {
            return ResponseEntity.status(404).body("Farmer not found");
        }

        try {
            return ResponseEntity.ok(action.apply(farmer));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}