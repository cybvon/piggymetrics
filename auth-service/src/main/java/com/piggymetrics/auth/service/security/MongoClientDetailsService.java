package com.piggymetrics.auth.service.security;

import com.piggymetrics.auth.repository.AuthClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.stereotype.Service;

@Service
public class MongoClientDetailsService implements ClientDetailsService {

    @Autowired
    private AuthClientRepository Repository;

    @Override
    public ClientDetails loadClientByClientId(String clientId) {
        return Repository.findByClientId(clientId).orElseThrow(IllegalArgumentException::new);
    }
}
