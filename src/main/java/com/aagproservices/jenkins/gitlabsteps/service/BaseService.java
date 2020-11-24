package com.aagproservices.jenkins.gitlabsteps.service;

import com.aagproservices.jenkins.gitlabsteps.util.HttpUtil;
import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
import hudson.security.ACL;
import okhttp3.*;
import org.apache.commons.lang.StringUtils;
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

    BaseService() {
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

    protected Request buildRequest(final String gitlabUrl, final String token,
                                   final String project, final String repoSlug,
                                   final String requestResource, final String httpMethod,
                                   final RequestBody requestBody, final Map<String, String> queryParams) {
        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.addHeader(AUTHORIZATION_HEADER, "Bearer " + token);
        requestBuilder.url(buildUrl(
                        gitlabUrl
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

    protected Object executeRequest(final Request request, int timeout, boolean debugMode, boolean trustAllCertificates) throws BadRequestException {
        try {
            if (debugMode) {
                LOGGER.info(TAG + "Request: " + request.method() + " " + request.url().toString());
                if (request.body() != null && request.body().contentLength() > 0) {
                    LOGGER.info(TAG + request.body().toString());
                }
            }

            OkHttpClient client = initClient(timeout, trustAllCertificates);
            Response response = client.newCall(request).execute();
            ResponseBody respBody = response.body();
            String respString = respBody == null ? "" : respBody.string();

            if (debugMode) {
                LOGGER.info(TAG + "Response: " + response.code() + " " + response.message());
                LOGGER.info(TAG + respString);
            }

            JSONObject object;
            JSONArray array;

            if ((object = getJSONObject(respString)) != null) {
                if(response.code() >= 400) {
                    throw new BadRequestException("Error response from server (" + response.code() + "): " + object.toString());
                }

                checkForError(object, "Error response from server");
                return object;
            } else if((array = getJSONOArray(respString)) != null) {
                if(response.code() >= 400) {
                    throw new BadRequestException("Error response from server (" + response.code() + "): " + array.toString());
                }
                return array;
            } else {
                throw new BadRequestException("Unable to parse response from server: '" + respString + "'");
            }
        } catch (IOException e) {
            LOGGER.error("Error while executing request " + request.toString(), e);
            throw new IllegalArgumentException(e);
        }
    }

    private OkHttpClient initClient(int timeout, boolean trustAllCertificates) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(timeout, TimeUnit.SECONDS)
                .readTimeout(timeout, TimeUnit.SECONDS)
                .writeTimeout(timeout, TimeUnit.SECONDS);

        if (trustAllCertificates) {
            builder = installTrustManager(builder, HttpUtil.buildAllTrustingManager())
                    .hostnameVerifier((s, sslSession) -> true);
        }

        return builder.build();
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
}
