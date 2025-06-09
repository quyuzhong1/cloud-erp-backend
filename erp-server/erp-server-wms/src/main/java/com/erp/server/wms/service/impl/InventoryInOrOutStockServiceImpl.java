package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.StrUtils;
import com.common.core.utils.ValidatorUtil;
import com.erp.model.wms.dto.StocktakingProfitLossDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.inventory.*;
import com.erp.model.wms.entity.WarehouseLocationEntity;
import com.erp.model.wms.enums.inventory.*;
import com.erp.server.wms.annotation.InventoryHandler;
import com.erp.server.wms.service.WarehouseLocationService;
import com.erp.server.wms.service.WarehouseService;
import com.erp.server.wms.utils.InventoryUtils;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Classname: InventoryInOrOutStockServiceImpl
 * @Description: 出入库库存处理
 * @CreateTime: 2023-05-04  15:46
 * @Author: zhangchunlin
 */
@Slf4j
@Service
@InventoryHandler(InventoryBizTypeEnum.IN_OUT_STOCK)
public class InventoryInOrOutStockServiceImpl extends AbstractInventoryServiceImpl {

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private WarehouseLocationService warehouseLocationService;

    @Override
    public <T extends InventoryStockBaseDTO> void checkParam(List<T> paramList, InventoryBusinessTypeEnum businessType, List<TransactionRuleDTO> transactionRules) {
        Map<String, WarehouseDTO.UpdateDTO> warehouseMap = Maps.newHashMap();
        Map<String, WarehouseLocationEntity> warehouseLocationMap = Maps.newHashMap();
        // 判断是否需要忽略计算库存的sku
        List<String>  ignoreInventorySkuIds = this.getIgnoreSkuIds();
        for(InventoryStockBaseDTO baseParam : paramList) {
            if(baseParam instanceof InOutStockDTO) { // 出入库业务-走交易规则
                InOutStockDTO param = (InOutStockDTO) baseParam;
                ValidatorUtil.validateEntity(param);
                if(0 == param.getQty()) {
                    ServiceException.runError("库存变更数量不能等于0");
                }

                if(param.getQty() < 0 && !this.allowNegativeQtyBusinessList.contains(param.getSourceType())) {
                    ServiceException.runError("库存变更数量不能小于0");
                }

                if(ignoreInventorySkuIds.contains(param.getSkuId())) {
                    log.warn("sku id: {}，sku编号：{}产品属性是费用或服务，不参与库存出入库，不做库存验证", param.getSkuId(), param.getSkuNo());
                    continue;
                }

                WarehouseDTO.UpdateDTO warehouseDetail = warehouseMap.computeIfAbsent(param.getWarehouseId(), v -> warehouseService.detailWithCache(v));
                if (Objects.isNull(warehouseDetail) || CharSequenceUtil.isEmpty(warehouseDetail.getId())) {
                    ServiceException.runError(ApiError.ERROR_99002);
                }
                if (StrUtils.isNotEmpty(param.getWarehouseLocation())) {
                    WarehouseLocationEntity warehouseLocation = warehouseLocationMap.computeIfAbsent(param.getWarehouseLocation(), v -> warehouseLocationService.findByWarehouseIdAndCode(param.getWarehouseId(), v));
                    if (Objects.isNull(warehouseLocation) || CharSequenceUtil.isEmpty(warehouseLocation.getId())) {
                        ServiceException.runError("仓位信息不存在");
                    }
                }

                InventoryBaseInfoDTO inventoryBaseInfoDTO = new InventoryBaseInfoDTO();
                inventoryBaseInfoDTO.setSourceType(param.getSourceType());
                inventoryBaseInfoDTO.setBillDate(param.getBillDate());
                inventoryBaseInfoDTO.setSourceId(param.getSourceId());
                inventoryBaseInfoDTO.setWarehouseId(param.getWarehouseId());
                inventoryBaseInfoDTO.setSkuId(param.getSkuId());
                inventoryBaseInfoDTO.setSkuNo(param.getSkuNo());
                inventoryBaseInfoDTO.setQty(param.getQty());
                inventoryBaseInfoDTO.setWarehouseLocation(param.getWarehouseLocation());

                this.checkStockByRule(inventoryBaseInfoDTO, businessType, transactionRules);// 出库库存数量检查
            }
        }
    }

