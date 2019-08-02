package com.piggymetrics.auth.service.security;

import com.mongodb.client.result.UpdateResult;
import com.piggymetrics.auth.domain.MongoAccessToken;
import com.piggymetrics.auth.domain.MongoRefreshToken;
import com.piggymetrics.auth.util.SerializableObjectConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.AuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.DefaultAuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service("MongoTokenStoreService")
//@Primary
public class MongoTokenStoreService implements TokenStore {

    private AuthenticationKeyGenerator authenticationKeyGenerator = new DefaultAuthenticationKeyGenerator();

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public OAuth2Authentication readAuthentication(OAuth2AccessToken accessToken) {
        return readAuthentication(accessToken.getValue());
    }

    @Override
    public OAuth2Authentication readAuthentication(String token) {
        Query query = new Query();
        query.addCriteria(Criteria.where(MongoAccessToken.TOKEN_ID).is(extractTokenKey(token)));

        MongoAccessToken mongoAccessToken = mongoTemplate.findOne(query, MongoAccessToken.class);
        return mongoAccessToken != null ? mongoAccessToken.getAuthentication() : null;
    }

    @Override
    public void storeAccessToken(OAuth2AccessToken oAuth2AccessToken, OAuth2Authentication oAuth2Authentication) {
        String refreshToken = null;
        if (oAuth2AccessToken.getRefreshToken() != null) {
            refreshToken = oAuth2AccessToken.getRefreshToken().getValue();
        }

/*         if (readAccessToken(oAuth2AccessToken.getValue()) != null) {
            this.removeAccessToken(oAuth2AccessToken);
        }
        MongoAccessToken mongoAccessToken = new MongoAccessToken();
        mongoAccessToken.setTokenId(extractTokenKey(accessToken.getValue()));
        mongoAccessToken.setToken(accessToken);
        mongoAccessToken.setAuthenticationId(authenticationKeyGenerator.extractKey(authentication));
        mongoAccessToken.setUsername(authentication.isClientOnly() ? null : authentication.getName());
        mongoAccessToken.setClientId(authentication.getOAuth2Request().getClientId());
        mongoAccessToken.setAuthentication(authentication);
        mongoAccessToken.setRefreshToken(extractTokenKey(refreshToken));
        mongoTemplate.save(mongoAccessToken);*/

        Query query = Query.query(Criteria.where(MongoAccessToken.TOKEN_ID).is(extractTokenKey(oAuth2AccessToken.getValue())));
        Update update = Update.update("tokenId", extractTokenKey(oAuth2AccessToken.getValue()))
                .set("token", oAuth2AccessToken)
                .set("authenticationId", (authenticationKeyGenerator.extractKey(oAuth2Authentication)))
                .set("username", (oAuth2Authentication.isClientOnly() ? null : oAuth2Authentication.getName()))
                .set("clientID", oAuth2Authentication.getOAuth2Request().getClientId())
                .set("authentication", SerializableObjectConverter.serialize(oAuth2Authentication))
                .set("refreshTokenKey", extractTokenKey(refreshToken));
        UpdateResult updateResult = mongoTemplate.upsert(query, update, MongoAccessToken.class);
        System.out.println("storeAccessToken result:" + updateResult.wasAcknowledged());

    }

    @Override
    public OAuth2AccessToken readAccessToken(String tokenValue) {
        Query query = new Query();
        query.addCriteria(Criteria.where(MongoAccessToken.TOKEN_ID).is(extractTokenKey(tokenValue)));

        MongoAccessToken mongoAccessToken = mongoTemplate.findOne(query, MongoAccessToken.class);
        return mongoAccessToken != null ? mongoAccessToken.getToken() : null;
    }

    @Override
    public void removeAccessToken(OAuth2AccessToken oAuth2AccessToken) {
/*        Query query = new Query();
        query.addCriteria(Criteria.where(MongoAccessToken.TOKEN_ID).is(extractTokenKey(oAuth2AccessToken.getValue())));
        mongoTemplate.remove(query, MongoAccessToken.class);
*/
        Query query = Query.query(Criteria.where(MongoAccessToken.TOKEN_ID).is(extractTokenKey(oAuth2AccessToken.getValue())));
        Update update = Update.update("tokenId", "")
                .set("token", null)
                .set("authenticationId", "")
                .set("username", "")
                .set("clientID", "")
                .set("authentication", "")
                .unset("refreshToken");
        UpdateResult updateResult=mongoTemplate.updateFirst(query, update, MongoAccessToken.class);
        System.out.println("removeAccessToken result:"+updateResult.wasAcknowledged());

    }

