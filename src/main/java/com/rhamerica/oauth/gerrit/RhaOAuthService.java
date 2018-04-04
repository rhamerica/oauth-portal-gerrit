package com.rhamerica.oauth.gerrit;

import com.google.gerrit.extensions.auth.oauth.OAuthServiceProvider;
import com.google.gerrit.extensions.auth.oauth.OAuthToken;
import com.google.gerrit.extensions.auth.oauth.OAuthUserInfo;
import com.google.gerrit.extensions.auth.oauth.OAuthVerifier;
import com.google.gerrit.server.config.PluginConfig;
import com.google.gerrit.server.config.PluginConfigFactory;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.rhamerica.oauth.gerrit.rest.SimpleRestClient;
import com.rhamerica.oauth.gerrit.rest.TokenResponse;
import com.rhamerica.oauth.gerrit.rest.UserResponse;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Singleton
class RhaOAuthService implements OAuthServiceProvider {

    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;

    private final String authUri;
    private final String tokenUri; //"http://localhost:8080/token"
    private final String userUri;

    private final Gson gson;

    @Inject
    public RhaOAuthService(PluginConfigFactory configFactory) {
        PluginConfig config = configFactory.getFromGerritConfig(Constants.PLUGIN_NAME);
        clientId = config.getString("clientId");
        clientSecret = config.getString("clientSecret");
        authUri = config.getString("authUri");
        tokenUri = config.getString("tokenUri");
        userUri = config.getString("tokenUri");

        String canonicalUrl = configFactory.getFromGerritConfig("gerrit").getString("canonicalWebUrl");
        redirectUri = canonicalUrl + "oauth";

        gson = new Gson();
    }

