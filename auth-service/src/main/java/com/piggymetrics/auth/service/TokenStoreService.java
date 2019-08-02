package com.piggymetrics.auth.service;

import com.mongodb.client.result.UpdateResult;
import com.piggymetrics.auth.domain.Token;
import com.piggymetrics.auth.util.SerializableObjectConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
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

@Service("TokenStoreService")
@Primary
public class TokenStoreService implements TokenStore {

    private AuthenticationKeyGenerator authenticationKeyGenerator = new DefaultAuthenticationKeyGenerator();

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public OAuth2Authentication readAuthentication(OAuth2AccessToken oAuth2AccessToken) {
        return readAuthentication(oAuth2AccessToken.getValue());
    }

    @Override
    public OAuth2Authentication readAuthentication(String s) {
        Query query = new Query();
        query.addCriteria(Criteria.where(Token.TOKEN_ID).is(extractTokenKey(s)));

        Token accessToken = mongoTemplate.findOne(query, Token.class);
        //check getToken() for insure access token validation
        System.out.println("readAuthentication result:"+accessToken.toString());
        return (accessToken != null/*&& accessToken.getAuthentication()!=null*/) ? accessToken.getAuthentication() : null;
    }

    @Override
    public void storeAccessToken(OAuth2AccessToken oAuth2AccessToken, OAuth2Authentication oAuth2Authentication) {
        String refreshToken = null;
        if (oAuth2AccessToken.getRefreshToken() != null) {
            refreshToken = oAuth2AccessToken.getRefreshToken().getValue();
        }

//        if (readAccessToken(oAuth2AccessToken.getValue()) != null) {
//            this.removeAccessToken(oAuth2AccessToken);
//        }
        Query query = Query.query(Criteria.where(Token.TOKEN_ID).is(extractTokenKey(oAuth2AccessToken.getValue())));
        Update update = Update.update("tokenId", extractTokenKey(oAuth2AccessToken.getValue()))
                .set("token", oAuth2AccessToken)
                .set("authenticationId", (authenticationKeyGenerator.extractKey(oAuth2Authentication)))
                .set("username", (oAuth2Authentication.isClientOnly() ? null : oAuth2Authentication.getName()))
                .set("clientID", oAuth2Authentication.getOAuth2Request().getClientId())
                .set("authentication", SerializableObjectConverter.serialize(oAuth2Authentication))
                .set("refreshTokenKey", extractTokenKey(refreshToken));
        UpdateResult updateResult = mongoTemplate.upsert(query, update, Token.class);
        System.out.println("storeAccessToken result:" + updateResult.wasAcknowledged());


/*        MongoAccessToken mongoAccessToken = new MongoAccessToken();
        mongoAccessToken.setTokenId(extractTokenKey(oAuth2AccessToken.getValue()));
        mongoAccessToken.setToken(oAuth2AccessToken);
        mongoAccessToken.setAuthenticationId(authenticationKeyGenerator.extractKey(oAuth2Authentication));
        mongoAccessToken.setUsername(oAuth2Authentication.isClientOnly() ? null : oAuth2Authentication.getName());
        mongoAccessToken.setClientId(oAuth2Authentication.getOAuth2Request().getClientId());
        mongoAccessToken.setAuthentication(oAuth2Authentication);
        mongoAccessToken.setRefreshToken(extractTokenKey(refreshToken));
        mongoTemplate.save(mongoAccessToken);*/
    }

    @Override
    public OAuth2AccessToken readAccessToken(String s) {
        Query query = new Query();
        query.addCriteria(Criteria.where(Token.TOKEN_ID).is(extractTokenKey(s)));

        Token accessToken = mongoTemplate.findOne(query, Token.class);
        System.out.println("readAccessToken result: "+accessToken);
        return (accessToken != null /*&& accessToken.getToken()!= null*/)? accessToken.getToken() : null;
        //check access token validation
    }

    @Override
    public void removeAccessToken(OAuth2AccessToken oAuth2AccessToken) {
        // update accesstoken relative field, delete then insert seem faster?
        Query query = Query.query(Criteria.where(Token.TOKEN_ID).is(extractTokenKey(oAuth2AccessToken.getValue())));
        Update update = Update.update("tokenId", "")
                .set("token", null)
                .set("authenticationId", "")
                .set("username", "")
                .set("clientID", "")
                .set("authentication", "")
                .unset("refreshTokenKey");
        UpdateResult updateResult=mongoTemplate.updateFirst(query, update, Token.class);
        System.out.println("removeAccessToken result:"+updateResult.wasAcknowledged());

//        Query query = new Query();
//        query.addCriteria(Criteria.where(Token.TOKEN_ID).is(extractTokenKey(oAuth2AccessToken.getValue())));
//        mongoTemplate.remove(query, Token.class);
    }

