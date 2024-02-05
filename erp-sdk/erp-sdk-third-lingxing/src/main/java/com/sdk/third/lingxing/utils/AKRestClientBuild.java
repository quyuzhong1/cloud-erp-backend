package com.sdk.third.lingxing.utils;

import com.sdk.third.lingxing.core.Config;
import com.sdk.third.lingxing.dto.Result;
import com.sdk.third.lingxing.dto.Token;

import java.util.Map;

public class AKRestClientBuild {

    public static AKRestClientBuilder builder() {
        return new AKRestClientBuilder();
    }

    public static class AKRestClientBuilder {

        private Config config;

        private String endpoint;

        public AKRestClientBuilder endpoint(String endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        public AKRestClientBuilder endpoint(Config config) {
            this.config = config;
            return this;
        }

        public Result<Token> getAccessToken(String appId, String appSecret) throws Exception {
            AKRestClient akRestClient = new AKRestClient(endpoint, config);
            return akRestClient.getAccessToken(appId, appSecret);
        }

        public Object refreshToken(String appId, String refreshToken) throws Exception {
            AKRestClient akRestClient = new AKRestClient(endpoint, config);
            return akRestClient.refreshToken(appId, refreshToken);
        }

        public Object sign(Map<String, Object> params) throws Exception {
            AKRestClient akRestClient = new AKRestClient(endpoint, config);
            return akRestClient.sign(params);
        }
    }

}
