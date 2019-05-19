package com.piggymetrics.auth;

import com.piggymetrics.auth.library.document.MongoAuthorizationCode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;

import com.piggymetrics.auth.library.document.MongoClientDetails;
import com.piggymetrics.auth.library.document.MongoUser;
import com.piggymetrics.auth.library.document.MongoAccessToken;
import com.google.common.collect.Sets;

@SpringBootApplication
@EnableResourceServer
@EnableDiscoveryClient
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class AuthApplication {

    public static void main(String[] args) {
//		SpringApplication.run(AuthApplication.class, args);
        final ConfigurableApplicationContext context = SpringApplication.run(AuthApplication.class, args);
//		if (args .length > 0 && "init".equalsIgnoreCase(args[0])) {

        MongoTemplate mongoTemplate = (MongoTemplate) context.getBean(MongoTemplate.class);

        mongoTemplate.dropCollection(MongoClientDetails.class);
        mongoTemplate.dropCollection(MongoUser.class);
        mongoTemplate.dropCollection(MongoAccessToken.class);
		mongoTemplate.dropCollection(MongoAuthorizationCode.class);
        // init the users
        MongoUser mongoUser = new MongoUser();
		mongoUser.setUsername("user");
		mongoUser.setPassword("user");
		mongoUser.setRoles(Sets.newHashSet(("ROLE_USER")));
		mongoTemplate.save(mongoUser);

        // init the client details
        MongoClientDetails clientDetails = new MongoClientDetails();
        clientDetails.setClientId("browser");
        clientDetails.setAuthorizedGrantTypes(Sets.newHashSet("refresh_token", "password"));
        clientDetails.setScope(Sets.newHashSet("ui"));
        clientDetails.setClientSecret("$2a$10$jogpg/bxiyabTqOOUt.CLekoi3N36qYVt3oZEMuAxuQANxlx58nku");
        clientDetails.setSecretRequired(true);
//        clientDetails.setResourceIds(Sets.newHashSet("foo"));
        clientDetails.setAuthorities(AuthorityUtils.createAuthorityList("ROLE_USER"));
//        clientDetails.setRegisteredRedirectUri(Sets.newHashSet("http://auth-service:5000/uaa/oauth/token"));
//        clientDetails.setAccessTokenValiditySeconds(60);
//        clientDetails.setRefreshTokenValiditySeconds(14400);
//        clientDetails.setAutoApprove(false);
        mongoTemplate.save(clientDetails);

        MongoClientDetails clientDetails2 = new MongoClientDetails();
        clientDetails2.setClientId("account-service");
        clientDetails2.setAuthorizedGrantTypes(Sets.newHashSet("client_credentials", "refresh_token"));
        clientDetails2.setScope(Sets.newHashSet("server"));
        clientDetails2.setClientSecret("$2a$10$jogpg/bxiyabTqOOUt.CLekoi3N36qYVt3oZEMuAxuQANxlx58nku");
//        clientDetails.setRegisteredRedirectUri(Sets.newHashSet("http://auth-service:5000/uaa/oauth/token"));
        clientDetails.setAuthorities(AuthorityUtils.createAuthorityList("ROLE_USER"));
        mongoTemplate.save(clientDetails2);

        MongoClientDetails clientDetails3 = new MongoClientDetails();
        clientDetails3.setClientId("statistics-service");
        clientDetails3.setAuthorizedGrantTypes(Sets.newHashSet("client_credentials", "refresh_token"));
        clientDetails3.setScope(Sets.newHashSet("server"));
        clientDetails3.setClientSecret("$2a$10$jogpg/bxiyabTqOOUt.CLekoi3N36qYVt3oZEMuAxuQANxlx58nku");
//        clientDetails.setRegisteredRedirectUri(Sets.newHashSet("http://auth-service:5000/uaa/oauth/token"));
        clientDetails.setAuthorities(AuthorityUtils.createAuthorityList("ROLE_USER"));
        mongoTemplate.save(clientDetails3);

        MongoClientDetails clientDetails4 = new MongoClientDetails();
        clientDetails4.setClientId("notification-service");
        clientDetails4.setAuthorizedGrantTypes(Sets.newHashSet("client_credentials", "refresh_token"));
        clientDetails4.setScope(Sets.newHashSet("server"));
        clientDetails4.setClientSecret("$2a$10$jogpg/bxiyabTqOOUt.CLekoi3N36qYVt3oZEMuAxuQANxlx58nku");
//        clientDetails.setRegisteredRedirectUri(Sets.newHashSet("http://auth-service:5000/uaa/oauth/token"));
        clientDetails.setAuthorities(AuthorityUtils.createAuthorityList("ROLE_USER"));
        mongoTemplate.save(clientDetails4);

        System.out.println("init complete!");

//		}
    }

}