    @Override
    public void storeRefreshToken(OAuth2RefreshToken oAuth2RefreshToken, OAuth2Authentication oAuth2Authentication) {
        Token token = new Token();
        token.setRefreshId(extractTokenKey(oAuth2RefreshToken.getValue()));
        token.setRefresh(oAuth2RefreshToken);
        token.setRefreshAuthentication(oAuth2Authentication);
        mongoTemplate.save(token);
    }

    @Override
    public OAuth2RefreshToken readRefreshToken(String s) {
        Query query = new Query();
        query.addCriteria(Criteria.where(Token.REFRESH_ID).is(extractTokenKey(s)));

        //refreshId
        Token refreshToken = mongoTemplate.findOne(query, Token.class);
        System.out.println("readRefreshToken result:"+refreshToken);
        return refreshToken != null ? refreshToken.getRefresh() : null;

    }

    @Override
    public OAuth2Authentication readAuthenticationForRefreshToken(OAuth2RefreshToken oAuth2RefreshToken) {
        Query query = new Query();
        query.addCriteria(Criteria.where(Token.REFRESH_ID).is(extractTokenKey(oAuth2RefreshToken.getValue())));

        //refreshId
        Token refreshToken = mongoTemplate.findOne(query, Token.class);
        System.out.println("readAuthForRefresh result:"+refreshToken);
        return refreshToken != null ? refreshToken.getRefreshAuthentication() : null;

    }

    @Override
    public void removeRefreshToken(OAuth2RefreshToken oAuth2RefreshToken) {
//        Query query = new Query();
//        query.addCriteria(Criteria.where(MongoRefreshToken.TOKEN_ID).is(extractTokenKey(oAuth2RefreshToken.getValue())));
//        mongoTemplate.remove(query, MongoRefreshToken.class);

        Query query = Query.query(Criteria.where(Token.REFRESH_ID).is(extractTokenKey(oAuth2RefreshToken.getValue())));
        Update update = Update.update("refresh", null)
                .set("refreshId", "")
                .set("refreshAuthentication", "");
        UpdateResult updateResult= mongoTemplate.updateFirst(query, update, Token.class);
        System.out.println("removeRefreshToken result:"+updateResult.wasAcknowledged());
    }

    @Override
    public void removeAccessTokenUsingRefreshToken(OAuth2RefreshToken oAuth2RefreshToken) {
        Query query = new Query();
        query.addCriteria(Criteria.where(Token.REFRESH_TOKEN).is(extractTokenKey(oAuth2RefreshToken.getValue())));

        Token token = mongoTemplate.findOne(query, Token.class);
        System.out.println("removeAccessToken result:"+token);
        this.removeAccessToken(token.getToken());
    }

    @Override
    public OAuth2AccessToken getAccessToken(OAuth2Authentication oAuth2Authentication) {
        OAuth2AccessToken accessToken = null;
        String authenticationId = authenticationKeyGenerator.extractKey(oAuth2Authentication);

        Query query = new Query();
        query.addCriteria(Criteria.where(Token.AUTHENTICATION_ID).is(authenticationId));

        Token token = mongoTemplate.findOne(query, Token.class);
        if (token != null) {
            accessToken = token.getToken();
            if (accessToken != null && !authenticationId.equals(this.authenticationKeyGenerator.extractKey(this.readAuthentication(accessToken)))) {
                this.removeAccessToken(accessToken);
                this.storeAccessToken(accessToken, oAuth2Authentication);
            }
        }
        return accessToken;
    }

    @Override
    public Collection<OAuth2AccessToken> findTokensByClientIdAndUserName(String id, String name) {
        return findTokensByCriteria(
                Criteria.where(Token.CLIENT_ID).is(id)
                        .and(Token.USER_NAME).is(name));

    }

    @Override
    public Collection<OAuth2AccessToken> findTokensByClientId(String id) {
        return findTokensByCriteria(Criteria.where(Token.CLIENT_ID).is(id));

    }

    private Collection<OAuth2AccessToken> findTokensByCriteria(Criteria criteria) {
        Collection<OAuth2AccessToken> tokens = new ArrayList<>();
        Query query = new Query();
        query.addCriteria(criteria);
        List<Token> accessTokens = mongoTemplate.find(query, Token.class);
        for (Token accessToken : accessTokens) {
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
                byte[] e = digest.digest(value.getBytes(StandardCharsets.UTF_8.name()));
                return String.format("%032x", new BigInteger(1, e));
            } catch (/*UnsupportedEncoding*/Exception var4) {
                throw new IllegalStateException("UTF-8 encoding not available.  Fatal (should be in the JDK).");
            }
        }
    }

}