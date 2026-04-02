package com.erp.server.dmp.inout.handler.output.task.api;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpWdtWarehouseInventoryRecordEntity;
import com.erp.model.dmp.enums.InventoryBillStatusEnum;
import com.erp.model.dmp.enums.InventoryOrderTypeEnum;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.dmp.convert.DmpWdtConverter;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import com.erp.server.dmp.service.DmpWdtWarehouseInventoryRecordService;
import com.sdk.wangdian.sdk.api.wms.stockin.dto.CreateOtherStockinRequest;
import com.sdk.wangdian.sdk.api.wms.stockout.dto.CreateOtherStockoutRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 旺店通原始订单推送数帝云
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpOutputWdtQueryInventoryHandler extends DmpOutputWdtBaseTaskHandler {
    @Resource
    private SysUserFeign sysUserFeign;

    @Override
    public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
        Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps = dmpRequest.getConvertInputDmpBaseEntityListMaps();
        Map<String, List<DmpWdtWarehouseInventoryRecordEntity>> dmpInventoryEntityMap = new HashMap<>();
        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMap : convertInputDmpBaseEntityListMaps.entrySet()) {
            List<BaseEntity> value = convertInputDmpBaseEntityListMap.getValue();
            if (CollUtil.isNotEmpty(value)) {
                String storageName = convertInputDmpBaseEntityListMap.getKey().getStorageName();
                if ("dmp_wdt_warehouse_inventory_record".equals(storageName)) {
                    List<DmpWdtWarehouseInventoryRecordEntity> list = value.stream().map(e -> (DmpWdtWarehouseInventoryRecordEntity) e).collect(Collectors.toList());
                    dmpInventoryEntityMap.put(InventoryOrderTypeEnum.IN_STOCK.getCode(), list.stream().filter(e -> InventoryOrderTypeEnum.IN_STOCK.getCode().equals(e.getOrderType()) && InventoryBillStatusEnum.INIT.getCode().equals(e.getBillStatus())).collect(Collectors.toList()));
                    dmpInventoryEntityMap.put(InventoryOrderTypeEnum.OUT_STOCK.getCode(), list.stream().filter(e -> InventoryOrderTypeEnum.OUT_STOCK.getCode().equals(e.getOrderType()) && InventoryBillStatusEnum.INIT.getCode().equals(e.getBillStatus())).collect(Collectors.toList()));
                }
            }
        }
        Map<String, String> map = new HashMap<>();
        String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();
        for (String key : dmpInventoryEntityMap.keySet()) {
            List<DmpWdtWarehouseInventoryRecordEntity> detailList = dmpInventoryEntityMap.get(key);
            if (CollUtil.isEmpty(detailList)) {
                continue;
            }
            if (InventoryOrderTypeEnum.IN_STOCK.getCode().equals(key)) {
                Map<String, CreateOtherStockinRequest> result = this.convertInStock(cfgOutputId, detailList);
                if (!result.isEmpty()) {
                    for (Map.Entry<String, CreateOtherStockinRequest> r : result.entrySet()) {
                        map.put(r.getKey(), JSON.toJSONString(r.getValue()));
                    }
                }
            } else if (InventoryOrderTypeEnum.OUT_STOCK.getCode().equals(key)) {
                Map<String, CreateOtherStockoutRequest> result = this.convertOutStock(cfgOutputId, detailList);
                if (!result.isEmpty()) {
                    for (Map.Entry<String, CreateOtherStockoutRequest> r : result.entrySet()) {
                        map.put(r.getKey(), JSON.toJSONString(r.getValue()));
                    }
                }
            }


        }
        return map;
    }

    private Map<String, CreateOtherStockinRequest> convertInStock(String cfgOutputId, List<DmpWdtWarehouseInventoryRecordEntity> detailList) {
        Map<String, CreateOtherStockinRequest> result = new HashMap<>();
        if (CollUtil.isNotEmpty(detailList)) {

            //根据仓库ID分组
            Map<String, List<DmpWdtWarehouseInventoryRecordEntity>> groupByWarehouseId = detailList.stream().collect(Collectors.groupingBy(DmpWdtWarehouseInventoryRecordEntity::getThirdWarehouseCode));
            for (String thirdWarehouseCode : groupByWarehouseId.keySet()) {
                List<DmpWdtWarehouseInventoryRecordEntity> inventoryRecordEntityList = groupByWarehouseId.get(thirdWarehouseCode);
                if (validateDataBlack(inventoryRecordEntityList.get(0), cfgOutputId)) {
                    continue;
                }
                CreateOtherStockinRequest createOtherStockinRequest = DmpWdtConverter.INSTANCE.toCreateOtherStockinRequest(inventoryRecordEntityList.get(0), groupByWarehouseId.get(thirdWarehouseCode));
                result.put(InventoryOrderTypeEnum.IN_STOCK.getCode() + inventoryRecordEntityList.get(0).getBatchNo() + thirdWarehouseCode, createOtherStockinRequest);
            }
        }
        return result;
    }

    private Map<String, CreateOtherStockoutRequest> convertOutStock(String cfgOutputId, List<DmpWdtWarehouseInventoryRecordEntity> detailList) {
        Map<String, CreateOtherStockoutRequest> result = new HashMap<>();
        if (CollUtil.isNotEmpty(detailList)) {
            //根据仓库ID分组
            Map<String, List<DmpWdtWarehouseInventoryRecordEntity>> groupByWarehouseId = detailList.stream().collect(Collectors.groupingBy(DmpWdtWarehouseInventoryRecordEntity::getThirdWarehouseCode));
            for (String thirdWarehouseCode : groupByWarehouseId.keySet()) {
                List<DmpWdtWarehouseInventoryRecordEntity> inventoryRecordEntityList = groupByWarehouseId.get(thirdWarehouseCode);
                if (validateDataBlack(inventoryRecordEntityList.get(0), cfgOutputId)) {
                    continue;
                }
                CreateOtherStockoutRequest createOtherStockOutRequest = DmpWdtConverter.INSTANCE.toCreateOtherStockOutRequest(inventoryRecordEntityList.get(0), groupByWarehouseId.get(thirdWarehouseCode));
                result.put(InventoryOrderTypeEnum.OUT_STOCK.getCode() + inventoryRecordEntityList.get(0).getBatchNo() + thirdWarehouseCode, createOtherStockOutRequest);
            }
        }
        return result;
    }
    @Override
    protected List<String> getSourceCodeKeys() {
        return Arrays.asList("outerNo", "warehouseNo");
    }
}
