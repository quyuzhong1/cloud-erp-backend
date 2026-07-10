package com.erp.model.sys.constants;

import com.common.business.constant.TokenConstants;

/**
 * 个人 API Token 认证常量。
 */
public final class SysApiTokenConstants {

    private SysApiTokenConstants() {
    }

    public static final String TOKEN_PREFIX = "sdc_pat_";

    public static final String INTERNAL_AUTH_HEADER = "X-Erp-Api-Token-Auth";

    public static final String INTERNAL_TOKEN_ID_HEADER = TokenConstants.API_TOKEN_ID_HEADER;

    public static final String INTERNAL_AUTH_VALUE = "true";

    /**
     * 网关 filter 链内部属性，只能由 AuthGatewayFilter 设置，用于避免信任客户端伪造的内部认证头。
     */
    public static final String INTERNAL_AUTH_ATTRIBUTE = "erpApiTokenAuth";
}
