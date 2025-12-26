package com.erp.sdk.oms.yunting.cem.handler;

import com.erp.sdk.oms.yunting.cem.api.TokenApi;
import com.erp.sdk.oms.yunting.cem.client.ApiException;
import com.erp.sdk.oms.yunting.cem.dto.YuntingCredentialDTO;
import com.erp.sdk.oms.yunting.cem.enums.YuntingErrorCodeEnum;
import com.erp.sdk.oms.yunting.cem.model.TokenResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 云听CEM访问凭证处理器
 *
 * @author ERP System
 */
@Slf4j
@Component
public class YuntingTokenHandler {

    /**
     * 获取访问凭证
     *
     * @param source       来源标识
     * @param thirdPartyId 第三方应用ID
     * @param projectId    项目ID
     * @return 云听凭证DTO
     * @throws ApiException API异常
     */
    public YuntingCredentialDTO getCredential(String source, String thirdPartyId, String projectId) throws ApiException {
        // 参数验证
        if (source == null || source.trim().isEmpty()) {
            throw new ApiException("来源标识(source)不能为空");
        }
        if (thirdPartyId == null || thirdPartyId.trim().isEmpty()) {
            throw new ApiException("第三方应用ID(thirdPartyId)不能为空");
        }
        if (projectId == null || projectId.trim().isEmpty()) {
            throw new ApiException("项目ID(projectId)不能为空");
        }

        // 创建TokenApi
        TokenApi tokenApi = new TokenApi();

        // 获取访问令牌
        log.info("云听CEM - 获取访问凭证: source={}, thirdPartyId={}", source, thirdPartyId);
        TokenResponse response = tokenApi.getToken(source, thirdPartyId);

        // 验证响应
        validateResponse(response);

        // 转换为凭证DTO
        TokenResponse.TokenResult result = response.getResult();
        YuntingCredentialDTO credential = YuntingCredentialDTO.builder()
                .source(source)
                .thirdPartyId(thirdPartyId)
                .projectId(projectId)
                .accessToken(result.getAccessToken())
                .expiresIn(result.getExpiresIn())
                .createTimestamp(result.getCreateTimestamp())
                .build();

        log.info("云听CEM - 访问凭证获取成功，有效期：{}秒，剩余时间：{}秒", 
                credential.getExpiresIn(), credential.getRemainingTime());

        return credential;
    }

    /**
     * 刷新访问凭证
     *
     * @param credential 现有凭证
     * @return 新的凭证
     * @throws ApiException API异常
     */
    public YuntingCredentialDTO refreshCredential(YuntingCredentialDTO credential) throws ApiException {
        if (credential == null) {
            throw new ApiException("凭证不能为空");
        }

        log.info("云听CEM - 刷新访问凭证");
        return getCredential(credential.getSource(), credential.getThirdPartyId(), credential.getProjectId());
    }

    /**
     * 验证响应
     */
    private void validateResponse(TokenResponse response) throws ApiException {
        if (response == null) {
            throw new ApiException("获取访问凭证失败：响应为空");
        }

        Integer code = response.getCode();
        if (!YuntingErrorCodeEnum.isSuccess(code)) {
            YuntingErrorCodeEnum errorCode = YuntingErrorCodeEnum.getByCode(code);
            throw new ApiException(code, "获取访问凭证失败: " + errorCode.getDescription() + " - " + response.getMsg());
        }

        if (response.getResult() == null) {
            throw new ApiException("获取访问凭证失败：结果为空");
        }

        if (response.getResult().getAccessToken() == null || response.getResult().getAccessToken().trim().isEmpty()) {
            throw new ApiException("获取访问凭证失败：accessToken为空");
        }
    }
}

