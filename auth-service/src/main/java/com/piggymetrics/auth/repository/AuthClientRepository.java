package com.piggymetrics.auth.repository;

import com.piggymetrics.auth.domain.MongoClientDetails;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthClientRepository extends MongoRepository<MongoClientDetails, String> {
    Optional<MongoClientDetails> findByClientId(String clientId);
}
