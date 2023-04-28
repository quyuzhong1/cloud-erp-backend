package com.erp.server.wms.config;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.ValidatorUtil;
import com.erp.model.wms.dto.inventory.*;
import com.erp.model.wms.entity.CfgTransactionRulesEntity;
import com.erp.model.wms.entity.InventoryEntity;
import com.erp.model.wms.enums.SourceTypeEnum;
import com.erp.model.wms.enums.inventory.InventoryBusinessTypeEnum;
import com.erp.model.wms.enums.inventory.InventoryModeEnum;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.model.wms.enums.inventory.InventoryWarehouseOptionEnum;
import com.erp.server.wms.service.CfgTransactionRulesService;
import com.erp.server.wms.service.InventoryService;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Classname: InventoryHelper
 * @Description: 库存辅助配置类
 * @CreateTime: 2023-04-26  16:31
 * @Author: zhangchunlin
 */
@Slf4j
@Component
public class InventoryHelper {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private CfgTransactionRulesService cfgTransactionRulesService;

    /**
     * 通用业务验证
     * @param sourceTypeEnum
     * @param sourceId
     * @param billDate
     */
    public void checkCommonBiz(SourceTypeEnum sourceTypeEnum, String sourceId, LocalDate billDate) {
        log.info("开始检查是否关闭账套，单据类型：【{}】，单据id：【{}】，单据日期：【{}】", sourceTypeEnum.getName(), sourceId, billDate);
        // TODO 1.检查是否关账
        log.info("通过检查是否关闭账套，单据类型：【{}】，单据id：【{}】，单据日期：【{}】", sourceTypeEnum.getName(), sourceId, billDate);
    }

    /**
     * 业务验证（盘点中，可能精确到SKU级别）
     * @param sourceTypeEnum
     * @param warehouseId
     * @param skuNo
     */
    public void checkAllowTrade(SourceTypeEnum sourceTypeEnum, String warehouseId, String skuNo) {
        // TODO 检查是否盘点中
        log.info("开始检查是否盘点中，仓库：【{}】，单据类型：【{}】，SKU：【{}】", warehouseId, sourceTypeEnum.getName(), skuNo);
        log.info("通过检查是否盘点中，仓库：【{}】，单据类型：【{}】，SKU：【{}】", warehouseId, sourceTypeEnum.getName(), skuNo);
    }

    /**
     * 出库检查库存是否足够
     */
    public void checkEnoughStockIfNecessary(InventoryBaseInfoDTO param, InventoryBusinessTypeEnum businessType, List<TransactionRuleDTO> transactionRules) {
        SourceTypeEnum sourceTypeEnum = param.getSourceType();
        String sourceId = param.getSourceId();
        LocalDate billDate = param.getBillDate();
        // 状态
        InventoryStatusEnum inventoryStatusEnum = param.getInventoryStatus();
        List<TransactionRuleDTO> outTransactionRules = null;
        if(Objects.isNull(inventoryStatusEnum)) { // 从配置中取，配置中也取不到则报错
            log.info("参数未传库存状态，从配置中取，业务类型：【{}】，单据类型：【{}】，单据id：【{}】，单据日期：【{}】，SKU编号：【{}】", businessType.getName(), sourceTypeEnum.getName(), sourceId, billDate, param.getSkuNo());
            if(CollUtil.isEmpty(transactionRules)) {
                throw new ServiceException(ApiError.ERROR_99034.code, StrUtil.format(ApiError.ERROR_99034.msg, businessType.getName()));
            }
            if(Objects.nonNull(param.getWarehouseOption())) { // 调拨类业务，包含当前仓和目的仓
                outTransactionRules = transactionRules.stream().filter(r->Objects.equals(r.getTransactionMode(), InventoryModeEnum.OUT_STOCK)
                        && Objects.equals(r.getWarehouseOption(), param.getWarehouseOption().getCode())).collect(Collectors.toList());
            } else {
                // 直接过滤得到出库类型的数据
                outTransactionRules = transactionRules.stream().filter(r->Objects.equals(r.getTransactionMode(), InventoryModeEnum.OUT_STOCK)).collect(Collectors.toList());
            }
            if(CollUtil.isNotEmpty(outTransactionRules)) {
                for (TransactionRuleDTO rule : outTransactionRules) {
                    InventoryStatusEnum ruleInventoryStatusEnum = rule.getInventoryStatus();
                    ValidatorUtil.isTrue(Objects.nonNull(ruleInventoryStatusEnum),()->new ServiceException(ApiError.ERROR_99036));
                    this.checkStockQtyByWareLocalSkuStatus(businessType, param, ruleInventoryStatusEnum);
                }
            }
        } else {
            log.info("参数已传库存状态：【{}】，业务类型：【{}】，单据类型：【{}】，单据id：【{}】，单据日期：【{}】，SKU编号：【{}】", inventoryStatusEnum.getName(), businessType.getName(), sourceTypeEnum.getName(), sourceId, billDate, param.getSkuNo());
            InventoryModeEnum inventoryModeEnum = param.getInventoryMode();
            ValidatorUtil.isTrue(Objects.nonNull(inventoryModeEnum),()->new ServiceException(ApiError.ERROR_400.code, "交易类型不能为空"));
            if(Objects.equals(InventoryModeEnum.OUT_STOCK, inventoryModeEnum)) { // 交易方向为-的检查
                this.checkStockQtyByWareLocalSkuStatus(businessType, param, inventoryStatusEnum);
            }
        }
    }

