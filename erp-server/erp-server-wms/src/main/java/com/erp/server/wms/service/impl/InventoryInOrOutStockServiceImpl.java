package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.ValidatorUtil;
import com.erp.model.wms.dto.inventory.InStockOrOutStockDTO;
import com.erp.model.wms.dto.inventory.InventoryStockBaseDTO;
import com.erp.model.wms.dto.inventory.TransactionRuleDTO;
import com.erp.model.wms.enums.inventory.*;
import com.erp.server.wms.config.InventoryHelper;
import com.erp.server.wms.service.InventoryStockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
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

    @Resource
    private InventoryHelper inventoryHelper;

    @Override
    public <T extends InventoryStockBaseDTO> void checkParam(List<T> paramList, InventoryBusinessTypeEnum businessType, List<TransactionRuleDTO> transactionRules) {
        inventoryHelper.checkInOutStockParam(paramList, businessType, transactionRules);
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
            InStockOrOutStockDTO param = (InStockOrOutStockDTO)baseParam;
            this.singleHandler(param, businessType, transactionRuleParams, transactionNo);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public <T extends InventoryStockBaseDTO> void singleHandler(T baseParam, InventoryBusinessTypeEnum businessType, List<TransactionRuleDTO> transactionRuleParams, String transactionNo) {
        InStockOrOutStockDTO param = (InStockOrOutStockDTO)baseParam;
        // 状态
        if(Objects.nonNull(param.getInventoryStatus())) { // 参数传输了要改的状态
            log.info("参数已传库存状态：【{}】，业务类型：【{}】，单据类型：【{}】，单据id：【{}】，单据日期：【{}】,SKU编号：【{}】", param.getInventoryStatus().getName(), businessType.getName(), param.getSourceType().getName(), param.getSourceId(), param.getBillDate(), param.getSkuNo());
            InventoryModeEnum inventoryModeEnum = param.getInventoryMode();
            ValidatorUtil.isTrue(Objects.nonNull(inventoryModeEnum),()->new ServiceException(ApiError.ERROR_400.code, "交易类型不能为空"));
            if(Objects.equals(InventoryModeEnum.IN_STOCK, inventoryModeEnum)) { //入库
                this.inStockCore(param, businessType, param.getInventoryStatus(), "", transactionNo);
            } else if (Objects.equals(InventoryModeEnum.OUT_STOCK, inventoryModeEnum)) { // 出库
                this.outStockCore(param, businessType, param.getInventoryStatus(), "", transactionNo);
            }
        } else {
            if(CollUtil.isEmpty(transactionRuleParams)) {
                throw new ServiceException(ApiError.ERROR_99034.code, StrUtil.format(ApiError.ERROR_99034.msg, businessType.getName()));
            }
            log.info("参数未传库存状态，从配置读取，业务类型：【{}】，单据类型：【{}】，单据id：【{}】，单据日期：【{}】,SKU编号：【{}】,交易配置信息：【{}】", businessType.getName(), param.getSourceType().getName(), param.getSourceId(), param.getBillDate(), param.getSkuNo(), JSONObject.toJSONString(transactionRuleParams));
            for(TransactionRuleDTO transactionRule : transactionRuleParams) {
                InventoryWarehouseOptionEnum inventoryWarehouseOptionEnum = transactionRule.getWarehouseOption();
                ValidatorUtil.isTrue(Objects.nonNull(inventoryWarehouseOptionEnum), () -> new ServiceException(ApiError.ERROR_99033));
                InventoryStatusEnum inventoryStatusEnum = transactionRule.getInventoryStatus();
                ValidatorUtil.isTrue(Objects.nonNull(inventoryStatusEnum), () -> new ServiceException(ApiError.ERROR_99036));
                InventoryModeEnum inventoryModeEnum = transactionRule.getTransactionMode();
                ValidatorUtil.isTrue(Objects.nonNull(inventoryModeEnum), () -> new ServiceException(ApiError.ERROR_99038));
                // 可能某个业务类型在同一个仓库即需要做入也需要做出，分别调用逻辑
                if(Objects.equals(InventoryModeEnum.IN_STOCK, inventoryModeEnum)) { // 入库
                    this.inStockCore(param, businessType, inventoryStatusEnum, transactionRule.getId(), transactionNo);
                } else if (Objects.equals(InventoryModeEnum.OUT_STOCK, inventoryModeEnum)) { // 出库
                    this.outStockCore(param, businessType, inventoryStatusEnum,  transactionRule.getId(), transactionNo);
                }
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