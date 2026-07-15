package com.erp.server.dmp.inout.handler.input.task.init.api.aiya;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.model.wms.entity.OverseasProviderEntity;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.handler.input.task.init.DmpInputInitHandler;
import com.sdk.wms.aiya.service.AiyaOpenApiService;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * AIYA（爱亚）海外仓 DMP 输入 init 任务处理器公共基类。
 * <p>
 * 收敛 6 个爱亚 init handler 共用的「按 nextLevelId 定位授权 + 读取 customerCode/partnerKey +
 * 组装 DmpInputTaskInitDTO」逻辑，避免各业务 handler 重复复制。
 * <p>
 * 授权约定：爱亚不走 OAuth，凭证以 {@code customerCode}（客户编码）/ {@code partnerKey}（合作方密钥）
 * 形式存放于 {@code overseas_provider.auth_json}，分别对应 {@link AiyaOpenApiService} 的
 * accessToken / secret 参数；{@code partnerKey} 仅用于本地签名，不发送给第三方。
 * <p>
 * 因父类 {@link DmpInputInitHandler} 含成员变量，子类必须使用多例 {@code @Scope("prototype")}。
 */
public abstract class AbstractAiyaInitHandler extends DmpInputInitHandler {

    /**
     * 爱亚授权 JSON（overseas_provider.auth_json）中的 customerCode 字段 key。
     */
    protected static final String AUTH_KEY_CUSTOMER_CODE = "customerCode";

    /**
     * 爱亚授权 JSON（overseas_provider.auth_json）中的 partnerKey 字段 key。
     */
    protected static final String AUTH_KEY_PARTNER_KEY = "partnerKey";

    /**
     * 最大翻页保护，避免接口异常导致死循环。
     */
    protected static final int MAX_PAGE_LIMIT = 1000;

    @Resource
    protected AiyaOpenApiService aiyaOpenApiService;

    /**
     * 按 {@link #dmpInputTaskEntity} 的 {@code nextLevelId} 定位本次任务对应的已授权爱亚服务商，
     * 并解析 customerCode / partnerKey。
     *
     * @return 爱亚授权信息（authId + customerCode + partnerKey）
     */
    protected AiyaAuth resolveAuth() {
        List<OverseasProviderEntity> providerList = FeignQuery.create(OverseasProviderEntity.class)
                .eq(OverseasProviderEntity::getAuthStatus, AuthStatusEnum.ALREADY.getCode())
                .eq(OverseasProviderEntity::getCode, DmpBasicSystemCodeEnum.AIYA.getCode())
                .list();
        if (CollUtil.isEmpty(providerList)) {
            throw new ServiceException(ApiError.WH_AIYA_AUTH_INFO_NOT_FOUND);
        }
        OverseasProviderEntity provider = providerList.stream()
                .filter(e -> e.getId().equalsIgnoreCase(dmpInputTaskEntity.getNextLevelId()))
                .findFirst()
                .orElse(null);
        if (provider == null) {
            throw new ServiceException(ApiError.WH_AIYA_AUTH_ID_NOT_FOUND, dmpInputTaskEntity.getNextLevelId());
        }
        Map<String, Object> authJson = provider.getAuthJson();
        if (authJson == null || authJson.isEmpty()) {
            throw new ServiceException(ApiError.WH_AIYA_AUTH_JSON_EMPTY, provider.getId());
        }
        String customerCode = toStr(authJson.get(AUTH_KEY_CUSTOMER_CODE));
        String partnerKey = toStr(authJson.get(AUTH_KEY_PARTNER_KEY));
        if (CharSequenceUtil.hasBlank(customerCode, partnerKey)) {
            throw new ServiceException(ApiError.WH_AIYA_TOKEN_SECRET_MISSING, provider.getId());
        }
        return new AiyaAuth(provider.getId(), customerCode, partnerKey);
    }

    /**
     * 为每条记录补充 {@code authId} / {@code sourcePlatform} 后组装成单条 init DTO，
     * 与下游 mongo/dmp handler 的数据协议保持一致。
     *
     * @param result 已拉取的业务数据数组（原地补充公共字段）
     * @param authId 当前授权服务商 ID
     * @return 承载整个数组 JSON 的 init DTO
     */
    protected DmpInputTaskInitDTO buildInitDTO(JSONArray result, String authId) {
        result.forEach(item -> {
            JSONObject obj = (JSONObject) item;
            obj.put("authId", authId);
            obj.put("sourcePlatform", DmpBasicSystemCodeEnum.AIYA.getCode());
        });
        DmpInputTaskInitDTO dto = new DmpInputTaskInitDTO();
        dto.setMsg(result.toJSONString());
        return dto;
    }

    /**
     * 解析爱亚 JSONObject 响应中的分页对象，兼容 result 为对象（含 list/pages/emptyFlag）或数组两种结构。
     *
     * @param response   爱亚接口原始响应
     * @param actionName 业务动作名（用于异常文案）
     * @return 分页对象；success=false 时抛业务异常，结构异常时返回 null
     */
    protected JSONObject extractPageResult(JSONObject response, String actionName) {
        if (response == null) {
            return null;
        }
        if (!Boolean.TRUE.equals(response.getBoolean("success"))) {
            throw new ServiceException(ApiError.WH_AIYA_RESPONSE_FAILED, actionName,
                    String.valueOf(response.get("errorCode")), String.valueOf(response.get("errorMsg")));
        }
        Object resultObj = response.get("result");
        if (resultObj instanceof JSONObject) {
            return (JSONObject) resultObj;
        }
        if (resultObj instanceof JSONArray) {
            JSONObject wrap = new JSONObject();
            wrap.put("list", resultObj);
            wrap.put("pageNum", 1);
            wrap.put("pages", 1);
            wrap.put("emptyFlag", ((JSONArray) resultObj).isEmpty());
            return wrap;
        }
        return null;
    }

    protected String toStr(Object value) {
        return value == null ? null : value.toString();
    }

    /**
     * 爱亚授权信息载体。
     */
    @Getter
    @AllArgsConstructor
    protected static class AiyaAuth {
        private final String authId;
        private final String customerCode;
        private final String partnerKey;
    }
}
