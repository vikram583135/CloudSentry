package com.devops.platform.metrics.service;

import com.devops.platform.common.exception.BadRequestException;
import com.devops.platform.common.exception.ResourceNotFoundException;
import com.devops.platform.metrics.dto.ApplicationRequest;
import com.devops.platform.metrics.dto.ApplicationResponse;
import com.devops.platform.metrics.model.Application;
import com.devops.platform.metrics.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for application management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private static final SecureRandom secureRandom = new SecureRandom();

    /**
     * Creates a new application.
     */
    @Transactional
    @CacheEvict(value = "applications", allEntries = true)
    public ApplicationResponse createApplication(ApplicationRequest request) {
        if (applicationRepository.existsByName(request.getName())) {
            throw new BadRequestException("Application with name '" + request.getName() + "' already exists");
        }

        Application application = Application.builder()
                .name(request.getName())
                .displayName(request.getDisplayName() != null ? request.getDisplayName() : request.getName())
                .description(request.getDescription())
                .environment(request.getEnvironment())
                .team(request.getTeam())
                .owner(request.getOwner())
                .apiKey(generateApiKey())
                .active(true)
                .build();

        application = applicationRepository.save(application);
        log.info("Created application: id={}, name={}", application.getId(), application.getName());
        
        return toResponse(application);
    }

    /**
     * Gets an application by ID.
     */
    @Cacheable(value = "applications", key = "#id")
    public ApplicationResponse getApplication(UUID id) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application", "id", id));
        return toResponse(application);
    }

    /**
     * Gets an application by name.
     */
    public ApplicationResponse getApplicationByName(String name) {
        Application application = applicationRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Application", "name", name));
        return toResponse(application);
    }

    /**
     * Gets all active applications.
     */
    @Cacheable(value = "applications", key = "'active'")
    public List<ApplicationResponse> getActiveApplications() {
        return applicationRepository.findByActiveTrue()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Gets all applications.
     */
    public List<ApplicationResponse> getAllApplications() {
        return applicationRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Updates an application.
     */
    @Transactional
    @CacheEvict(value = "applications", allEntries = true)
    public ApplicationResponse updateApplication(UUID id, ApplicationRequest request) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application", "id", id));

        // Check if name is being changed to an existing name
        if (!application.getName().equals(request.getName()) && 
            applicationRepository.existsByName(request.getName())) {
            throw new BadRequestException("Application with name '" + request.getName() + "' already exists");
        }

        application.setName(request.getName());
        application.setDisplayName(request.getDisplayName());
        application.setDescription(request.getDescription());
        application.setEnvironment(request.getEnvironment());
        application.setTeam(request.getTeam());
        application.setOwner(request.getOwner());

        application = applicationRepository.save(application);
        log.info("Updated application: id={}, name={}", application.getId(), application.getName());
        
        return toResponse(application);
    }

    /**
     * Deactivates an application.
     */
    @Transactional
    @CacheEvict(value = "applications", allEntries = true)
    public void deactivateApplication(UUID id) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application", "id", id));
        
        application.setActive(false);
        applicationRepository.save(application);
        log.info("Deactivated application: id={}, name={}", id, application.getName());
    }

    /**
     * Regenerates the API key for an application.
     */
    @Transactional
    @CacheEvict(value = "applications", key = "#id")
    public ApplicationResponse regenerateApiKey(UUID id) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application", "id", id));
        
        application.setApiKey(generateApiKey());
        application = applicationRepository.save(application);
        log.info("Regenerated API key for application: id={}", id);
        
        return toResponse(application);
    }

    /**
     * Validates an API key and returns the application ID.
     */
    public UUID validateApiKey(String apiKey) {
        Application application = applicationRepository.findByApiKey(apiKey)
                .orElseThrow(() -> new BadRequestException("Invalid API key"));
        
        if (!application.getActive()) {
            throw new BadRequestException("Application is not active");
        }
        
        return application.getId();
    }

    private String generateApiKey() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return "cs_" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private ApplicationResponse toResponse(Application application) {
        return ApplicationResponse.builder()
                .id(application.getId())
                .name(application.getName())
                .displayName(application.getDisplayName())
                .description(application.getDescription())
                .environment(application.getEnvironment())
                .team(application.getTeam())
                .owner(application.getOwner())
                .apiKey(application.getApiKey())
                .active(application.getActive())
                .createdAt(application.getCreatedAt())
                .updatedAt(application.getUpdatedAt())
                .build();
    }
}