    /**
     * 检查库存是否足够
     * @param businessType
     * @param param
     * @param status
     */
    public void checkStockQtyByWareLocalSkuStatus(InventoryBusinessTypeEnum businessType, InventoryBaseInfoDTO param, InventoryStatusEnum status) {
        // 仓库组织
        String orgId = param.getOrgId();
        // 仓库
        String warehouseId = param.getWarehouseId();
        // SKU
        String skuId = param.getSkuId();
        String skuNo = param.getSkuNo();
        // 库位
        String warehouseLocationId = param.getWarehouseLocation();
        // 数量
        Integer qty = param.getQty();
        // 来源
        SourceTypeEnum sourceTypeEnum = param.getSourceType();
        InventoryEntity inventory = inventoryService.findInventoryByWareLocalSkuStatus(orgId, warehouseId,skuId,warehouseLocationId,status.getCode());
        ValidatorUtil.isTrue(Objects.nonNull(inventory),()->new ServiceException(ApiError.ERROR_99035));
        Integer inventoryQty = inventory.getQty();
        log.info("仓库【{}】，组织：【{}】，库位：【{}】，SKU：【{}】，SKU编号：【{}】, 来源单据：【{}】, 业务类型：【{}】，状态【{}】，操作数量：【{}】，库存状态对应的总数量：【{}】", warehouseId, orgId, warehouseLocationId,skuId, skuNo, sourceTypeEnum.getName(),
                businessType.getName(), status.getName(), qty, inventoryQty);
        ValidatorUtil.isTrue(inventoryQty >= qty,()->new ServiceException(ApiError.ERROR_99035));
    }

