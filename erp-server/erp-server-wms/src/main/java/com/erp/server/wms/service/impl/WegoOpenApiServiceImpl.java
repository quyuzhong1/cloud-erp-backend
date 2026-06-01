package com.erp.server.wms.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.common.core.exception.ServiceException;
import com.common.core.utils.OkHttpUtils;
import com.erp.model.wms.dto.WegoWarehouseQueryDTO;
import com.erp.server.wms.service.WegoOpenApiService;
import com.sdk.wms.wego.constants.WeGoConstants;
import com.sdk.wms.wego.utils.WeGoSignUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * WEGO 开放接口服务实现
 */
@Service
public class WegoOpenApiServiceImpl implements WegoOpenApiService {

    @Override
    public JSONObject queryWarehouse(WegoWarehouseQueryDTO.QueryReqDTO dto) {
        Map<String, Object> params = new HashMap<>();
        params.put("accessToken", dto.getAccessToken());
        params.put("interfaceType", WeGoConstants.WAREHOUSE_GET);
        if (dto.getBizParams() != null && !dto.getBizParams().isEmpty()) {
            params.putAll(dto.getBizParams());
        }
        String sign = WeGoSignUtils.sign(params, dto.getSecret());
        params.put(WeGoSignUtils.SIGN_FIELD, sign);

        String url = buildRouterUrl(dto.getDomain());
        String response = OkHttpUtils.doPostJson(url, params, null);
        ThirdWarehouseContext.setRequestJson(JSON.toJSONString(params));
        ThirdWarehouseContext.setResponseJson(response);

        JSONObject result = new JSONObject();
        result.put("requestUrl", url);
        result.put("requestBody", params);
        if (response == null || response.isEmpty()) {
            throw new ServiceException("WEGO 查询仓库接口返回为空");
        }
        try {
            result.put("responseBody", JSON.parseObject(response));
        } catch (Exception ex) {
            result.put("responseBody", response);
        }
        return result;
    }

    private String buildRouterUrl(String domain) {
        String normalized = domain == null ? "" : domain.trim();
        if (normalized.isEmpty()) {
            throw new ServiceException("WEGO域名不能为空");
        }
        if (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized + WeGoConstants.ROUTER_PATH;
    }
}
