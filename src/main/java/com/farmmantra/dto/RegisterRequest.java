package com.farmmantra.dto;

import lombok.Data;

@Data
public class RegisterRequest {

    private String name;
    private String phone;
    private String password;

    private String role;      // ✅ add
    private String location;
    private String soilType;  // ✅ add

    private String email;     // optional
}