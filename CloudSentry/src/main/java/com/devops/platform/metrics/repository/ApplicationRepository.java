package com.devops.platform.metrics.repository;

import com.devops.platform.metrics.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Application entities.
 */
@Repository
public interface ApplicationRepository extends JpaRepository<Application, UUID> {

    Optional<Application> findByName(String name);

    Optional<Application> findByApiKey(String apiKey);

    List<Application> findByActiveTrue();

    List<Application> findByTeam(String team);

    List<Application> findByEnvironment(String environment);

    boolean existsByName(String name);

    boolean existsByApiKey(String apiKey);
}
