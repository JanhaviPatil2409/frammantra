package com.farmmantra.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;

import com.farmmantra.dto.DiseaseDto;
import com.farmmantra.entity.Detection;
import com.farmmantra.entity.Farmer;
import com.farmmantra.repository.DetectionRepository;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DiseaseService {

    private static final long MAX_BYTES = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png");
    private static final double LOW_CONFIDENCE = 0.5;

    private final PlantHealthClient client;
    private final DetectionRepository repo;

    public DiseaseDto.Result detect(Farmer farmer, String cropName, MultipartFile file) {
        if (cropName == null || cropName.isBlank()) {
            throw new IllegalArgumentException("Please choose a crop");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Please upload a leaf photo");
        }
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("Only JPG or PNG images are allowed");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new IllegalArgumentException("Image must be 5 MB or smaller");
        }

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not read the uploaded image");
        }

        JsonNode root = callApi(bytes);
        Detection d = parse(root, farmer, cropName.trim());
        repo.save(d);

        return toResult(d);
    }

    public List<DiseaseDto.HistoryItem> history(Farmer farmer) {
        return repo.findByFarmerIdOrderByCreatedAtDesc(farmer.getId()).stream()
                .map(d -> new DiseaseDto.HistoryItem(
                        d.getId(), d.getCropName(), d.isHealthy(),
                        d.getDiseaseName(), d.getConfidence(), d.getCreatedAt()))
                .toList();
    }

    private JsonNode callApi(byte[] bytes) {
        JsonNode root;
        try {
            root = client.assess(bytes);
        } catch (HttpClientErrorException e) {
            throw new IllegalStateException(explain(e.getStatusCode().value()));
        } catch (RestClientException e) {
            throw new IllegalStateException(
                    "Could not reach the disease detection service. Check your internet connection.");
        }
        if (root == null) {
            throw new IllegalStateException("Empty response from the disease detection service");
        }
        return root;
    }

    private Detection parse(JsonNode root, Farmer farmer, String cropName) {
        JsonNode result = root.path("result");
        boolean healthy = result.path("is_healthy").path("binary").asBoolean(false);
        JsonNode top = result.path("disease").path("suggestions").path(0);
        JsonNode details = top.path("details");
        JsonNode treatment = details.path("treatment");

        Detection d = new Detection();
        d.setFarmerId(farmer.getId());
        d.setCropName(cropName);
        d.setHealthy(healthy);
        d.setCreatedAt(LocalDateTime.now());

        if (healthy) {
            d.setDiseaseName("Healthy plant");
            d.setConfidence(result.path("is_healthy").path("probability").asDouble(0));
        } else {
            d.setDiseaseName(top.path("name").asText("Unknown issue"));
            d.setConfidence(top.path("probability").asDouble(0));
            d.setDescription(details.path("description").asText(""));
            d.setChemical(join(toList(treatment.path("chemical"))));
            d.setOrganic(join(toList(treatment.path("biological"))));
            d.setPrevention(join(toList(treatment.path("prevention"))));
        }
        return d;
    }

    private DiseaseDto.Result toResult(Detection d) {
        String warning = null;
        if (!d.isHealthy() && d.getConfidence() < LOW_CONFIDENCE) {
            warning = "Low confidence result. Check the photo, or confirm with your local KVK before using any treatment.";
        }
        return new DiseaseDto.Result(
                d.getId(),
                d.getCropName(),
                d.isHealthy(),
                d.getDiseaseName(),
                d.getConfidence(),
                d.getDescription(),
                split(d.getChemical()),
                split(d.getOrganic()),
                split(d.getPrevention()),
                warning,
                d.getCreatedAt());
    }

    private List<String> toList(JsonNode node) {
        List<String> out = new ArrayList<>();
        if (node.isArray()) {
            node.forEach(n -> {
                String s = n.asText();
                if (!s.isBlank()) out.add(s);
            });
        }
        return out;
    }

    private String join(List<String> items) {
        return String.join("\n", items);
    }

    private List<String> split(String text) {
        if (text == null || text.isBlank()) return List.of();
        return Arrays.asList(text.split("\n"));
    }

    private String explain(int status) {
        return switch (status) {
            case 401, 403 -> "Plant.id rejected the API key. Check plantid.api-key in application.properties.";
            case 402 -> "Your Plant.id credits are used up. Check your account or buy more credits.";
            case 429 -> "Too many requests to Plant.id. Please try again in a minute.";
            default -> "The disease detection service returned an error (status " + status + ").";
        };
    }
}