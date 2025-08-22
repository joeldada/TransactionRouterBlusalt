package com.blusalt.crypto.repository;

import com.blusalt.crypto.model.RsaKeypair;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RsaKeypairRepo extends JpaRepository<RsaKeypair, Long> {

    Optional<RsaKeypair> findByActiveTrue();

    Optional<RsaKeypair> findByKeyId(String keyId);

}
