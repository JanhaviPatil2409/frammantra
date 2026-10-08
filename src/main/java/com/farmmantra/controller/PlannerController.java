package com.farmmantra.controller;

import java.time.LocalDate;
import java.util.function.Function;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.farmmantra.entity.Farmer;
import com.farmmantra.repository.FarmerRepository;
import com.farmmantra.service.CropPlannerService;
import com.farmmantra.util.JwtUtil;

import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/planner")
@RequiredArgsConstructor
public class PlannerController {

    private final JwtUtil jwtUtil;
    private final FarmerRepository farmerRepo;
    private final CropPlannerService planner;

    @GetMapping("/crops")
    public ResponseEntity<?> crops(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return withFarmer(authHeader, farmer -> planner.cropsFor(farmer));
    }

    @GetMapping("/plan")
    public ResponseEntity<?> plan(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam Long cropId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate sowingDate) {
        return withFarmer(authHeader, farmer -> planner.plan(farmer, cropId, sowingDate));
    }

    /** Validates the JWT, loads the farmer, and runs the action. */
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