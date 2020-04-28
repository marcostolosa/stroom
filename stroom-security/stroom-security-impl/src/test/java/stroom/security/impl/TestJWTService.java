package stroom.security.impl;

import org.jose4j.base64url.Base64;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwk.RsaJwkGenerator;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.lang.JoseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stroom.util.authentication.DefaultOpenIdCredentials;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class TestJWTService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestJWTService.class);

    @Mock
    private OpenIdPublicKeysSupplier openIdPublicKeysSupplier;
    @Mock
    private ResolvedOpenIdConfig resolvedOpenIdConfig;

    private DefaultOpenIdCredentials defaultOpenIdCredentials = new DefaultOpenIdCredentials();



    @Test
    void verifyToken() throws InvalidJwtException, JoseException {
        // Verify the hard coded default token

        final JWTService jwtService = new JWTService(resolvedOpenIdConfig, openIdPublicKeysSupplier);

        final String apiKey = defaultOpenIdCredentials.getApiKey();

        Mockito.when(openIdPublicKeysSupplier.get())
                .thenReturn(getPublicKeys());

        Mockito.when(resolvedOpenIdConfig.getClientId())
                .thenReturn(defaultOpenIdCredentials.getOauth2ClientId());

        jwtService.verifyToken(apiKey);

    }

    @Test
    void testPublicKey() throws JoseException {
        LOGGER.info("JsonWebToken (API key): \n\n{}\n", defaultOpenIdCredentials.getApiKey());

        final PublicJsonWebKey publicJsonWebKey = RsaJsonWebKey.Factory
                .newPublicJwk(defaultOpenIdCredentials.getPublicKeyJson());

        LOGGER.info("Public key: {}", publicJsonWebKey.getPublicKey().toString());

        LOGGER.info("Public key Base64: \n\n{}\n", new String(Base64.encode(publicJsonWebKey.getPublicKey().getEncoded())));

        LOGGER.info("private key: {}", publicJsonWebKey.getPrivateKey().toString());

        LOGGER.info("Private key Base64: \n\n{}\n", new String(Base64.encode(publicJsonWebKey.getPrivateKey().getEncoded())));
    }

    /**
     * Copied from https://bitbucket.org/b_c/jose4j/wiki/JWT Examples
     *
     * Not really a test
     */
    @Test
    void testJose4jExample() throws InvalidJwtException, JoseException {

        //
        // JSON Web Token is a compact URL-safe means of representing claims/attributes to be transferred between two parties.
        // This example demonstrates producing and consuming a signed JWT
        //

        // Generate an RSA key pair, which will be used for signing and verification of the JWT, wrapped in a JWK
        RsaJsonWebKey rsaJsonWebKey = RsaJwkGenerator.generateJwk(2048);

        // Give the JWK a Key ID (kid), which is just the polite thing to do
        rsaJsonWebKey.setKeyId("k1");

        // Create the Claims, which will be the content of the JWT
        JwtClaims claims = new JwtClaims();
        claims.setIssuer("Issuer");  // who creates the token and signs it
        claims.setAudience("Audience"); // to whom the token is intended to be sent
        claims.setExpirationTimeMinutesInTheFuture(10); // time when the token will expire (10 minutes from now)
        claims.setGeneratedJwtId(); // a unique identifier for the token
        claims.setIssuedAtToNow();  // when the token was issued/created (now)
        claims.setNotBeforeMinutesInThePast(2); // time before which the token is not yet valid (2 minutes ago)
        claims.setSubject("subject"); // the subject/principal is whom the token is about
        claims.setClaim("email","mail@example.com"); // additional claims/attributes about the subject can be added
        List<String> groups = Arrays.asList("group-one", "other-group", "group-three");
        claims.setStringListClaim("groups", groups); // multi-valued claims work too and will end up as a JSON array

        // A JWT is a JWS and/or a JWE with JSON claims as the payload.
        // In this example it is a JWS so we create a JsonWebSignature object.
        JsonWebSignature jws = new JsonWebSignature();

        // The payload of the JWS is JSON content of the JWT Claims
        jws.setPayload(claims.toJson());

        // The JWT is signed using the private key
        jws.setKey(rsaJsonWebKey.getPrivateKey());

        // Set the Key ID (kid) header because it's just the polite thing to do.
        // We only have one key in this example but a using a Key ID helps
        // facilitate a smooth key rollover process
        jws.setKeyIdHeaderValue(rsaJsonWebKey.getKeyId());

        // Set the signature algorithm on the JWT/JWS that will integrity protect the claims
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256);

        // Sign the JWS and produce the compact serialization or the complete JWT/JWS
        // representation, which is a string consisting of three dot ('.') separated
        // base64url-encoded parts in the form Header.Payload.Signature
        // If you wanted to encrypt it, you can simply set this jwt as the payload
        // of a JsonWebEncryption object and set the cty (Content Type) header to "jwt".
        String jwt = jws.getCompactSerialization();


        // Now you can do something with the JWT. Like send it to some other party
        // over the clouds and through the interwebs.
        System.out.println("JWT: " + jwt);

        // Use JwtConsumerBuilder to construct an appropriate JwtConsumer, which will
        // be used to validate and process the JWT.
        // The specific validation requirements for a JWT are context dependent, however,
        // it typically advisable to require a (reasonable) expiration time, a trusted issuer, and
        // and audience that identifies your system as the intended recipient.
        // If the JWT is encrypted too, you need only provide a decryption key or
        // decryption key resolver to the builder.
        JwtConsumer jwtConsumer = new JwtConsumerBuilder()
                .setRequireExpirationTime() // the JWT must have an expiration time
                .setAllowedClockSkewInSeconds(30) // allow some leeway in validating time based claims to account for clock skew
                .setRequireSubject() // the JWT must have a subject claim
                .setExpectedIssuer("Issuer") // whom the JWT needs to have been issued by
                .setExpectedAudience("Audience") // to whom the JWT is intended for
                .setVerificationKey(rsaJsonWebKey.getKey()) // verify the signature with the public key
                .setJwsAlgorithmConstraints( // only allow the expected signature algorithm(s) in the given context
                        new AlgorithmConstraints(
                                AlgorithmConstraints.ConstraintType.WHITELIST, AlgorithmIdentifiers.RSA_USING_SHA256)) // which is only RS256 here
                .build(); // create the JwtConsumer instance

        LOGGER.info("Public key: {}", rsaJsonWebKey.getPublicKey().toString());

        LOGGER.info("Base64: \n{}\n", new String(Base64.encode(rsaJsonWebKey.getPublicKey().getEncoded())));

        LOGGER.info("private key: {}", rsaJsonWebKey.getPrivateKey().toString());

        LOGGER.info("Base64: \n{}\n", new String(Base64.encode(rsaJsonWebKey.getPrivateKey().getEncoded())));


        //  Validate the JWT and process it to the Claims
        JwtClaims jwtClaims = jwtConsumer.processToClaims(jwt);
        System.out.println("JWT validation succeeded! " + jwtClaims);

    }

    private JsonWebKeySet getPublicKeys() throws JoseException {

        final PublicJsonWebKey publicJsonWebKey = RsaJsonWebKey.Factory
                .newPublicJwk(defaultOpenIdCredentials.getPublicKeyJson());

        final List<PublicJsonWebKey> publicJsonWebKeys = Collections.singletonList(publicJsonWebKey);

//        final List<Map<String, Object>> maps = list.stream()
//                .map(jwk -> jwk.toParams(JsonWebKey.OutputControlLevel.PUBLIC_ONLY))
//                .collect(Collectors.toList());
//
//        Map<String, List<Map<String, Object>>> keys = new HashMap<>();
//        keys.put("keys", maps);

//        String publicKeysJson = JsonUtil.toJson(keys);
//
//        LOGGER.info("publicKeysJson \n{}", publicJsonWebKey);

        JsonWebKeySet jsonWebKeySet = new JsonWebKeySet(publicJsonWebKeys);

        return jsonWebKeySet;
    }
}