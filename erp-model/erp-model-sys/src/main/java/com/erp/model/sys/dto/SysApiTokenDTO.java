package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 个人访问令牌请求响应实体
 */
@Data
@NoArgsConstructor
public class SysApiTokenDTO implements Serializable {

    @Data
    @NoArgsConstructor
    public static class AddDTO implements Serializable {

        /**
         * 令牌名称
         */
        @NotBlank(message = "令牌名称不能为空")
        @Size(max = 50, message = "令牌名称最大长度不能超过50位")
        private String tokenName;

        /**
         * 有效期天数：30、90、180、365；0 表示永不过期
         */
        @NotNull(message = "有效期不能为空")
        private Integer validityDays;
    }

    @Data
    @NoArgsConstructor
    public static class UpdateDTO implements Serializable {

        @NotBlank(message = "id不能为空")
        private String id;

        /**
         * 令牌名称
         */
        @NotBlank(message = "令牌名称不能为空")
        @Size(max = 50, message = "令牌名称最大长度不能超过50位")
        private String tokenName;

    }

    @Data
    @NoArgsConstructor
    public static class ListDTO implements Serializable {

        private String id;

        /**
         * 令牌名称
         */
        private String tokenName;

        /**
         * 脱敏后的令牌
         */
        private String maskedToken;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 令牌过期时间，为空表示永不过期
         */
        private LocalDateTime expiresTime;
    }

    @Data
    @NoArgsConstructor
    public static class TokenDTO implements Serializable {

        /**
         * 完整令牌，仅新增或复制时返回
         */
        private String token;

        /**
         * 令牌过期时间，为空表示永不过期
         */
        private LocalDateTime expiresTime;

        public TokenDTO(String token, LocalDateTime expiresTime) {
            this.token = token;
            this.expiresTime = expiresTime;
        }
    }

    @Data
    @NoArgsConstructor
    public static class ValidateReqDTO implements Serializable {

        /**
         * API Token 的 SHA-256 哈希
         */
        @NotBlank(message = "令牌哈希不能为空")
        private String tokenHash;

        /**
         * 当前请求路径
         */
        @NotBlank(message = "接口路径不能为空")
        private String requestPath;
    }

    @Data
    @NoArgsConstructor
    public static class ValidateRespDTO implements Serializable {

        /**
         * 令牌是否有效
         */
        private Boolean tokenValid = false;

        /**
         * 请求路径是否命中白名单
         */
        private Boolean pathAllowed = false;

        private String tokenId;

        /**
         * 令牌过期时间，为空表示永不过期
         */
        private LocalDateTime expiresTime;

        private String userId;

        private String userName;

        private String realName;

        private String mobile;

        private String userAccount;

        private Boolean superAdmin;
    }
}
