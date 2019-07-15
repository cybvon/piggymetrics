package com.piggymetrics.auth.domain;

import com.piggymetrics.auth.repository.TokenRepository;
import com.piggymetrics.auth.util.SerializableObjectConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.AuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.DefaultAuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.TokenStore;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Document
public class Token {

    public static final String TOKEN_ID = "tokenId";
    public static final String REFRESH_TOKEN = "refreshTokenKey";
    public static final String AUTHENTICATION_ID = "authenticationId";
    public static final String CLIENT_ID = "clientId";
    public static final String USER_NAME = "username";

    public static final String REFRESH_ID = "refreshId";

    @Id
    private String id;

    private String tokenId;
    private OAuth2AccessToken token;
    private String authenticationId;
    private String username;
    private String clientId;
    private String authentication;
    private String refreshTokenKey;

    private OAuth2RefreshToken refresh;
    private String refreshId;
    private String refreshAuthentication;

    public void setRefresh(OAuth2RefreshToken refreshToken) {
        this.refresh = refreshToken;
    }

    public OAuth2RefreshToken getRefresh() {
        return this.refresh;
    }

    public String getRefreshId() {
        return refreshId;
    }

    public void setRefreshId(String refreshId) {
        this.refreshId = refreshId;
    }

    public OAuth2Authentication getRefreshAuthentication() {
        return SerializableObjectConverter.deserialize(refreshAuthentication);
    }

    public void setRefreshAuthentication(OAuth2Authentication authentication) {
        this.refreshAuthentication = SerializableObjectConverter.serialize(authentication);
    }

    ///////////////////////////////////

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    public OAuth2AccessToken getToken() {
        return token;
    }

    public void setToken(OAuth2AccessToken token) {
        this.token = token;
    }

    public String getAuthenticationId() {
        return authenticationId;
    }

    public void setAuthenticationId(String authenticationId) {
        this.authenticationId = authenticationId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public OAuth2Authentication getAuthentication() {
        return SerializableObjectConverter.deserialize(authentication);
    }

    public void setAuthentication(OAuth2Authentication authentication) {
        this.authentication = SerializableObjectConverter.serialize(authentication);
    }

    public String getRefreshToken() {
        return refreshTokenKey;
    }

    public void setRefreshToken(String refreshTokenKey) {
        this.refreshTokenKey = refreshTokenKey;
    }

}