package com.sdk.oms.dht.config;

import com.common.business.constant.BusinessCommonConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DhtConfig {

    @Bean
    public String appId() {
        return BusinessCommonConstants.hasProfile("prod") ?
                "FSAID_1320998" : "FSAID_1320998";
    }

    @Bean
    public String appSecret() {
        return BusinessCommonConstants.hasProfile("prod") ?
                "803c8d535711404281ddf5750245a779" : "803c8d535711404281ddf5750245a779";
    }

    @Bean
    public String permanentCode() {
        return BusinessCommonConstants.hasProfile("prod") ?
                "ECDE744727AA7F1310607DC5DEAF8364" : "ECDE744727AA7F1310607DC5DEAF8364";
    }

    @Bean
    public String url() {
        return BusinessCommonConstants.hasProfile("prod") ?
                "https://open.fxiaoke.com" : "https://open.fxiaoke.com";
    }
}
