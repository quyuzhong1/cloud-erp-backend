package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.ValidatorUtil;
import com.erp.model.wms.dto.inventory.*;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.enums.inventory.*;
import com.erp.server.wms.config.InventoryHelper;
import com.erp.server.wms.service.InventoryStockService;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Classname: InventoryTransferServiceImpl
 * @Description: 调拨类业务处理
 * @CreateTime: 2023-05-04  15:49
 * @Author: zhangchunlin
 */
@Service
@Slf4j
public class InventoryTransferServiceImpl extends AbstractInventoryServiceImpl implements InventoryStockService {

    @Resource
    private InventoryHelper inventoryHelper;

    @Override
    public <T extends InventoryStockBaseDTO> void checkParam(List<T> paramList, InventoryBusinessTypeEnum businessType, List<TransactionRuleDTO> transactionRules) {
        inventoryHelper.checkTransferStockParam(paramList, businessType, transactionRules);
    }

    /**
     * 循环处理调拨业务
     * @param paramLis
     * @param businessType
     * @param transactionRuleParams
     * @param transactionNo
     */
    @Override
    public <T extends InventoryStockBaseDTO> void stockHandler(List<T> paramLis, InventoryBusinessTypeEnum businessType, List<TransactionRuleDTO> transactionRuleParams, String transactionNo) {
        Map<String, WarehouseEntity> warehouseMap = Maps.newHashMap();// TODO 仓库集合，后续改成从redis缓存中读取
        for(InventoryStockBaseDTO baseParam : paramLis) {
            // 当前仓出入库业务处理
            TransferDTO param = (TransferDTO)baseParam;
            InStockOrOutStockTransformDTO curWareInOrOutStock = inventoryHelper.wrapInOutStockByTransfer(param, InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT);
            this.singleHandler(curWareInOrOutStock, businessType, transactionRuleParams, warehouseMap, transactionNo);
            // 目的仓出入库业务处理
            InStockOrOutStockTransformDTO targetWareInOrOutStock = inventoryHelper.wrapInOutStockByTransfer(param, InventoryWarehouseOptionEnum.WAREHOUSE_TARGET);
            this.singleHandler(targetWareInOrOutStock, businessType, transactionRuleParams, warehouseMap, transactionNo);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public <T extends InventoryStockBaseDTO> void singleHandler(T baseParam, InventoryBusinessTypeEnum businessType, List<TransactionRuleDTO> transactionRuleParams, Map<String, WarehouseEntity> warehouseMap, String transactionNo) {
        InStockOrOutStockTransformDTO param = (InStockOrOutStockTransformDTO)baseParam;
        // 状态
        if(Objects.nonNull(param.getInventoryStatus())) { // 参数传输了要改的状态
            log.info("参数已传库存状态：【{}】，业务类型：【{}】，单据类型：【{}】，单据id：【{}】，单据日期：【{}】,SKU编号：【{}】", param.getInventoryStatus().getName(), businessType.getName(), param.getSourceType().getName(), param.getSourceId(), param.getBillDate(), param.getSkuNo());
            InventoryModeEnum inventoryModeEnum = param.getInventoryMode();
            ValidatorUtil.isTrue(Objects.nonNull(inventoryModeEnum),()->new ServiceException(ApiError.ERROR_400.code, "交易类型不能为空"));
            if(Objects.equals(InventoryModeEnum.IN_STOCK, inventoryModeEnum)) { //入库
                this.inStockCore(param, businessType, param.getInventoryStatus(), "", warehouseMap, transactionNo);
            } else if (Objects.equals(InventoryModeEnum.OUT_STOCK, inventoryModeEnum)) { // 出库
                this.outStockCore(param, businessType, param.getInventoryStatus(), "", warehouseMap, transactionNo);
            }
        } else {
            if(CollUtil.isEmpty(transactionRuleParams)) {
                throw new ServiceException(ApiError.ERROR_99034.code, StrUtil.format(ApiError.ERROR_99034.msg, businessType.getName()));
            }
            log.info("参数未传库存状态，从配置读取，业务类型：【{}】，单据类型：【{}】，单据id：【{}】，单据日期：【{}】,SKU编号：【{}】,交易配置信息：【{}】", businessType.getName(), param.getSourceType().getName(), param.getSourceId(), param.getBillDate(), param.getSkuNo(), JSONObject.toJSONString(transactionRuleParams));
            // 判断当前仓是入库还是出库
            transactionRuleParams = transactionRuleParams.stream().filter(r->Objects.equals(r.getWarehouseOption(), param.getWarehouseOptionEnum())).collect(Collectors.toList());
            if(CollUtil.isEmpty(transactionRuleParams)) {
                throw new ServiceException(ApiError.ERROR_99034.code, StrUtil.format(ApiError.ERROR_99034.msg, businessType.getName()));
            }
            for(TransactionRuleDTO transactionRule : transactionRuleParams) {
                InventoryWarehouseOptionEnum inventoryWarehouseOptionEnum = transactionRule.getWarehouseOption();
                ValidatorUtil.isTrue(Objects.nonNull(inventoryWarehouseOptionEnum), () -> new ServiceException(ApiError.ERROR_99033));
                InventoryStatusEnum inventoryStatusEnum = transactionRule.getInventoryStatus();
                ValidatorUtil.isTrue(Objects.nonNull(inventoryStatusEnum), () -> new ServiceException(ApiError.ERROR_99036));
                InventoryModeEnum inventoryModeEnum = transactionRule.getTransactionMode();
                ValidatorUtil.isTrue(Objects.nonNull(inventoryModeEnum), () -> new ServiceException(ApiError.ERROR_99038));
                // 可能某个业务类型在同一个仓库即需要做入也需要做出，分别调用逻辑
                if(Objects.equals(InventoryModeEnum.IN_STOCK, inventoryModeEnum)) { // 入库
                    this.inStockCore(param, businessType, inventoryStatusEnum, transactionRule.getId(), warehouseMap, transactionNo);
                } else if (Objects.equals(InventoryModeEnum.OUT_STOCK, inventoryModeEnum)) { // 出库
                    this.outStockCore(param, businessType, inventoryStatusEnum,  transactionRule.getId(), warehouseMap, transactionNo);
                }
            }
        }
    }

    /**
     * 调拨类
     * @return
     */
    @Override
    public InventoryBizTypeEnum handlerType() {
        return InventoryBizTypeEnum.TRANSFER_STOCK;
    }
}