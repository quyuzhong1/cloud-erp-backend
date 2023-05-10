package com.erp.server.wms.utils;

import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ValidatorUtil;
import com.erp.model.wms.dto.inventory.*;
import com.erp.model.wms.enums.inventory.*;

import java.time.LocalDate;
import java.util.Objects;

/**
 * @Classname: InventoryUtils
 * @Description: TODO
 * @CreateTime: 2023-05-10  14:17
 * @Author: zhangchunlin
 */
public class InventoryUtils {

    /**
     * 填充出入库核心实体属性
     * @param param
     * @return
     */
    public static InOutStockCoreDTO wrapCoreParam(InOutStockDTO param, InventoryOperationModeEnum operationMode) {
        InOutStockCoreDTO inOutStockCoreDTO = BeanMapperUtils.map(InOutStockCoreDTO.class, param);
        inOutStockCoreDTO.setOperationMode(operationMode);
        return inOutStockCoreDTO;
    }

    /**
     * 验证库存交易规则参数
     * @param transactionRule
     */
    public static void checkTransRule(TransactionRuleDTO transactionRule) {
        InventoryWarehouseOptionEnum inventoryWarehouseOptionEnum = transactionRule.getWarehouseOption();
        ValidatorUtil.isTrue(Objects.nonNull(inventoryWarehouseOptionEnum), () -> new ServiceException(ApiError.ERROR_99033));
        InventoryStatusEnum inventoryStatusEnum = transactionRule.getInventoryStatus();
        ValidatorUtil.isTrue(Objects.nonNull(inventoryStatusEnum), () -> new ServiceException(ApiError.ERROR_99036));
        InventoryModeEnum inventoryModeEnum = transactionRule.getTransactionMode();
        ValidatorUtil.isTrue(Objects.nonNull(inventoryModeEnum), () -> new ServiceException(ApiError.ERROR_99038));
    }

    /**
     * 调拨参数转换成出入库参数
     * @param param
     * @param warehouseOption
     * @return
     */
    public InOutStockTransformDTO wrapInOutStockByTransfer(TransferDTO param, InventoryWarehouseOptionEnum warehouseOption) {
        InOutStockTransformDTO inOutStockParam = new InOutStockTransformDTO();

        if(Objects.equals(warehouseOption, InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT)) {
            inOutStockParam.setOrgId(param.getCurOrgId());
            inOutStockParam.setWarehouseId(param.getCurWarehouseId());
            inOutStockParam.setWarehouseLocation(param.getCurWarehouseLocation());
        } else if (Objects.equals(warehouseOption, InventoryWarehouseOptionEnum.WAREHOUSE_TARGET)) {
            inOutStockParam.setOrgId(param.getTargetOrgId());
            inOutStockParam.setWarehouseId(param.getTargetWarehouseId());
            inOutStockParam.setWarehouseLocation(param.getTargetWarehouseLocation());
        }
        inOutStockParam.setSourceType(param.getSourceType());
        inOutStockParam.setSourceId(param.getSourceId());
        inOutStockParam.setSourceDetailId(param.getSourceDetailId());
        inOutStockParam.setSourceCode(param.getSourceCode());
        inOutStockParam.setBillDate(param.getBillDate());
        inOutStockParam.setSkuId(param.getSkuId());
        inOutStockParam.setSkuNo(param.getSkuNo());

        inOutStockParam.setQty(param.getQty());
        inOutStockParam.setWarehouseOptionEnum(warehouseOption);
        return inOutStockParam;
    }

