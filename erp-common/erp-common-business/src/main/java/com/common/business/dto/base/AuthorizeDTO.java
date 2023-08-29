package com.common.business.dto.base;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author Lambda
 * @Classname AuthorizeDTO
 * @Description TODO
 * @Date 2023-08-29 9:23
 * @Created by yl
 */
public class AuthorizeDTO  implements Serializable {


    @Data
    @NoArgsConstructor
    public static class FindShopAuthorizeDTO{

        /**
         * code
         */
        private String code;

        /**
         * clientId
         */
        private String clientId;

        /**
         * clientSecret
         */
        private String clientSecret;

        /**
         * accessTokenUrl
         */
        private String accessTokenUrl;

        /**
         * hmc
         */
        private String hmac;

        /**
         * host
         */
        private String host;

        /**
         * shop
         */
        private String shop;

        /**
         * shop
         */
        private String timestamp;

    }
}
