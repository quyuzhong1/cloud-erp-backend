package com.erp.server.dmp.inout.handler.input.task.init.api.aiya;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.dto.AiyaInventoryQueryDTO;
import com.erp.model.wms.entity.OverseasProviderWarehouseEntity;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 爱亚海外仓库存拉取 InitHandler，对齐 {@code WegoInventoryInitHandler} 的整体结构（按仓库分页拉取）。
 * <p>
 * 按服务商下每个已启用仓库分页调用爱亚 {@code 2c.inventory.search}（库存概要查询），
 * 逐条补充 warehouseCode / warehouseName 后写入 DMP Init 阶段。
 * <p>
 * 注意：该接口响应结构为 {@code {Code, message, success, inventoryVOList:[...]}}，与仓库接口的
 * {@code resultList}、入库/出库/退货分页接口的 {@code result:{list,pages,emptyFlag}} 结构均不同，
 * 不能复用 {@link AbstractAiyaInitHandler#extractPageResult}（该方法假设的是后者的分页结构），
 * 本类单独解析 {@code inventoryVOList}，做法与 {@code AiyaSkuInitHandler} 一致。
 * <p>
 * 2026-07-16 已按爱亚接口文档页面截图核对 {@code inventoryVOList} 明细字段，比翻译稿多出
 * {@code customerCode}/{@code barcode}/{@code skuStatus} 三个字段；本类不做字段白名单过滤，
 * 这些新字段会随原始 JSON 原样透传进 DMP Init 阶段，无需改动本类代码即可携带。但需特别关注
 * {@code skuStatus}：疑似与 6.3.2 入库签收段的同名字段（GOOD/DAMAGE）同义，若库存接口也按
 * 良品/不良品拆成两条明细（即同一 warehouseCode+sku 出现多条、仅 skuStatus 不同的记录），
 * 而下游 {@code dmp_third_inventory} 的去重唯一键若只按 warehouseCode+sku 配置，会导致其中一条
 * 明细被覆盖丢失——需联调真实接口确认是否拆行，并跟产品/DMP 配置一起确认唯一键是否要纳入
 * skuStatus，详见 docs/integrations/aiya-overseas-warehouse/README.md「待产品确认」。
 * <p>
 * TODO：文档未提供 {@code total}/{@code pages}/{@code emptyFlag} 等分页终止字段，暂以
 * "本页返回条数 &lt; pageSize" 判断已到最后一页，需联调真实接口后确认。
 * <p>
 * TODO：文档「库存数据」请求参数 {@code stockStatus} 标"是否必填=是"，但未给出可选枚举值，
 * 当前用占位值 {@link #STOCK_STATUS_PLACEHOLDER}，需联调/产品确认真实取值后替换，
 * 详见 docs/integrations/aiya-overseas-warehouse/README.md「待产品确认」。
 */
@Slf4j
@Service
@Scope("prototype")
public class AiyaInventoryInitHandler extends AbstractAiyaInitHandler {

    private static final String ACTION = "库存";
    private static final int DEFAULT_PAGE_SIZE = AiyaInventoryQueryDTO.DEFAULT_PAGE_SIZE;

    /**
     * TODO：文档「库存数据」{@code stockStatus} 标"必填"，但未给出可选枚举值/"查询全部状态"应传的值，
     * 当前占位为 "ALL"，未经真实接口验证，联调/产品确认后需替换为真实取值。
     */
    private static final String STOCK_STATUS_PLACEHOLDER = "ALL";

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
     * 分页拉取指定仓库的库存概要明细。
     */
    private List<Object> fetchInventoryByWarehouse(AiyaAuth auth, String warehouseCode, String warehouseName) {
        List<Object> inventoryList = new ArrayList<>();
        int pageNum = 1;
        while (pageNum <= MAX_PAGE_LIMIT) {
            AiyaInventoryQueryDTO.QueryReqDTO reqDTO = new AiyaInventoryQueryDTO.QueryReqDTO();
            reqDTO.setAccessToken(auth.getPartnerId());
            reqDTO.setSecret(auth.getPartnerKey());
            reqDTO.setCustomerCode(auth.getCustomerCode());
            reqDTO.setWarehouseCode(warehouseCode);
            reqDTO.setPageNum(pageNum);
            reqDTO.setPageSize(DEFAULT_PAGE_SIZE);
            reqDTO.setStockStatus(STOCK_STATUS_PLACEHOLDER);

            JSONObject response;
            try {
                response = aiyaOpenApiService.queryInventory(reqDTO);
            } catch (Exception e) {
                log.error("[爱亚库存] 服务商[id={}] 仓库[{}] 调用异常, pageNum={}",
                        auth.getAuthId(), warehouseCode, pageNum, e);
                throw new ServiceException(e, ApiError.WH_AIYA_PAGE_QUERY_ERROR, ACTION, pageNum);
            }

            JSONArray inventoryVOList = extractInventoryList(response, ACTION);
            if (inventoryVOList == null) {
                log.error("[爱亚库存] 服务商[id={}] 仓库[{}] 第{}页响应解析失败", auth.getAuthId(), warehouseCode, pageNum);
                throw new ServiceException(ApiError.WH_AIYA_PAGE_PARSE_FAILED, ACTION, pageNum);
            }

            for (int i = 0; i < inventoryVOList.size(); i++) {
                JSONObject item = inventoryVOList.getJSONObject(i);
                if (item != null) {
                    item.put("warehouseCode", warehouseCode);
                    item.put("warehouseName", warehouseName);
                    inventoryList.add(item);
                }
            }

            if (inventoryVOList.isEmpty() || inventoryVOList.size() < DEFAULT_PAGE_SIZE) {
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

    /**
     * 解析爱亚库存查询响应，提取 {@code inventoryVOList} 数组。
     * <p>
     * 接口结构：{@code {Code, message, success, inventoryVOList:[...]}}，success=true 为成功；
     * success=false 视为真实失败直接抛出 {@link ServiceException}，与
     * {@link AiyaSkuInitHandler} 保持一致的"不静默降级"原则。
     *
     * @return success=true 时返回 inventoryVOList（可能为空数组）；response 为 null 或结构异常时返回 null，由调用方判定为解析失败
     */
    private JSONArray extractInventoryList(JSONObject response, String actionName) {
        if (response == null) {
            return null;
        }
        if (!Boolean.TRUE.equals(response.getBoolean("success"))) {
            throw new ServiceException(ApiError.WH_AIYA_RESPONSE_FAILED, actionName,
                    String.valueOf(response.get("Code")), String.valueOf(response.get("message")));
        }
        JSONArray inventoryVOList = response.getJSONArray("inventoryVOList");
        return inventoryVOList == null ? new JSONArray() : inventoryVOList;
    }
}
