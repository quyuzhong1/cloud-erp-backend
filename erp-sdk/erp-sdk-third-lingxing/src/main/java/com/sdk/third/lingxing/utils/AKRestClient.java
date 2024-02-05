package com.sdk.third.lingxing.utils;



import com.sdk.third.lingxing.core.Config;
import com.sdk.third.lingxing.core.HttpMethod;
import com.sdk.third.lingxing.core.HttpRequest;
import com.sdk.third.lingxing.core.HttpResponse;
import com.sdk.third.lingxing.dto.Result;
import com.sdk.third.lingxing.dto.Token;

import java.util.Map;

public class AKRestClient {

    private final Config config;

    private final String endpoint;

    public AKRestClient(String endpoint, Config config) {
        this.config = config;
        this.endpoint = endpoint;
    }

    public Result<Token> getAccessToken(String appId, String appSecret) throws Exception {
        HttpRequest<Result> build = HttpRequest.builder(Result.class)
                .config(this.config)
                .method(HttpMethod.POST)
                .endpoint(this.endpoint)
                .path("api/auth-server/oauth/access-token")
                .queryParam("appId", appId)
                .queryParam("appSecret", appSecret)
                .build();
        try (HttpResponse execute = HttpExecutor.create().execute(build)) {
            return execute.readEntity(Result.class);
        }
    }

    public Object refreshToken(String appId, String refreshToken) throws Exception {
        HttpRequest<Object> build = HttpRequest.builder(Object.class)
                .config(this.config)
                .method(HttpMethod.POST)
                .endpoint(this.endpoint)
                .path("api/auth-server/oauth/refresh")
                .queryParam("appId", appId)
                .queryParam("refreshToken", refreshToken)
                .build();
        try (HttpResponse execute = HttpExecutor.create().execute(build)) {
            return execute.readEntity(Object.class);
        }
    }

    public Object sign(Map<String, Object> params) throws Exception {
        HttpRequest<Object> build = HttpRequest.builder(Object.class)
                .config(this.config)
                .method(HttpMethod.POST)
                .endpoint(this.endpoint)
                .path("api/auth-server/oauth/api/authorize")
                .queryParams(params)
                .build();
        try (HttpResponse execute = HttpExecutor.create().execute(build)) {
            return execute.readEntity(Object.class);
        }
    }
}
