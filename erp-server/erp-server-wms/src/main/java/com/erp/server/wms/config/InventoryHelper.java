package com.erp.server.wms.config;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.ValidatorUtil;
import com.erp.model.wms.dto.inventory.InStockOrOutStockDTO;
import com.erp.model.wms.dto.inventory.InventoryBaseInfoDTO;
import com.erp.model.wms.dto.inventory.TransactionFlowDTO;
import com.erp.model.wms.dto.inventory.TransactionRuleDTO;
import com.erp.model.wms.entity.CfgTransactionRulesEntity;
import com.erp.model.wms.enums.SourceTypeEnum;
import com.erp.model.wms.enums.inventory.InventoryBusinessTypeEnum;
import com.erp.model.wms.enums.inventory.InventoryModeEnum;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.model.wms.enums.inventory.InventoryWarehouseOptionEnum;
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
        List<TransactionRuleDTO> outTransactionRules;
        if(Objects.isNull(inventoryStatusEnum)) { // 从配置中取，配置中也取不到则报错
            log.info("参数未传库存状态，从配置中取，业务类型：【{}】，单据类型：【{}】，单据id：【{}】，单据日期：【{}】，SKU编号：【{}】，不包含出库业务，不检查", businessType.getName(), sourceTypeEnum.getName(), sourceId, billDate, param.getSkuNo());
            if(CollUtil.isEmpty(transactionRules)) {
                throw new ServiceException(ApiError.ERROR_99034.code, StrUtil.format(ApiError.ERROR_99034.msg, businessType.getName()));
            }
            outTransactionRules = transactionRules.stream().filter(r->Objects.equals(r.getTransactionMode(), InventoryModeEnum.OUT_STOCK)).collect(Collectors.toList());
            if(CollUtil.isNotEmpty(outTransactionRules)) {
                for (TransactionRuleDTO rule : outTransactionRules) {
                    InventoryStatusEnum ruleInventoryStatusEnum = rule.getInventoryStatus();
                    ValidatorUtil.isTrueCall(Objects.nonNull(ruleInventoryStatusEnum),()->new ServiceException(ApiError.ERROR_99036));
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
        Integer usableQty = inventoryService.getInventoryTotal(orgId, warehouseId, skuId, warehouseLocationId, status.getCode());
        log.info("仓库【{}】，组织：【{}】，库位：【{}】，SKU：【{}】，SKU编号：{}, 来源单据：{}, 业务类型：【{}】，状态【{}】，操作数量：【{}】，库存状态对应的总数量：【{}】", warehouseId, orgId, warehouseLocationId,skuId, skuNo, sourceTypeEnum.getName(),
                businessType.getName(), status.getName(), qty, usableQty);
        ValidatorUtil.isTrueCall(usableQty >= qty,()->new ServiceException(ApiError.ERROR_99035));
    }

    /**
     *
     * @param transactionRulesEntities
     * @return
     */
    public List<TransactionRuleDTO> wrapTransactionRule(List<CfgTransactionRulesEntity> transactionRulesEntities) {
        if(CollUtil.isNotEmpty(transactionRulesEntities)) {
            List<TransactionRuleDTO> transactionRuleDTOS = Lists.newArrayListWithExpectedSize(transactionRulesEntities.size());
            transactionRulesEntities.stream().forEach(r->{
                TransactionRuleDTO transactionRuleDTO = new TransactionRuleDTO();
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
                                                            String inventoryDetailId, InventoryStatusEnum inventoryStatusEnum, LocalDate instockBatchDate) {
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
        transactionFlowDTO.setQty(param.getQty());
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

}