    @Override
    public String getAuthorizationUrl() {
        try {
            return authUri //"http://localhost:8080/auth?"
                    + "response_type=code"
                    + "&client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8.name())
                    + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public OAuthToken getAccessToken(OAuthVerifier verifier) {
        String value = verifier.getValue();
        SimpleRestClient.Response response = SimpleRestClient.requestTo(tokenUri).post(outputStream -> {
            try {
                String content = "code=" + URLEncoder.encode(value, StandardCharsets.UTF_8.name())
                        + "&client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8.name())
                        + "&client_secret=" + URLEncoder.encode(clientSecret, StandardCharsets.UTF_8.name());
                outputStream.write(content.getBytes());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        String message = response.getMessage();
        TokenResponse tokenResponse = gson.fromJson(message, TokenResponse.class);
        OAuthToken token = new OAuthToken(tokenResponse.getAccess_token(), clientSecret, message);
        return token;
    }

    @Override
    public OAuthUserInfo getUserInfo(OAuthToken token) {
        String tokenStr = token.getToken();
        SimpleRestClient.Response httpResponse = SimpleRestClient.requestTo(userUri).addHeader("Authorization", String.format("Bearer %s", tokenStr)).get();
        String json = httpResponse.getMessage();
        UserResponse userResponse = gson.fromJson(json, UserResponse.class);

        String email = userResponse.getEmail();
        String name = userResponse.getName();

        OAuthUserInfo info = new OAuthUserInfo(
            email,
            email,
            email,
            name,
            ""
        );
        return info;
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String getName() {
        return "RHA Auth";
    }
//    private static final Logger log = LoggerFactory.getLogger(RhaOAuthService.class);
//    //static final String CONFIG_SUFFIX = "-google-oauth";
//    //private static final String GOOGLE_PROVIDER_PREFIX = "google-oauth:";
//    //private static final String PROTECTED_RESOURCE_URL = "https://www.googleapis.com/userinfo/v2/me";
//    private static final String DISCOVERY_URI = "http://localhost:8080/.well-known/openid-configuration";
//
//    //"https://www.googleapis.com/plus/v1/people/me/openIdConnect";
//    private final OAuthService service;
//    private final String canonicalWebUrl;
//
//    @Inject
//    RhaOAuthService(
//            PluginConfigFactory cfgFactory,
//            @PluginName String pluginName,
//            @CanonicalWebUrl Provider<String> urlProvider
//    ) {
//        //PluginConfig cfg = cfgFactory.getFromGerritConfig(pluginName + CONFIG_SUFFIX);
//        PluginConfig cfg = cfgFactory.getFromGerritConfig(Constants.PLUGIN_NAME);
//        this.canonicalWebUrl = CharMatcher.is('/').trimTrailingFrom(urlProvider.get()) + "/";
//        this.service =
//                new ServiceBuilder()
//                        .provider(Rha2Api.class)
//                        //.apiKey(cfg.getString(InitOAuth.CLIENT_ID))
//                        //.apiSecret(cfg.getString(InitOAuth.CLIENT_SECRET))
//                        .apiKey(cfg.getString("-"))
//                        .apiSecret(cfg.getString("-"))
//                        .callback(canonicalWebUrl + "oauth")
//                        //.scope(SCOPE)
//                        .build();
//        if (log.isDebugEnabled()) {
//            log.debug("OAuth2: canonicalWebUrl={}", canonicalWebUrl);
//            //log.debug("OAuth2: scope={}", SCOPE);
//            //log.debug("OAuth2: domains={}", domains);
//            //log.debug("OAuth2: useEmailAsUsername={}", useEmailAsUsername);
//        }
//    }
//
//    @Override
//    public OAuthUserInfo getUserInfo(OAuthToken token) throws IOException {
//        OAuthRequest request = new OAuthRequest(Verb.GET, DISCOVERY_URI);
//        Token t = new Token(token.getToken(), token.getSecret(), token.getRaw());
//        service.signRequest(t, request);
//        Response response = request.send();
//        if (response.getCode() != HttpServletResponse.SC_OK) {
//            throw new IOException(String.format("Status %s (%s) for request %s", response.getCode(), response.getBody(), request.getUrl()));
//        }
//        JsonElement userJson = OutputFormat.JSON.newGson().fromJson(response.getBody(), JsonElement.class);
//        if (log.isDebugEnabled()) {
//            log.debug("User info response: {}", response.getBody());
//        }
//        if (userJson.isJsonObject()) {
//            JsonObject jsonObject = userJson.getAsJsonObject();
//            JsonElement id = jsonObject.get("id");
//            if (id == null || id.isJsonNull()) {
//                throw new IOException(String.format("Response doesn't contain id field"));
//            }
//            JsonElement email = jsonObject.get("email");
//            JsonElement name = jsonObject.get("name");
//            String login = null;
//
//            /*
//            if (domains.size() > 0) {
//                boolean domainMatched = false;
//                JsonObject jwtToken = retrieveJWTToken(token);
//                String hdClaim = retrieveHostedDomain(jwtToken);
//                for (String domain : domains) {
//                    if (domain.equalsIgnoreCase(hdClaim)) {
//                        domainMatched = true;
//                        break;
//                    }
//                }
//                if (!domainMatched) {
//                    log.error("Error: hosted domain validation failed: {}", Strings.nullToEmpty(hdClaim));
//                    return null;
//                }
//            }
//            */
//
//            if (!email.isJsonNull()) {
//                login = email.getAsString().split("@")[0];
//            }
//
//            return new OAuthUserInfo(
//                    id.getAsString() /*externalId*/,
//                    login /*username*/,
//                    email == null || email.isJsonNull() ? null : email.getAsString() /*email*/,
//                    name == null || name.isJsonNull() ? null : name.getAsString() /*displayName*/,
//                     id.getAsString() /*claimedIdentity*/
//            );
//        }
//
//        throw new IOException(String.format("Invalid JSON '%s': not a JSON Object", userJson));
//    }
//
//    private JsonObject retrieveJWTToken(OAuthToken token) {
//        JsonElement idToken = OutputFormat.JSON.newGson().fromJson(token.getRaw(), JsonElement.class);
//        if (idToken != null && idToken.isJsonObject()) {
//            JsonObject idTokenObj = idToken.getAsJsonObject();
//            JsonElement idTokenElement = idTokenObj.get("id_token");
//            if (idTokenElement != null && !idTokenElement.isJsonNull()) {
//                String payload = decodePayload(idTokenElement.getAsString());
//                if (!Strings.isNullOrEmpty(payload)) {
//                    JsonElement tokenJsonElement =
//                            OutputFormat.JSON.newGson().fromJson(payload, JsonElement.class);
//                    if (tokenJsonElement.isJsonObject()) {
//                        return tokenJsonElement.getAsJsonObject();
//                    }
//                }
//            }
//        }
//        return null;
//    }
//
//    private static String retrieveHostedDomain(JsonObject jwtToken) {
//        JsonElement hdClaim = jwtToken.get("hd");
//        if (hdClaim != null && !hdClaim.isJsonNull()) {
//            String hd = hdClaim.getAsString();
//            log.debug("OAuth2: hd={}", hd);
//            return hd;
//        }
//        log.debug("OAuth2: JWT doesn't contain hd element");
//        return null;
//    }
//
//    /**
//     * Decode payload from JWT according to spec: "header.payload.signature"
//     *
//     * @param idToken Base64 encoded tripple, separated with dot
//     * @return openid_id part of payload, when contained, null otherwise
//     */
//    private static String decodePayload(String idToken) {
//        Preconditions.checkNotNull(idToken);
//        String[] jwtParts = idToken.split("\\.");
//        Preconditions.checkState(jwtParts.length == 3);
//        String payloadStr = jwtParts[1];
//        Preconditions.checkNotNull(payloadStr);
//        return new String(Base64.decodeBase64(payloadStr));
//    }
//
//    @Override
//    public OAuthToken getAccessToken(OAuthVerifier rv) {
//        Verifier vi = new Verifier(rv.getValue());
//        Token to = service.getAccessToken(null, vi);
//        OAuthToken result = new OAuthToken(to.getToken(), to.getSecret(), to.getRawResponse());
//        return result;
//    }
//
//    @Override
//    public String getAuthorizationUrl() {
//        String url = service.getAuthorizationUrl(null);
//        /*
//        try {
//            if (domains.size() == 1) {
//                url += "&hd=" + URLEncoder.encode(domains.get(0), StandardCharsets.UTF_8.name());
//            } else if (domains.size() > 1) {
//                url += "&hd=*";
//            }
//        } catch (UnsupportedEncodingException e) {
//            throw new IllegalArgumentException(e);
//        }
//        if (log.isDebugEnabled()) {
//            log.debug("OAuth2: authorization URL={}", url);
//        }
//        */
//        return url;
//    }
//
//    @Override
//    public String getVersion() {
//        return service.getVersion();
//    }
//
//    @Override
//    public String getName() {
//        return "Google OAuth2";
//    }
}