    /**
     * 循环处理出入库业务
     * @param paramList     业务参数
     * @param businessType  业务类型
     * @param transactionRuleParams 交易规则
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public <T extends InventoryStockBaseDTO> void stockHandler(List<T> paramList, InventoryBusinessTypeEnum businessType, List<TransactionRuleDTO> transactionRuleParams, String transactionNo) {
        // 获取忽略库存计算的sku
        List<String> ignoreInventorySkuIds = this.getIgnoreSkuIds();
        // 通过对sku id顺序执行, 避免多线程死锁
        Comparator<InventoryStockBaseDTO> comparing = Comparator.comparing(InventoryStockBaseDTO::getSkuId)
                .thenComparing(InventoryStockBaseDTO::getWarehouseId)
                .thenComparing(x -> CharSequenceUtil.isNotEmpty(x.getWarehouseLocation()) ? x.getWarehouseLocation() : "");
        paramList = paramList.stream().sorted(comparing).collect(Collectors.toList());
        for(InventoryStockBaseDTO baseParam : paramList) {
            InOutStockDTO param = (InOutStockDTO)baseParam;
            if(ignoreInventorySkuIds.contains(param.getSkuId())) {
                log.warn("sku id: {}，sku编号：{}产品属性是费用或服务，不参与库存出入库", param.getSkuId(), param.getSkuNo());
                continue;
            }
            this.singleHandler(param, businessType, transactionRuleParams, transactionNo);
        }


    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public <T extends InventoryStockBaseDTO> void singleHandler(T baseParam, InventoryBusinessTypeEnum businessType, List<TransactionRuleDTO> transactionRuleParams, String transactionNo) {
        InOutStockDTO param = (InOutStockDTO)baseParam;
        if(CollUtil.isEmpty(transactionRuleParams)) {
            ServiceException.runError(ApiError.ERROR_99034.code, CharSequenceUtil.format(ApiError.ERROR_99034.msg, businessType.getName()));
        }
        log.warn("从配置读取库存交易规则，业务类型：【{}】，单据类型：【{}】，单据id：【{}】，单据日期：【{}】,SKU编号：【{}】,交易配置信息：【{}】", businessType.getName(), param.getSourceType().getName(), param.getSourceId(), param.getBillDate(), param.getSkuNo(), JSONObject.toJSONString(transactionRuleParams));
        // 交易规则安装状态排序
        transactionRuleParams = transactionRuleParams.stream()
                .sorted(Comparator.comparing(x -> ObjectUtil.isNotEmpty(x.getInventoryStatus()) ? x.getInventoryStatus().getCode(): ""))
                .collect(Collectors.toList());
        for(TransactionRuleDTO transactionRule : transactionRuleParams) {
            InventoryUtils.checkTransRule(transactionRule);

            // 可能某个业务类型在同一个仓库即需要做入也需要做出，分别调用逻辑
            InOutStockCoreDTO inOutStockCoreDTO = InventoryUtils.wrapCoreParam(param, InventoryOperationModeEnum.APPROVE);
            InventoryModeEnum inventoryModeEnum = transactionRule.getTransactionMode();
            InventoryStatusEnum inventoryStatusEnum = transactionRule.getInventoryStatus();

            if(Objects.equals(InventoryModeEnum.IN_STOCK, inventoryModeEnum)) {
                // 入库
                this.inStockCore(inOutStockCoreDTO, businessType, inventoryStatusEnum, transactionRule.getId(), transactionNo);
            } else if (Objects.equals(InventoryModeEnum.OUT_STOCK, inventoryModeEnum)) {
                // 出库
                this.outStockCore(inOutStockCoreDTO, businessType, inventoryStatusEnum,  transactionRule.getId(), transactionNo);
            }
        }
    }

}