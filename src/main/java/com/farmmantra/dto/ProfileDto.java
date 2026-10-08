package com.farmmantra.dto;

public class ProfileDto {

    public record View(Long id, String name, String phone, String role,
                       String location, String soilType) {}

    public record UpdateRequest(String name, String location, String soilType) {}

    public record PasswordRequest(String currentPassword, String newPassword) {}
}