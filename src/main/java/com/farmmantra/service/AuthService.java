package com.farmmantra.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.farmmantra.dto.AuthResponse;
import com.farmmantra.dto.LoginRequest;
import com.farmmantra.dto.RegisterRequest;
import com.farmmantra.entity.Farmer;
import com.farmmantra.repository.FarmerRepository;
import com.farmmantra.util.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final FarmerRepository repo;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder encoder;

    // ✅ REGISTER
    public String register(RegisterRequest request) {

        // 🔍 check if phone already exists
        if (repo.findByPhone(request.getPhone()).isPresent()) {
            throw new RuntimeException("Phone already registered");
        }

        Farmer farmer = new Farmer();

        // 🧾 basic details
        farmer.setName(request.getName());
        farmer.setPhone(request.getPhone());
        farmer.setPassword(encoder.encode(request.getPassword()));

        // 🧠 role (safe default)
        farmer.setRole(
                request.getRole() != null ? request.getRole() : "FARMER"
        );

        // 🌍 additional details
        farmer.setLocation(request.getLocation());
        farmer.setSoilType(request.getSoilType());
        

        repo.save(farmer);

        return "Registered Successfully";
    }

    // ✅ LOGIN
    public AuthResponse login(LoginRequest request) {

        Farmer farmer = repo.findByPhone(request.getPhone())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 🔐 password check
        if (!encoder.matches(request.getPassword(), farmer.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        // 🔑 generate JWT
        String token = jwtUtil.generateToken(farmer.getPhone());

        return new AuthResponse(
                token,
                farmer.getRole(),
                farmer.getName(),
                farmer.getId()
        );
    }
}
