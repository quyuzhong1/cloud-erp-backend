package com.erp.server.dmp.inout.handler.input.task.init.api.wego;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.business.wrapper.FeignQuery;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.model.wms.dto.WegoInventoryQueryDTO;
import com.erp.model.wms.entity.OverseasProviderEntity;
import com.erp.model.wms.entity.OverseasProviderWarehouseEntity;
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
 * WEGO 海外仓库存拉取 InitHandler。
 * <p>
 * 按服务商维度分页调用 WEGO {@code 2c.inventory.search} 接口，
 * 将库存明细数据写入 DMP Init 阶段，后续由 Mongo/Dmp Handler 消费。
 * <p>
 * 因父类 {@link DmpInputInitHandler} 含成员变量，必须使用多例 {@code @Scope("prototype")}。
 */
@Slf4j
@Service
@Scope("prototype")
public class WegoInventoryInitHandler extends DmpInputInitHandler {

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
            log.warn("[WEGO库存] 无已授权的WEGO服务商配置，跳过");
            return Collections.emptyList();
        }

        // 通过 nextLevelId 定位本次任务对应的服务商
        OverseasProviderEntity provider = providerList.stream()
                .filter(e -> e.getId().equalsIgnoreCase(dmpInputTaskEntity.getNextLevelId()))
                .findFirst()
                .orElse(null);
        if (provider == null) {
            throw new ServiceException("WEGO库存：nextLevelId[" + dmpInputTaskEntity.getNextLevelId() + "]对应的服务商不存在");
        }

        Map<String, Object> authJson = provider.getAuthJson();
        if (authJson == null || authJson.isEmpty()) {
            throw new ServiceException("WEGO库存：服务商[" + provider.getId() + "]auth_json为空");
        }
        String appToken = toStr(authJson.get(AUTH_KEY_APP_TOKEN));
        String appSecret = toStr(authJson.get(AUTH_KEY_APP_SECRET));
        if (StringUtils.isAnyBlank(appToken, appSecret)) {
            throw new ServiceException("WEGO库存：服务商[" + provider.getId() + "]appToken/appSecret缺失");
        }

        // 查询服务商下已启用的仓库
        List<OverseasProviderWarehouseEntity> warehouseList = FeignQuery.create(OverseasProviderWarehouseEntity.class)
                .eq(OverseasProviderWarehouseEntity::getMainId, provider.getId())
                .eq(OverseasProviderWarehouseEntity::getDisabled, Boolean.FALSE)
                .list();
        if (CollUtil.isEmpty(warehouseList)) {
            log.warn("[WEGO库存] 服务商[id={}]下无可用仓库，跳过", provider.getId());
            return Collections.emptyList();
        }

        String authId = provider.getId();
        List<Object> allInventory = new ArrayList<>();

        for (OverseasProviderWarehouseEntity warehouse : warehouseList) {
            String warehouseCode = warehouse.getPlatformWarehouseCode();
            String warehouseName = warehouse.getPlatformWarehouseName();
            List<Object> warehouseInventory = fetchInventoryByWarehouse(appToken, appSecret, warehouseCode, authId, warehouseName);
            allInventory.addAll(warehouseInventory);
        }

        if (allInventory.isEmpty()) {
            log.info("[WEGO库存] 服务商[id={}]所有仓库库存均为空", authId);
            return Collections.emptyList();
        }

        JSONArray result = JSON.parseArray(JSONObject.toJSONString(allInventory));
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
     * 分页拉取指定仓库的库存数据。
     */
    private List<Object> fetchInventoryByWarehouse(String appToken, String appSecret,
                                                    String warehouseCode, String authId, String warehouseName) {
        List<Object> inventoryList = new ArrayList<>();
        int pageNum = 1;

        while (pageNum <= MAX_PAGE_LIMIT) {
            WegoInventoryQueryDTO.QueryReqDTO reqDTO = new WegoInventoryQueryDTO.QueryReqDTO();
            reqDTO.setAccessToken(appToken);
            reqDTO.setSecret(appSecret);
            reqDTO.setWarehouseCode(warehouseCode);
            reqDTO.setPageNum(pageNum);
            reqDTO.setPageSize(DEFAULT_PAGE_SIZE);

            JSONObject response;
            try {
                response = wegoOpenApiService.queryInventory(reqDTO);
            } catch (Exception e) {
                log.error("[WEGO库存] 服务商[id={}] 仓库[{}]调用异常，pageNum={}", authId, warehouseCode, pageNum, e);
                break;
            }

            JSONObject pageResult = extractPageResult(response, authId, warehouseCode);
            if (pageResult == null) {
                break;
            }

            JSONArray list = pageResult.getJSONArray("list");
            if (list != null && !list.isEmpty()) {
                for (int i = 0; i < list.size(); i++) {
                    JSONObject item = list.getJSONObject(i);
                    if (item != null) {
                        // 补充仓库信息，便于下游 Handler 直接使用
                        item.put("warehouseCode", warehouseCode);
                        item.put("warehouseName", warehouseName);
                        inventoryList.add(item);
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
            log.warn("[WEGO库存] 服务商[id={}] 仓库[{}]已达最大翻页上限({})，可能存在未拉取数据",
                    authId, warehouseCode, MAX_PAGE_LIMIT);
        }
        log.info("[WEGO库存] 服务商[id={}] 仓库[{}] 共拉取库存明细={}条，页数={}",
                authId, warehouseCode, inventoryList.size(), pageNum);
        return inventoryList;
    }

    /**
     * 解析 WEGO 接口响应，提取分页对象。
     * 接口结构：{success, errorCode, errorMsg, serverTime, result:{pageNum,pageSize,total,pages,list,emptyFlag}}
     *
     * @return 分页对象，失败时返回 null
     */
    private JSONObject extractPageResult(JSONObject response, String authId, String warehouseCode) {
        if (response == null) {
            log.error("[WEGO库存] 服务商[id={}] 仓库[{}]接口响应为空", authId, warehouseCode);
            return null;
        }
        Boolean success = response.getBoolean("success");
        if (!Boolean.TRUE.equals(success)) {
            String errorCode = String.valueOf(response.get("errorCode"));
            String errorMsg = String.valueOf(response.get("errorMsg"));
            log.error("[WEGO库存] 服务商[id={}] 仓库[{}]接口返回失败: errorCode={}, errorMsg={}",
                    authId, warehouseCode, errorCode, errorMsg);
            throw new ServiceException("WEGO库存接口返回失败: errorCode=" + errorCode + ", errorMsg=" + errorMsg);
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
        log.error("[WEGO库存] 服务商[id={}] 仓库[{}] result结构异常: {}", authId, warehouseCode, resultObj);
        return null;
    }

    private String toStr(Object value) {
        return Objects.isNull(value) ? null : value.toString();
    }
}
