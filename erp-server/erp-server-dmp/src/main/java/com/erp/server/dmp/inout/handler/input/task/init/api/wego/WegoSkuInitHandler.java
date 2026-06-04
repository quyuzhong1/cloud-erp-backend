package com.erp.server.dmp.inout.handler.input.task.init.api.wego;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.business.wrapper.FeignQuery;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.model.wms.dto.WegoSkuQueryDTO;
import com.erp.model.wms.entity.OverseasProviderEntity;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.input.task.init.DmpInputInitHandler;
import com.sdk.wms.wego.service.WegoOpenApiService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * WEGO SKU 基础数据拉取 InitHandler。
 * <p>
 * 分页调用 WEGO {@code product.search} 接口，
 * 将 SKU 明细数据写入 DMP Init 阶段，后续由 Mongo/Dmp Handler 消费。
 * <p>
 * 因父类 {@link DmpInputInitHandler} 含成员变量，必须使用多例 {@code @Scope("prototype")}。
 */
@Slf4j
@Service
@Scope("prototype")
public class WegoSkuInitHandler extends DmpInputInitHandler {

    private static final String AUTH_KEY_APP_TOKEN = "appToken";
    private static final String AUTH_KEY_APP_SECRET = "appSecret";
    private static final int DEFAULT_PAGE_SIZE = 200;
    private static final int MAX_PAGE_LIMIT = 1000;

    @Resource
    private WegoOpenApiService wegoOpenApiService;

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        List<OverseasProviderEntity> providerList = FeignQuery.create(OverseasProviderEntity.class)
                .eq(OverseasProviderEntity::getAuthStatus, AuthStatusEnum.ALREADY.getCode())
                .eq(OverseasProviderEntity::getCode, DmpBasicSystemCodeEnum.WEGO.getCode())
                .list();
        if (CollUtil.isEmpty(providerList)) {
            log.warn("[WEGO SKU] 无已授权的WEGO服务商配置，跳过");
            return Collections.emptyList();
        }

        OverseasProviderEntity provider = providerList.stream()
                .filter(e -> e.getId().equalsIgnoreCase(dmpInputTaskEntity.getNextLevelId()))
                .findFirst()
                .orElse(null);
        if (provider == null) {
            throw new ServiceException("WEGO SKU：nextLevelId[" + dmpInputTaskEntity.getNextLevelId() + "]对应的服务商不存在");
        }

        Map<String, Object> authJson = provider.getAuthJson();
        if (authJson == null || authJson.isEmpty()) {
            throw new ServiceException("WEGO SKU：服务商[" + provider.getId() + "]auth_json为空");
        }
        String appToken = toStr(authJson.get(AUTH_KEY_APP_TOKEN));
        String appSecret = toStr(authJson.get(AUTH_KEY_APP_SECRET));
        if (StringUtils.isAnyBlank(appToken, appSecret)) {
            throw new ServiceException("WEGO SKU：服务商[" + provider.getId() + "]appToken/appSecret缺失");
        }

        String authId = provider.getId();
        List<Object> allSkuList = new ArrayList<>();
        int pageNum = 1;

        while (pageNum <= MAX_PAGE_LIMIT) {
            WegoSkuQueryDTO.QueryReqDTO reqDTO = new WegoSkuQueryDTO.QueryReqDTO();
            reqDTO.setAccessToken(appToken);
            reqDTO.setSecret(appSecret);
            reqDTO.setPageNum(pageNum);
            reqDTO.setPageSize(DEFAULT_PAGE_SIZE);

            JSONObject response;
            try {
                response = wegoOpenApiService.querySku(reqDTO);
            } catch (Exception e) {
                log.error("[WEGO SKU] 服务商[id={}]调用异常，pageNum={}", authId, pageNum, e);
                break;
            }

            JSONObject pageResult = extractPageResult(response, authId);
            if (pageResult == null) {
                break;
            }

            JSONArray list = pageResult.getJSONArray("list");
            if (list != null && !list.isEmpty()) {
                for (int i = 0; i < list.size(); i++) {
                    JSONObject item = list.getJSONObject(i);
                    if (item != null) {
                        allSkuList.add(item);
                    }
                }
            }

            Boolean emptyFlag = pageResult.getBoolean("emptyFlag");
            Integer pages = pageResult.getInteger("pages");
            if (Boolean.TRUE.equals(emptyFlag)
                    || list == null
                    || list.isEmpty()
                    || (Objects.nonNull(pages) && pageNum >= pages)) {
                break;
            }
            pageNum++;
        }

        if (pageNum > MAX_PAGE_LIMIT) {
            log.warn("[WEGO SKU] 服务商[id={}]已达最大翻页上限({})，可能存在未拉取数据", authId, MAX_PAGE_LIMIT);
        }
        log.info("[WEGO SKU] 服务商[id={}] 共拉取SKU={}条，页数={}", authId, allSkuList.size(), pageNum);

        if (allSkuList.isEmpty()) {
            return Collections.emptyList();
        }

        JSONArray result = JSON.parseArray(JSONObject.toJSONString(allSkuList));
        result.forEach(item -> {
            JSONObject obj = (JSONObject) item;
            obj.put("authId", authId);
            obj.put("sourcePlatform", DmpBasicSystemCodeEnum.WEGO.getCode());
        });

        DmpInputTaskInitDTO initDTO = new DmpInputTaskInitDTO();
        initDTO.setMsg(result.toJSONString());
        return Collections.singletonList(initDTO);
    }

    /**
     * 解析 WEGO 接口响应，提取分页对象。
     * 接口结构：{success, errorCode, errorMsg, serverTime, result:{pageNum,pageSize,total,pages,list,emptyFlag}}
     *
     * @return 分页对象，失败时返回 null
     */
    private JSONObject extractPageResult(JSONObject response, String authId) {
        if (response == null) {
            log.error("[WEGO SKU] 服务商[id={}]接口响应为空", authId);
            return null;
        }
        Boolean success = response.getBoolean("success");
        if (!Boolean.TRUE.equals(success)) {
            String errorCode = String.valueOf(response.get("errorCode"));
            String errorMsg = String.valueOf(response.get("errorMsg"));
            log.error("[WEGO SKU] 服务商[id={}]接口返回失败: errorCode={}, errorMsg={}", authId, errorCode, errorMsg);
            throw new ServiceException("WEGO SKU接口返回失败: errorCode=" + errorCode + ", errorMsg=" + errorMsg);
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
        log.error("[WEGO SKU] 服务商[id={}] result结构异常: {}", authId, resultObj);
        return null;
    }

    private String toStr(Object value) {
        return Objects.isNull(value) ? null : value.toString();
    }
}
