package com.erp.sdk.oms.yunting.cem.api;

import com.erp.sdk.oms.yunting.cem.client.ApiClient;
import com.erp.sdk.oms.yunting.cem.client.ApiException;
import com.erp.sdk.oms.yunting.cem.client.ApiResponse;
import com.erp.sdk.oms.yunting.cem.model.TokenResponse;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * 云听CEM访问凭证API
 *
 * @author ERP System
 */
@Slf4j
public class TokenApi {

    private ApiClient apiClient;

    /**
     * 构造函数
     */
    public TokenApi() {
        this(new ApiClient());
    }

    /**
     * 构造函数
     *
     * @param apiClient API客户端
     */
    public TokenApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * 获取API客户端
     */
    public ApiClient getApiClient() {
        return apiClient;
    }

    /**
     * 设置API客户端
     */
    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * 获取访问凭证
     * <p>
     * 此接口获取必要的登录信息，可用于组装完整URL直接跳转登录云听系统
     *
     * @param source        来源标识（固定参数，由云听预设）
     * @param thirdPartyId  第三方应用名称
     * @return TokenResponse 访问凭证响应
     * @throws ApiException 如果API调用失败
     */
    public TokenResponse getToken(String source, String thirdPartyId) throws ApiException {
        ApiResponse<TokenResponse> response = getTokenWithHttpInfo(source, thirdPartyId);
        return response.getData();
    }

    /**
     * 获取访问凭证（带HTTP信息）
     *
     * @param source        来源标识
     * @param thirdPartyId  第三方应用名称
     * @return ApiResponse&lt;TokenResponse&gt; 包含HTTP信息的响应
     * @throws ApiException 如果API调用失败
     */
    public ApiResponse<TokenResponse> getTokenWithHttpInfo(String source, String thirdPartyId) throws ApiException {
        // 验证必填参数
        if (source == null) {
            throw new ApiException("缺少必填参数 'source'");
        }
        if (thirdPartyId == null) {
            throw new ApiException("缺少必填参数 'thirdPartyId'");
        }

        // 构建查询参数
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("source", source);
        queryParams.put("third_party_id", thirdPartyId);

        // 构建请求路径
        String path = "/oauth2/token";

        // 定义返回类型
        Type returnType = new TypeToken<TokenResponse>() {}.getType();

        // 执行GET请求
        log.info("云听CEM - 获取访问凭证: source={}, thirdPartyId={}", source, thirdPartyId);
        return apiClient.executeGet(path, queryParams, returnType);
    }
}

