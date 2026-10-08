package com.farmmantra.controller;

import java.util.function.Function;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.farmmantra.entity.Farmer;
import com.farmmantra.repository.FarmerRepository;
import com.farmmantra.service.DiseaseService;
import com.farmmantra.util.JwtUtil;

import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/disease")
@RequiredArgsConstructor
public class DiseaseController {

    private final JwtUtil jwtUtil;
    private final FarmerRepository farmerRepo;
    private final DiseaseService service;

    @PostMapping(value = "/detect", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> detect(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam("file") MultipartFile file,
            @RequestParam String cropName) {
        return withFarmer(authHeader, f -> service.detect(f, cropName, file));
    }

    @GetMapping("/history")
    public ResponseEntity<?> history(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return withFarmer(authHeader, service::history);
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
        } catch (IllegalStateException e) {
            return ResponseEntity.status(503).body(e.getMessage());
        } catch (Exception e) {
            log.error("Disease detection failed", e);
            return ResponseEntity.status(500).body("Detection failed: " + e.getClass().getSimpleName()
                    + " - " + e.getMessage());
        }
    }
}