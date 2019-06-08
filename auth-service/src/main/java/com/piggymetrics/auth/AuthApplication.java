package com.piggymetrics.auth;


import com.piggymetrics.auth.domain.MongoClientDetails;
import com.piggymetrics.auth.domain.MongoAccessToken;
import com.piggymetrics.auth.domain.MongoRefreshToken;
import com.piggymetrics.auth.domain.User;
import com.piggymetrics.auth.enums.Authorities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;

import java.util.HashSet;
import java.util.Set;

@SpringBootApplication
@EnableResourceServer
@EnableDiscoveryClient
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class AuthApplication {

    @Autowired
    private Environment env;

    public static void main(String[] args) {
//		SpringApplication.run(AuthApplication.class, args);
        final ConfigurableApplicationContext context = SpringApplication.run(AuthApplication.class, args);


        MongoTemplate mongoTemplate = (MongoTemplate) context.getBean(MongoTemplate.class);

        mongoTemplate.dropCollection(MongoClientDetails.class);
        mongoTemplate.dropCollection(User.class);
        mongoTemplate.dropCollection(MongoAccessToken.class);
        mongoTemplate.dropCollection(MongoRefreshToken.class);

        //////////////////MongoClientDetails////////////////
        MongoClientDetails browserClientDetails = new MongoClientDetails();
        browserClientDetails.setClientId("browser");
//        browserClientDetails.setClientSecret("$2a$10$jogpg/bxiyabTqOOUt.CLekoi3N36qYVt3oZEMuAxuQANxlx58nku");
        browserClientDetails.setScopes("ui");
        browserClientDetails.setGrantTypes("refresh_token,password");
        mongoTemplate.save(browserClientDetails);

        //////////////////////////////////
        Set<Authorities> authorities = new HashSet<>();
        authorities.add(Authorities.ROLE_USER);

        User user = new User();
//        user.setActivated(true);
//        user.setAuthorities(authorities);
        user.setPassword("$2a$10$jogpg/bxiyabTqOOUt.CLekoi3N36qYVt3oZEMuAxuQANxlx58nku");
        user.setUsername("admin");
        mongoTemplate.save(user);

        //////////////////////////////////
        MongoClientDetails accountServiceClientDetails = new MongoClientDetails();
        accountServiceClientDetails.setClientId("account-service");
        accountServiceClientDetails.setClientSecret("$2a$10$jogpg/bxiyabTqOOUt.CLekoi3N36qYVt3oZEMuAxuQANxlx58nku");
        accountServiceClientDetails.setScopes("server");
        accountServiceClientDetails.setGrantTypes("refresh_token,client_credentials");
//        accountServiceClientDetails.setClientSecret(env.getProperty("ACCOUNT_SERVICE_PASSWORD"));
        mongoTemplate.save(accountServiceClientDetails);

        MongoClientDetails statisticsServiceClientDetails = new MongoClientDetails();
        statisticsServiceClientDetails.setClientId("statistics-service");
        statisticsServiceClientDetails.setClientSecret("$2a$10$jogpg/bxiyabTqOOUt.CLekoi3N36qYVt3oZEMuAxuQANxlx58nku");
        statisticsServiceClientDetails.setScopes("server");
        statisticsServiceClientDetails.setGrantTypes("refresh_token,client_credentials");
        mongoTemplate.save(statisticsServiceClientDetails);

//        notification-service

        System.out.println("init complete*");


    }

}
