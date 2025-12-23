package com.erp.sdk.oms.yunting.cem.utils;

import com.erp.sdk.oms.yunting.cem.api.CommentApi;
import com.erp.sdk.oms.yunting.cem.api.TokenApi;
import com.erp.sdk.oms.yunting.cem.client.ApiClient;
import com.erp.sdk.oms.yunting.cem.client.ApiException;
import com.erp.sdk.oms.yunting.cem.model.TokenResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * 云听CEM API工具类
 *
 * @author ERP System
 */
@Slf4j
public class YuntingApiUtils {

    /**
     * 创建已认证的CommentApi实例
     *
     * @param source       来源标识
     * @param thirdPartyId 第三方应用ID
     * @return CommentApi实例
     * @throws ApiException 如果获取Token失败
     */
    public static CommentApi createAuthenticatedCommentApi(String source, String thirdPartyId) throws ApiException {
        // 获取访问令牌
        TokenApi tokenApi = new TokenApi();
        TokenResponse tokenResponse = tokenApi.getToken(source, thirdPartyId);

        // 验证响应
        if (tokenResponse == null || tokenResponse.getResult() == null) {
            throw new ApiException("获取访问凭证失败：响应为空");
        }

        if (!Integer.valueOf(20000).equals(tokenResponse.getCode())) {
            throw new ApiException("获取访问凭证失败：" + tokenResponse.getMsg());
        }

        String accessToken = tokenResponse.getResult().getAccessToken();
        if (StringUtils.isBlank(accessToken)) {
            throw new ApiException("获取访问凭证失败：accessToken为空");
        }

        log.info("云听CEM - 获取访问凭证成功，令牌将在{}秒后过期", 
                tokenResponse.getResult().getExpiresIn());

        // 创建API客户端并设置访问令牌
        ApiClient apiClient = new ApiClient(accessToken);
        return new CommentApi(apiClient);
    }

    /**
     * 刷新访问令牌
     *
     * @param commentApi   CommentApi实例
     * @param source       来源标识
     * @param thirdPartyId 第三方应用ID
     * @throws ApiException 如果刷新Token失败
     */
    public static void refreshAccessToken(CommentApi commentApi, String source, String thirdPartyId) throws ApiException {
        TokenApi tokenApi = new TokenApi();
        TokenResponse tokenResponse = tokenApi.getToken(source, thirdPartyId);

        if (tokenResponse == null || tokenResponse.getResult() == null) {
            throw new ApiException("刷新访问凭证失败：响应为空");
        }

        if (!Integer.valueOf(20000).equals(tokenResponse.getCode())) {
            throw new ApiException("刷新访问凭证失败：" + tokenResponse.getMsg());
        }

        String accessToken = tokenResponse.getResult().getAccessToken();
        if (StringUtils.isBlank(accessToken)) {
            throw new ApiException("刷新访问凭证失败：accessToken为空");
        }

        commentApi.getApiClient().setAccessToken(accessToken);
        log.info("云听CEM - 刷新访问凭证成功");
    }

    /**
     * 验证时间格式
     *
     * @param time 时间字符串
     * @return 是否有效
     */
    public static boolean isValidTimeFormat(String time) {
        if (StringUtils.isBlank(time)) {
            return false;
        }
        // 简单验证格式：yyyy-MM-dd HH:mm:ss
        String pattern = "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$";
        return time.matches(pattern);
    }

    /**
     * 处理API异常
     *
     * @param e API异常
     * @return 友好的错误信息
     */
    public static String handleApiException(ApiException e) {
        if (e == null) {
            return "未知错误";
        }

        int code = e.getCode();
        String responseBody = e.getResponseBody();

        StringBuilder sb = new StringBuilder();
        sb.append("云听CEM API调用失败");
        
        if (code > 0) {
            sb.append(" [状态码: ").append(code).append("]");
        }

        if (StringUtils.isNotBlank(e.getMessage())) {
            sb.append(" - ").append(e.getMessage());
        }

        if (StringUtils.isNotBlank(responseBody)) {
            sb.append(" | 响应体: ").append(responseBody);
        }

        return sb.toString();
    }
}