    @Override
    public void storeRefreshToken(OAuth2RefreshToken refreshToken, OAuth2Authentication authentication) {
        MongoRefreshToken token = new MongoRefreshToken();
        token.setTokenId(extractTokenKey(refreshToken.getValue()));
        token.setToken(refreshToken);
        token.setAuthentication(authentication);
        mongoTemplate.save(token);
    }

    @Override
    public OAuth2RefreshToken readRefreshToken(String tokenValue) {
        Query query = new Query();
        query.addCriteria(Criteria.where(MongoRefreshToken.TOKEN_ID).is(extractTokenKey(tokenValue)));

        MongoRefreshToken mongoRefreshToken = mongoTemplate.findOne(query, MongoRefreshToken.class);
        return mongoRefreshToken != null ? mongoRefreshToken.getToken() : null;
    }

    @Override
    public OAuth2Authentication readAuthenticationForRefreshToken(OAuth2RefreshToken refreshToken) {
        Query query = new Query();
        query.addCriteria(Criteria.where(MongoRefreshToken.TOKEN_ID).is(extractTokenKey(refreshToken.getValue())));

        MongoRefreshToken mongoRefreshToken = mongoTemplate.findOne(query, MongoRefreshToken.class);
        return mongoRefreshToken != null ? mongoRefreshToken.getAuthentication() : null;
    }

    @Override
    public void removeRefreshToken(OAuth2RefreshToken refreshToken) {
        Query query = new Query();
        query.addCriteria(Criteria.where(MongoRefreshToken.TOKEN_ID).is(extractTokenKey(refreshToken.getValue())));
        mongoTemplate.remove(query, MongoRefreshToken.class);
    }

    @Override
    public void removeAccessTokenUsingRefreshToken(OAuth2RefreshToken refreshToken) {
        Query query = new Query();
        query.addCriteria(Criteria.where(MongoAccessToken.REFRESH_TOKEN).is(extractTokenKey(refreshToken.getValue())));
//        mongoTemplate.remove(query, MongoAccessToken.class);
        MongoAccessToken accessToken=mongoTemplate.findOne(query,MongoAccessToken.class);
        this.removeAccessToken(accessToken.getToken());
    }

    @Override
    public OAuth2AccessToken getAccessToken(OAuth2Authentication authentication) {
        OAuth2AccessToken accessToken = null;
        String authenticationId = authenticationKeyGenerator.extractKey(authentication);

        Query query = new Query();
        query.addCriteria(Criteria.where(MongoAccessToken.AUTHENTICATION_ID).is(authenticationId));

        MongoAccessToken mongoAccessToken = mongoTemplate.findOne(query, MongoAccessToken.class);
        if (mongoAccessToken != null) {
            accessToken = mongoAccessToken.getToken();
            if (accessToken != null && !authenticationId.equals(authenticationKeyGenerator.extractKey(this.readAuthentication(accessToken)))) {
//                this.removeAccessToken(accessToken);
                this.storeAccessToken(accessToken, authentication);
            }
        }
        return accessToken;
    }

    @Override
    public Collection<OAuth2AccessToken> findTokensByClientIdAndUserName(String clientId, String username) {
        return findTokensByCriteria(
                Criteria.where(MongoAccessToken.CLIENT_ID).is(clientId)
                        .and(MongoAccessToken.USER_NAME).is(username));
    }

    @Override
    public Collection<OAuth2AccessToken> findTokensByClientId(String clientId) {
        return findTokensByCriteria(Criteria.where(MongoAccessToken.CLIENT_ID).is(clientId));
    }

    private Collection<OAuth2AccessToken> findTokensByCriteria(Criteria criteria) {
        Collection<OAuth2AccessToken> tokens = new ArrayList<>();
        Query query = new Query();
        query.addCriteria(criteria);
        List<MongoAccessToken> accessTokens = mongoTemplate.find(query, MongoAccessToken.class);
        for (MongoAccessToken accessToken : accessTokens) {
            tokens.add(accessToken.getToken());
        }
        return tokens;
    }

    private String extractTokenKey(String value) {
        if (value == null) {
            return null;
        } else {
            MessageDigest digest;
            try {
                digest = MessageDigest.getInstance("MD5");
//            } catch (NoSuchAlgorithmException var5) {
//                throw new IllegalStateException("MD5 algorithm not available.  Fatal (should be in the JDK).");
//            }

//            try {
                byte[] e = digest.digest(value.getBytes(StandardCharsets.UTF_8.name()));
                return String.format("%032x", new BigInteger(1, e));
            } catch (/*UnsupportedEncodingException*/ Exception var4) {
                throw new IllegalStateException("UTF-8 encoding not available.  Fatal (should be in the JDK).");
            }
        }
    }
}