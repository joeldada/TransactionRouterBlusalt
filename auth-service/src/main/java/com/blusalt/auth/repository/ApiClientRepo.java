package com.blusalt.auth.repository;

import com.blusalt.auth.model.ApiClient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApiClientRepo extends JpaRepository<ApiClient, Long> {

    Optional<ApiClient> findByClientIdAndActiveTrue(String clientId);

}
