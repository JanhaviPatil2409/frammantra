package com.farmmantra.controller;

import java.util.Map;
import java.util.function.Function;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.farmmantra.dto.ProfileDto;
import com.farmmantra.entity.Farmer;
import com.farmmantra.repository.FarmerRepository;
import com.farmmantra.service.ProfileService;
import com.farmmantra.util.JwtUtil;

import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final JwtUtil jwtUtil;
    private final FarmerRepository farmerRepo;
    private final ProfileService service;

    @GetMapping
    public ResponseEntity<?> get(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return withFarmer(authHeader, service::view);
    }

    @PutMapping
    public ResponseEntity<?> update(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody ProfileDto.UpdateRequest request) {
        return withFarmer(authHeader, f -> service.update(f, request));
    }

    @PostMapping("/password")
    public ResponseEntity<?> changePassword(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody ProfileDto.PasswordRequest request) {
        return withFarmer(authHeader, f -> {
            service.changePassword(f, request);
            return Map.of("changed", true);
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
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}