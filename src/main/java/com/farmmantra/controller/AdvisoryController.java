package com.farmmantra.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.farmmantra.dto.AdvisoryDto;
import com.farmmantra.entity.Farmer;
import com.farmmantra.entity.Scheme;
import com.farmmantra.repository.FarmerRepository;
import com.farmmantra.repository.SchemeRepository;
import com.farmmantra.service.AdvisoryService;
import com.farmmantra.util.JwtUtil;

import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/advisory")
@RequiredArgsConstructor
public class AdvisoryController {

    private final JwtUtil jwtUtil;
    private final FarmerRepository farmerRepo;
    private final SchemeRepository schemeRepo;
    private final AdvisoryService advisoryService;

    @GetMapping
    public ResponseEntity<?> getAdvisory(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

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

        List<Scheme> schemes = schemeRepo.findAll();

        try {
            AdvisoryDto.Response body = advisoryService.build(farmer, schemes);
            return ResponseEntity.ok(body);
        } catch (Exception e) {
            return ResponseEntity.status(503).body("Advisory service unavailable: " + e.getMessage());
        }
    }
}