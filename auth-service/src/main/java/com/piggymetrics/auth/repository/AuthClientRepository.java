package com.piggymetrics.auth.repository;

import com.piggymetrics.auth.domain.MongoClientDetails;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthClientRepository extends CrudRepository<MongoClientDetails, String> {
    Optional<MongoClientDetails> findByClientId(String clientId);
}
