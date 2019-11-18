package com.aagproservices.jenkins.gitlabsteps.service;

import com.aagproservices.jenkins.gitlabsteps.GitlabServer;
import com.aagproservices.jenkins.gitlabsteps.util.HttpUtil;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.ws.rs.BadRequestException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public abstract class BaseService {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseService.class);
    private static final String TAG = "[GITLAB_STEPS] ";

    private static final String SSL_INSTANCE_TYPE = "SSL";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    protected static final String BASE_RESOURCE = "/api/v4";

    protected GitlabServer gitlabSite;
    private OkHttpClient client;
    private Map<String, String> defaultRequestHeaders;
    private Map<String, String> customRequestHeaders;

    BaseService(final GitlabServer gitlabSite) {
        this.gitlabSite = gitlabSite;
        this.customRequestHeaders = new HashMap<>();
        initClient();
        initHeaders();
    }

    protected String guessMediaType(final File file) {
        return guessMediaType(file.getAbsolutePath());
    }

    protected String guessMediaType(final String fileName) {
        try {
            String contentType =  Files.probeContentType(Paths.get(fileName));
            if(contentType == null) {
                contentType = "application/octet-stream";
            }
            return contentType;
        } catch (IOException ex) {
            return "application/octet-stream";
        }
    }

    protected Request buildRequest(final String project, final String repoSlug,
                                   final String requestResource, final String httpMethod,
                                   final RequestBody requestBody, final Map<String, String> queryParams) {
        Request.Builder requestBuilder = new Request.Builder();
        registerAllHeaders(requestBuilder);
        //clear the custom headers for the next upcoming request
        this.customRequestHeaders.clear();
        requestBuilder.url(buildUrl(
                gitlabSite.getUrl()
                        + BASE_RESOURCE
                        + "/projects/"
                        + project
                        + "%2F" + repoSlug
                        + "/" + requestResource,
                queryParams));
        requestBuilder.method(httpMethod, requestBody);
        return requestBuilder.build();
    }

    private void checkForError(JSONObject result, String errorMessage) throws BadRequestException {
        try {
            String errorMsg = null;

            if (result.has("error")) {
                Object val = result.get("error");
                errorMsg = val == null ? null : val.toString();
            }

            if (errorMsg != null) {
                LOGGER.error("Error response from server: " + errorMsg);
                throw new BadRequestException(errorMessage + " - " + errorMsg);
            }
        } catch (Exception e) {
            throw new BadRequestException(errorMessage + " - " + e.getMessage(), e);
        }
    }

    private JSONObject getJSONObject(String json) {
        try {
            return new JSONObject(json);
        } catch (JSONException ex) {
            return null;
        }
    }

    private JSONArray getJSONOArray(String json) {
        try {
            return new JSONArray(json);
        } catch (JSONException ex) {
            return null;
        }
    }

    protected Object executeRequest(final Request request) throws BadRequestException {
        try {
            if (gitlabSite.isDebugMode()) {
                LOGGER.info(TAG + "Request: " + request.method() + " " + request.url().toString());
                if (request.body() != null && request.body().contentLength() > 0) {
                    LOGGER.info(TAG + request.body().toString());
                }
            }

            OkHttpClient client = getClient();
            Response response = client.newCall(request).execute();
            ResponseBody respBody = response.body();
            String respString = respBody == null ? "" : respBody.string();

            if (gitlabSite.isDebugMode()) {
                LOGGER.info(TAG + "Response: " + response.code() + " " + response.message());
                LOGGER.info(TAG + respString);
            }

            JSONObject object;
            JSONArray array;

            if ((object = getJSONObject(respString)) != null) {
                checkForError(object, "Error response from server");
                return object;
            } else if((array = getJSONOArray(respString)) != null) {
                return array;
            } else {
                throw new BadRequestException("Unable to parse response from server: '" + respString + "'");
            }
        } catch (IOException e) {
            LOGGER.error("Error while executing request " + request.toString(), e);
            throw new IllegalArgumentException(e);
        }
    }

    private void initClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(this.gitlabSite.getTimeout(), TimeUnit.SECONDS)
                .readTimeout(this.gitlabSite.getTimeout(), TimeUnit.SECONDS)
                .writeTimeout(this.gitlabSite.getTimeout(), TimeUnit.SECONDS)
                .connectionPool(new ConnectionPool(gitlabSite.getPoolSize(), 15, TimeUnit.SECONDS));

        if (gitlabSite.getTrustAllCertificates()) {
            builder = installTrustManager(builder, HttpUtil.buildAllTrustingManager())
                    .hostnameVerifier((s, sslSession) -> true);
        }

        this.client = builder.build();
    }

    private OkHttpClient.Builder installTrustManager(OkHttpClient.Builder builder, final TrustManager[] allTrustingManager) {
        try {
            SSLContext sslContext = SSLContext.getInstance(SSL_INSTANCE_TYPE);
            sslContext.init(null, allTrustingManager, new SecureRandom());
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            return builder.socketFactory(sslSocketFactory);
        } catch (NoSuchAlgorithmException e) {
            //We should never land here
            return builder;
        } catch (KeyManagementException e) {
            LOGGER.error("Something went wrong with the key-management", e);
            return builder;
        }
    }

    private void initHeaders() {
        this.defaultRequestHeaders = new HashMap<>();
        this.defaultRequestHeaders.put(AUTHORIZATION_HEADER, "Bearer " + gitlabSite.getAccessToken());
    }

    private void registerAllHeaders(final Request.Builder builder) {
        defaultRequestHeaders.forEach(builder::addHeader);
        customRequestHeaders.forEach(builder::addHeader);
    }

    private void addQueryParams(final HttpUrl.Builder urlBuilder, final Map<String, String> queryParams) {
        if (queryParams != null && !queryParams.isEmpty()) {
            queryParams.forEach(urlBuilder::addQueryParameter);
        }
    }

    private HttpUrl buildUrl(final String url, final Map<String, String> queryParams) {
        HttpUrl urlObject = HttpUrl.parse(url);
        HttpUrl.Builder urlBuilder = urlObject.newBuilder();
        addQueryParams(urlBuilder, queryParams);
        return urlBuilder.build();
    }

    private void registerRequestHeader(final String name, final String value) {
        this.customRequestHeaders.put(name, value);
    }

    public OkHttpClient getClient() {
        return client;
    }
}