    /**
     * 调拨参数转换成出入库参数（没有库存状态和交易方向）
     * @param param
     * @param warehouseOption
     * @return
     */
    public static InOutStockTransformDTO wrapInOutStockByTransfer(TransferDTO param, InventoryWarehouseOptionEnum warehouseOption, InventoryOperationModeEnum operationMode) {
        InOutStockTransformDTO inOutStockParam = new InOutStockTransformDTO();

        if(Objects.equals(warehouseOption, InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT)) {
            inOutStockParam.setOrgId(param.getCurOrgId());
            inOutStockParam.setWarehouseId(param.getCurWarehouseId());
            inOutStockParam.setWarehouseLocation(param.getCurWarehouseLocation());
        } else if (Objects.equals(warehouseOption, InventoryWarehouseOptionEnum.WAREHOUSE_TARGET)) {
            inOutStockParam.setOrgId(param.getTargetOrgId());
            inOutStockParam.setWarehouseId(param.getTargetWarehouseId());
            inOutStockParam.setWarehouseLocation(param.getTargetWarehouseLocation());
        }
        inOutStockParam.setSourceType(param.getSourceType());
        inOutStockParam.setSourceId(param.getSourceId());
        inOutStockParam.setSourceDetailId(param.getSourceDetailId());
        inOutStockParam.setSourceCode(param.getSourceCode());
        inOutStockParam.setBillDate(param.getBillDate());
        inOutStockParam.setSkuId(param.getSkuId());
        inOutStockParam.setSkuNo(param.getSkuNo());

        inOutStockParam.setQty(param.getQty());
        inOutStockParam.setOperationMode(operationMode);
        inOutStockParam.setWarehouseOptionEnum(warehouseOption);
        return inOutStockParam;
    }

    /**
     * 填充出入库核心实体属性（调拨）
     * @param param
     * @return
     */
    public static InOutStockCoreDTO wrapCoreParamByTransfer(InOutStockTransformDTO param, InventoryOperationModeEnum operationMode) {
        InOutStockCoreDTO inOutStockCoreDTO = BeanMapperUtils.map(InOutStockCoreDTO.class, param);
        inOutStockCoreDTO.setOperationMode(operationMode);
        return inOutStockCoreDTO;
    }

    /**
     * 出入库填充流水
     * @param param
     * @param inventoryId
     * @param businessType
     * @param inventoryDetailId
     * @param inventoryStatusEnum
     * @param instockBatchDate
     * @return
     */
    public static TransactionFlowDTO wrapTransactionFlowInOutStock(InOutStockCoreDTO param, String inventoryId, InventoryBusinessTypeEnum businessType,
                                                            String inventoryDetailId, InventoryStatusEnum inventoryStatusEnum, LocalDate instockBatchDate,
                                                            Integer qty) {
        TransactionFlowDTO transactionFlowDTO = new TransactionFlowDTO();
        // 复制对象性能慢，改为手工赋值
        transactionFlowDTO.setOrgId(param.getOrgId());
        transactionFlowDTO.setWarehouseId(param.getWarehouseId());
        transactionFlowDTO.setWarehouseLocation(param.getWarehouseLocation());
        transactionFlowDTO.setSkuId(param.getSkuId());
        transactionFlowDTO.setSkuNo(param.getSkuNo());
        transactionFlowDTO.setSourceId(param.getSourceId());
        transactionFlowDTO.setSourceCode(param.getSourceCode());
        transactionFlowDTO.setSourceDetailId(param.getSourceDetailId());

        transactionFlowDTO.setInventoryId(inventoryId);
        transactionFlowDTO.setInventoryDetailId(inventoryDetailId);
        transactionFlowDTO.setDictInventoryStatus(inventoryStatusEnum.getCode());
        transactionFlowDTO.setDictBizType(businessType.getCode());
        transactionFlowDTO.setInstockBatchDate(instockBatchDate);// 取库存明细上面的批次日期
        transactionFlowDTO.setBillDate(param.getBillDate());
        transactionFlowDTO.setSourceType(param.getSourceType().getCode());
        transactionFlowDTO.setQty(qty);
        transactionFlowDTO.setOperationMode(Objects.nonNull(param.getOperationMode()) ? param.getOperationMode().getCode() : "");
        return transactionFlowDTO;
    }

}