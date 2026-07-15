package com.erp.server.dmp.inout.handler.input.task.init.api.aiya;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.entity.OverseasProviderWarehouseEntity;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import org.apache.commons.lang3.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 爱亚海外仓库存拉取 InitHandler，对齐 {@code WegoInventoryInitHandler}。
 * <p>
 * 按服务商下每个已启用仓库分页调用爱亚 {@code 2c.inventory.search}，
 * 逐条补充 warehouseCode / warehouseName 后写入 DMP Init 阶段。
 */
@Slf4j
@Service
@Scope("prototype")
public class AiyaInventoryInitHandler extends AbstractAiyaInitHandler {

    private static final String ACTION = "库存";
    private static final int DEFAULT_PAGE_SIZE = 200;

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        AiyaAuth auth = resolveAuth();

        List<OverseasProviderWarehouseEntity> warehouseList = FeignQuery.create(OverseasProviderWarehouseEntity.class)
                .eq(OverseasProviderWarehouseEntity::getMainId, auth.getAuthId())
                .eq(OverseasProviderWarehouseEntity::getDisabled, Boolean.FALSE)
                .list();
        if (CollUtil.isEmpty(warehouseList)) {
            log.warn("[爱亚库存] 服务商[id={}] 下无可用仓库，跳过", auth.getAuthId());
            return Collections.emptyList();
        }

        List<Object> allInventory = new ArrayList<>();
        for (OverseasProviderWarehouseEntity warehouse : warehouseList) {
            String warehouseCode = warehouse.getPlatformWarehouseCode();
            if (StringUtils.isBlank(warehouseCode)) {
                log.warn("[爱亚库存] 服务商[id={}] 仓库[id={}] platformWarehouseCode 为空，跳过",
                        auth.getAuthId(), warehouse.getId());
                continue;
            }
            allInventory.addAll(fetchInventoryByWarehouse(auth, warehouseCode, warehouse.getPlatformWarehouseName()));
        }

        if (allInventory.isEmpty()) {
            log.info("[爱亚库存] 服务商[id={}] 所有仓库库存均为空", auth.getAuthId());
            return Collections.emptyList();
        }

        JSONArray result = JSON.parseArray(JSONObject.toJSONString(allInventory));
        return Collections.singletonList(buildInitDTO(result, auth.getAuthId()));
    }

    /**
     * 分页拉取指定仓库的库存明细。
     */
    private List<Object> fetchInventoryByWarehouse(AiyaAuth auth, String warehouseCode, String warehouseName) {
        List<Object> inventoryList = new ArrayList<>();
        int pageNum = 1;
        while (pageNum <= MAX_PAGE_LIMIT) {
            Map<String, Object> bizParams = new HashMap<>();
            bizParams.put("warehouseCode", warehouseCode);

            JSONObject response;
            try {
                response = aiyaOpenApiService.queryInventory(auth.getPartnerId(), auth.getPartnerKey(),
                        auth.getCustomerCode(), pageNum, DEFAULT_PAGE_SIZE, bizParams);
            } catch (Exception e) {
                log.error("[爱亚库存] 服务商[id={}] 仓库[{}] 调用异常, pageNum={}",
                        auth.getAuthId(), warehouseCode, pageNum, e);
                throw new ServiceException(e, ApiError.WH_AIYA_PAGE_QUERY_ERROR, ACTION, pageNum);
            }

            JSONObject pageResult = extractPageResult(response, ACTION);
            if (pageResult == null) {
                throw new ServiceException(ApiError.WH_AIYA_PAGE_PARSE_FAILED, ACTION, pageNum);
            }

            JSONArray list = pageResult.getJSONArray("list");
            if (list != null && !list.isEmpty()) {
                for (int i = 0; i < list.size(); i++) {
                    JSONObject item = list.getJSONObject(i);
                    if (item != null) {
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
            log.error("[爱亚库存] 服务商[id={}] 仓库[{}] 已达最大翻页上限({})，任务中止",
                    auth.getAuthId(), warehouseCode, MAX_PAGE_LIMIT);
            throw new ServiceException(ApiError.WH_AIYA_PAGE_LIMIT_EXCEEDED, ACTION, MAX_PAGE_LIMIT, inventoryList.size());
        }
        log.info("[爱亚库存] 服务商[id={}] 仓库[{}] 共拉取库存明细={}条", auth.getAuthId(), warehouseCode, inventoryList.size());
        return inventoryList;
    }
}