    /**
     *
     * @param businessType
     * @return
     */
    public List<TransactionRuleDTO> wrapTransactionRule(InventoryBusinessTypeEnum businessType) {
        // 查询配置的交易规则
        List<CfgTransactionRulesEntity> transactionRulesEntities = cfgTransactionRulesService.findByDictBizType(businessType.getCode());
        if(CollUtil.isNotEmpty(transactionRulesEntities)) {
            List<TransactionRuleDTO> transactionRuleDTOS = Lists.newArrayListWithExpectedSize(transactionRulesEntities.size());
            transactionRulesEntities.stream().forEach(r->{
                TransactionRuleDTO transactionRuleDTO = new TransactionRuleDTO();
                transactionRuleDTO.setId(r.getId());
                transactionRuleDTO.setDictBizType(InventoryBusinessTypeEnum.of(r.getDictBizType()));
                transactionRuleDTO.setWarehouseOption(InventoryWarehouseOptionEnum.of(r.getWarehouseOption()));
                transactionRuleDTO.setInventoryStatus(InventoryStatusEnum.of(r.getInventoryStatus()));
                transactionRuleDTO.setTransactionMode(InventoryModeEnum.of(r.getTransactionMode()));
                transactionRuleDTOS.add(transactionRuleDTO);
            });
            return transactionRuleDTOS;
        }
        return null;
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
    public TransactionFlowDTO wrapTransactionFlowInOutStock(InStockOrOutStockDTO param, String inventoryId,InventoryBusinessTypeEnum businessType,
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

    /**
     * 出入库业务验证参数
     * @param paramLis
     */
    public void checkInOutStockParam(List<InStockOrOutStockDTO> paramLis, InventoryBusinessTypeEnum businessType, List<TransactionRuleDTO> transactionRules) {
        for(InStockOrOutStockDTO param : paramLis) {
            ValidatorUtil.validateEntity(param);
            this.checkCommonBiz(param.getSourceType(), param.getSourceId(), param.getBillDate());// 通用检查
            this.checkAllowTrade(param.getSourceType(), param.getWarehouseId(), param.getSkuNo());// 关账检查

            InventoryBaseInfoDTO inventoryBaseInfoDTO = new InventoryBaseInfoDTO();
            inventoryBaseInfoDTO.setSourceType(param.getSourceType());
            inventoryBaseInfoDTO.setBillDate(param.getBillDate());
            inventoryBaseInfoDTO.setInventoryMode(param.getInventoryMode());
            inventoryBaseInfoDTO.setSourceId(param.getSourceId());
            inventoryBaseInfoDTO.setInventoryStatus(param.getInventoryStatus());
            inventoryBaseInfoDTO.setWarehouseId(param.getWarehouseId());
            inventoryBaseInfoDTO.setOrgId(param.getOrgId());
            inventoryBaseInfoDTO.setSkuId(param.getSkuId());
            inventoryBaseInfoDTO.setSkuNo(param.getSkuNo());
            inventoryBaseInfoDTO.setQty(param.getQty());
            inventoryBaseInfoDTO.setWarehouseLocation(param.getWarehouseLocation());

            this.checkEnoughStockIfNecessary(inventoryBaseInfoDTO, businessType, transactionRules);// 出库库存数量检查
        }
    }

    /**
     * 调拨业务验证参数
     * @param paramLis
     */
    public void checkTransferStockParam(List<TransferDTO> paramLis, InventoryBusinessTypeEnum businessType, List<TransactionRuleDTO> transactionRules) {
        for(TransferDTO param : paramLis) {
            ValidatorUtil.validateEntity(param);

            //当前仓和目的仓不能一样
            ValidatorUtil.isTrue(!Objects.equals(param.getCurWarehouseId(), param.getTargetWarehouseId()),()->new ServiceException(ApiError.ERROR_99039));

            this.checkCommonBiz(param.getSourceType(), param.getSourceId(), param.getBillDate());// 通用检查
            this.checkAllowTrade(param.getSourceType(), param.getCurWarehouseId(), param.getSkuNo());// 当前仓关账检查
            this.checkAllowTrade(param.getSourceType(), param.getTargetWarehouseId(), param.getSkuNo());// 目的仓关账检查

            InventoryBaseInfoDTO currInventoryBaseInfoDTO = new InventoryBaseInfoDTO();
            currInventoryBaseInfoDTO.setSourceType(param.getSourceType());
            currInventoryBaseInfoDTO.setBillDate(param.getBillDate());
            currInventoryBaseInfoDTO.setInventoryMode(param.getCurInventoryMode());
            currInventoryBaseInfoDTO.setSourceId(param.getSourceId());
            currInventoryBaseInfoDTO.setInventoryStatus(param.getCurInventoryStatus());
            currInventoryBaseInfoDTO.setWarehouseId(param.getCurWarehouseId());
            currInventoryBaseInfoDTO.setWarehouseLocation(param.getCurWarehouseLocation());
            currInventoryBaseInfoDTO.setOrgId(param.getCurOrgId());
            currInventoryBaseInfoDTO.setSkuId(param.getSkuId());
            currInventoryBaseInfoDTO.setSkuNo(param.getSkuNo());
            currInventoryBaseInfoDTO.setQty(param.getQty());
            currInventoryBaseInfoDTO.setWarehouseOption(InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT);//当前仓

            this.checkEnoughStockIfNecessary(currInventoryBaseInfoDTO, businessType, transactionRules);// 当前仓出库库存数量检查（因为有可能是当前仓入库）

            InventoryBaseInfoDTO targetInventoryBaseInfoDTO = new InventoryBaseInfoDTO();
            targetInventoryBaseInfoDTO.setSourceType(param.getSourceType());
            targetInventoryBaseInfoDTO.setBillDate(param.getBillDate());
            targetInventoryBaseInfoDTO.setInventoryMode(param.getTargetInventoryMode());
            targetInventoryBaseInfoDTO.setSourceId(param.getSourceId());
            targetInventoryBaseInfoDTO.setInventoryStatus(param.getTargetCurInventoryStatus());
            targetInventoryBaseInfoDTO.setWarehouseId(param.getTargetWarehouseId());
            targetInventoryBaseInfoDTO.setWarehouseLocation(param.getTargetWarehouseLocation());
            targetInventoryBaseInfoDTO.setOrgId(param.getTargetOrgId());
            targetInventoryBaseInfoDTO.setSkuId(param.getSkuId());
            targetInventoryBaseInfoDTO.setSkuNo(param.getSkuNo());
            targetInventoryBaseInfoDTO.setQty(param.getQty());
            targetInventoryBaseInfoDTO.setWarehouseOption(InventoryWarehouseOptionEnum.WAREHOUSE_TARGET);//目的仓

            this.checkEnoughStockIfNecessary(targetInventoryBaseInfoDTO, businessType, transactionRules);// 目的仓出库库存数量检查（因为有可能是目的仓出库）
        }
    }

    /**
     * 调拨参数转换成出入库参数
     * @param param
     * @param warehouseOption
     * @return
     */
    public InStockOrOutStockTransformDTO wrapInOutStockByTransfer(TransferDTO param, InventoryWarehouseOptionEnum warehouseOption) {
        InStockOrOutStockTransformDTO inOutStockParam = new InStockOrOutStockTransformDTO();

        if(Objects.equals(warehouseOption, InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT)) {
            inOutStockParam.setOrgId(param.getCurOrgId());
            inOutStockParam.setWarehouseId(param.getCurWarehouseId());
            inOutStockParam.setWarehouseLocation(param.getCurWarehouseLocation());
            inOutStockParam.setInventoryStatus(param.getCurInventoryStatus());
            inOutStockParam.setInventoryMode(param.getCurInventoryMode());
        } else if (Objects.equals(warehouseOption, InventoryWarehouseOptionEnum.WAREHOUSE_TARGET)) {
            inOutStockParam.setOrgId(param.getTargetOrgId());
            inOutStockParam.setWarehouseId(param.getTargetWarehouseId());
            inOutStockParam.setWarehouseLocation(param.getTargetWarehouseLocation());
            inOutStockParam.setInventoryStatus(param.getTargetCurInventoryStatus());
            inOutStockParam.setInventoryMode(param.getTargetInventoryMode());
        }
        inOutStockParam.setSourceType(param.getSourceType());
        inOutStockParam.setSourceId(param.getSourceId());
        inOutStockParam.setSourceDetailId(param.getSourceDetailId());
        inOutStockParam.setSourceCode(param.getSourceCode());
        inOutStockParam.setBillDate(param.getBillDate());
        inOutStockParam.setSkuId(param.getSkuId());
        inOutStockParam.setSkuNo(param.getSkuNo());

        inOutStockParam.setQty(param.getQty());
        inOutStockParam.setOperationMode(param.getOperationMode());
        inOutStockParam.setWarehouseOptionEnum(warehouseOption);
        return inOutStockParam;
    }

}