package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.StrUtils;
import com.common.core.utils.ValidatorUtil;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.inventory.*;
import com.erp.model.wms.entity.WarehouseLocationEntity;
import com.erp.model.wms.enums.inventory.*;
import com.erp.server.wms.service.InventoryStockService;
import com.erp.server.wms.service.WarehouseLocationService;
import com.erp.server.wms.service.WarehouseService;
import com.erp.server.wms.utils.InventoryUtils;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @Classname: InventoryInOrOutStockServiceImpl
 * @Description: 出入库库存处理
 * @CreateTime: 2023-05-04  15:46
 * @Author: zhangchunlin
 */
@Slf4j
@Service
public class InventoryInOrOutStockServiceImpl extends AbstractInventoryServiceImpl implements InventoryStockService {

    @Autowired
    private WarehouseService warehouseService;

    @Autowired
    private WarehouseLocationService warehouseLocationService;

    @Override
    public <T extends InventoryStockBaseDTO> void checkParam(List<T> paramList, InventoryBusinessTypeEnum businessType, List<TransactionRuleDTO> transactionRules) {
        Map<String, WarehouseDTO.UpdateDTO> warehouseMap = Maps.newHashMap();
        Map<String, WarehouseLocationEntity> warehouseLocationMap = Maps.newHashMap();
        for(InventoryStockBaseDTO baseParam : paramList) {
            if(baseParam instanceof InOutStockDTO) { // 出入库业务-走交易规则
                InOutStockDTO param = (InOutStockDTO) baseParam;
                ValidatorUtil.validateEntity(param);

                WarehouseDTO.UpdateDTO warehouseDetail = warehouseMap.computeIfAbsent(param.getWarehouseId(), (v) -> warehouseService.detailWithCache(v));
                if (Objects.isNull(warehouseDetail) || StrUtil.isEmpty(warehouseDetail.getId())) {
                    throw new ServiceException(ApiError.ERROR_99002);
                }
                if (StrUtils.isNotEmpty(param.getWarehouseLocation())) {
                    WarehouseLocationEntity warehouseLocation = warehouseLocationMap.computeIfAbsent(param.getWarehouseLocation(), (v) -> warehouseLocationService.findByWarehouseIdAndCode(param.getWarehouseId(), v));
                    if (Objects.isNull(warehouseLocation) || StrUtil.isEmpty(warehouseLocation.getId())) {
                        throw new ServiceException("仓位信息不存在");
                    }
                }

                inventoryHelper.checkCommonBiz(param.getSourceType(), param.getSourceId(), param.getBillDate());// 通用检查
                inventoryHelper.checkAllowTrade(param.getSourceType(), param.getWarehouseId(), param.getSkuNo());// 关账检查

                InventoryBaseInfoDTO inventoryBaseInfoDTO = new InventoryBaseInfoDTO();
                inventoryBaseInfoDTO.setSourceType(param.getSourceType());
                inventoryBaseInfoDTO.setBillDate(param.getBillDate());
                inventoryBaseInfoDTO.setSourceId(param.getSourceId());
                inventoryBaseInfoDTO.setWarehouseId(param.getWarehouseId());
                inventoryBaseInfoDTO.setSkuId(param.getSkuId());
                inventoryBaseInfoDTO.setSkuNo(param.getSkuNo());
                inventoryBaseInfoDTO.setQty(param.getQty());
                inventoryBaseInfoDTO.setWarehouseLocation(param.getWarehouseLocation());

                inventoryHelper.checkStockByRule(inventoryBaseInfoDTO, businessType, transactionRules);// 出库库存数量检查
            }
        }
    }

    /**
     * 循环处理出入库业务
     * @param paramLis
     * @param businessType
     * @param transactionRuleParams
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public <T extends InventoryStockBaseDTO> void stockHandler(List<T> paramLis, InventoryBusinessTypeEnum businessType, List<TransactionRuleDTO> transactionRuleParams, String transactionNo) {
        for(InventoryStockBaseDTO baseParam : paramLis) {
            InOutStockDTO param = (InOutStockDTO)baseParam;
            if(Objects.nonNull(param.getWarehouseLocation())) {
                param.setWarehouseLocation(StrUtils.null2EmptyWithTrim(param.getWarehouseLocation()));
            }
            this.singleHandler(param, businessType, transactionRuleParams, transactionNo);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public <T extends InventoryStockBaseDTO> void singleHandler(T baseParam, InventoryBusinessTypeEnum businessType, List<TransactionRuleDTO> transactionRuleParams, String transactionNo) {
        InOutStockDTO param = (InOutStockDTO)baseParam;
        if(CollUtil.isEmpty(transactionRuleParams)) {
            throw new ServiceException(ApiError.ERROR_99034.code, StrUtil.format(ApiError.ERROR_99034.msg, businessType.getName()));
        }
        log.info("从配置读取库存交易规则，业务类型：【{}】，单据类型：【{}】，单据id：【{}】，单据日期：【{}】,SKU编号：【{}】,交易配置信息：【{}】", businessType.getName(), param.getSourceType().getName(), param.getSourceId(), param.getBillDate(), param.getSkuNo(), JSONObject.toJSONString(transactionRuleParams));
        for(TransactionRuleDTO transactionRule : transactionRuleParams) {
            InventoryUtils.checkTransRule(transactionRule);
            // 可能某个业务类型在同一个仓库即需要做入也需要做出，分别调用逻辑
            InOutStockCoreDTO inOutStockCoreDTO = InventoryUtils.wrapCoreParam(param, InventoryOperationModeEnum.APPROVE);
            InventoryModeEnum inventoryModeEnum = transactionRule.getTransactionMode();
            InventoryStatusEnum inventoryStatusEnum = transactionRule.getInventoryStatus();
            if(Objects.equals(InventoryModeEnum.IN_STOCK, inventoryModeEnum)) { // 入库
                this.inStockCore(inOutStockCoreDTO, businessType, inventoryStatusEnum, transactionRule.getId(), transactionNo);
            } else if (Objects.equals(InventoryModeEnum.OUT_STOCK, inventoryModeEnum)) { // 出库
                this.outStockCore(inOutStockCoreDTO, businessType, inventoryStatusEnum,  transactionRule.getId(), transactionNo);
            }
        }
    }

    /**
     * 出入库
     * @return
     */
    @Override
    public InventoryBizTypeEnum handlerType() {
        return InventoryBizTypeEnum.IN_OUT_STOCK;
    }
}