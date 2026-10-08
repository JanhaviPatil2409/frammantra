package com.farmmantra.service;

import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;

@Component
public class PlantHealthClient {

    private final String apiKey;
    private final String url;
    private final RestClient http = RestClient.create();

    public PlantHealthClient(@Value("${plantid.api-key:}") String apiKey,
                             @Value("${plantid.url}") String url) {
        this.apiKey = apiKey;
        this.url = url;
    }

    /** Sends the leaf photo to Plant.id and returns the raw JSON response. */
    public JsonNode assess(byte[] imageBytes) {
        if (apiKey == null || apiKey.isBlank() || apiKey.startsWith("PASTE_")) {
            throw new IllegalStateException(
                    "Disease detection is not set up yet. Add your Plant.id API key to application.properties.");
        }

        String image = Base64.getEncoder().encodeToString(imageBytes);

        return http.post()
                .uri(url + "?details=description,treatment")
                .header("Api-Key", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("images", List.of(image)))
                .retrieve()
                .body(JsonNode.class);
    }
}