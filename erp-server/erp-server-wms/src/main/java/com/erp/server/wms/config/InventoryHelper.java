package com.erp.server.wms.config;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.ValidatorUtil;
import com.erp.model.wms.dto.inventory.InStockOrOutStockDTO;
import com.erp.model.wms.entity.InventoryEntity;
import com.erp.model.wms.entity.TransactionRuleEntity;
import com.erp.model.wms.enums.SourceTypeEnum;
import com.erp.model.wms.enums.inventory.InventoryBusinessTypeEnum;
import com.erp.model.wms.enums.inventory.InventoryModeEnum;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.server.wms.service.InventoryService;
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
     * @param param
     */
    public void checkCommonBiz(InStockOrOutStockDTO param) {
        SourceTypeEnum sourceTypeEnum = param.getSourceTypeEnum();
        String sourceId = param.getSourceId();
        LocalDate billDate = param.getBillDate();
        log.info("开始检查是否关闭账套，单据类型：【{}】，单据id：【{}】，单据日期：【{}】", sourceTypeEnum.getName(), sourceId, billDate);
        // TODO 1.检查是否关账
        log.info("通过检查是否关闭账套，单据类型：【{}】，单据id：【{}】，单据日期：【{}】", sourceTypeEnum.getName(), sourceId, billDate);
    }

    /**
     * 业务验证（盘点中，可能精确到SKU级别）
     * @param param
     */
    public void checkAllowTrade(InStockOrOutStockDTO param) {
        SourceTypeEnum sourceTypeEnum = param.getSourceTypeEnum();
        String sourceId = param.getSourceId();
        LocalDate billDate = param.getBillDate();
        // TODO 检查是否盘点中
        log.info("开始检查是否盘点中，单据类型：【{}】，单据id：【{}】，单据日期：【{}】，SKU：【{}】", sourceTypeEnum.getName(), sourceId, billDate, param.getSkuNo());
        log.info("通过检查是否盘点中，单据类型：【{}】，单据id：【{}】，单据日期：【{}】，SKU：【{}】", sourceTypeEnum.getName(), sourceId, billDate, param.getSkuNo());
    }

    /**
     * 出库检查库存是否足够
     */
    public void checkEnoughStockIfNecessary(InStockOrOutStockDTO param, InventoryBusinessTypeEnum businessType, List<TransactionRuleEntity> transactionRules) {
        SourceTypeEnum sourceTypeEnum = param.getSourceTypeEnum();
        String sourceId = param.getSourceId();
        LocalDate billDate = param.getBillDate();

        Boolean checkOutStock = businessType.getCheckOutStock();
        if(Objects.isNull(checkOutStock) || Objects.equals(checkOutStock, Boolean.FALSE)) {
            log.info("开始检查出库库存是否足够，业务类型：【{}】，单据类型：【{}】，单据id：【{}】，单据日期：【{}】，不包含出库业务，不检查", businessType.getName(), sourceTypeEnum.getName(), sourceId, billDate);
            return;
        }
        // 状态
        InventoryStatusEnum inventoryStatusEnum = param.getInventoryStatusEnum();
        List<TransactionRuleEntity> outTransactionRules;
        if(Objects.isNull(inventoryStatusEnum)) { // 从配置中取，配置中也取不到则报错
            log.info("参数未传库存状态，从配置中取，业务类型：【{}】，单据类型：【{}】，单据id：【{}】，单据日期：【{}】，SKU编号：【{}】，不包含出库业务，不检查", businessType.getName(), sourceTypeEnum.getName(), sourceId, billDate, param.getSkuNo());
            if(CollUtil.isEmpty(transactionRules)) {
                throw new ServiceException(ApiError.ERROR_99034.code, StrUtil.format(ApiError.ERROR_99034.msg, businessType.getName()));
            }
            outTransactionRules = transactionRules.stream().filter(r->Objects.equals(r.getTransactionMode(), InventoryModeEnum.OUT_STOCK.getCode())).collect(Collectors.toList());
            if(CollUtil.isNotEmpty(outTransactionRules)) {
                for (TransactionRuleEntity rule : outTransactionRules) {
                    InventoryStatusEnum ruleInventoryStatusEnum = InventoryStatusEnum.of(rule.getInventoryStatus());
                    ValidatorUtil.isTrueCall(Objects.nonNull(ruleInventoryStatusEnum),()->new ServiceException(ApiError.ERROR_99036));
                    this.checkStockQtyByWareLocalSkuStatus(businessType, param, ruleInventoryStatusEnum);
                }
            }
        } else {
            log.info("参数已传库存状态：【{}】，业务类型：【{}】，单据类型：【{}】，单据id：【{}】，单据日期：【{}】，SKU编号：【{}】", inventoryStatusEnum.getName(), businessType.getName(), sourceTypeEnum.getName(), sourceId, billDate, param.getSkuNo());
            this.checkStockQtyByWareLocalSkuStatus(businessType, param, inventoryStatusEnum);
        }
    }

    public void checkStockQtyByWareLocalSkuStatus(InventoryBusinessTypeEnum businessType, InStockOrOutStockDTO param, InventoryStatusEnum status) {
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
        SourceTypeEnum sourceTypeEnum = param.getSourceTypeEnum();
        InventoryEntity inventoryEntity = inventoryService.findInventoryByWareLocalSkuStatus(orgId, warehouseId, skuId, warehouseLocationId, status.getCode());
        if(Objects.isNull(inventoryEntity)) {
            log.info("仓库【{}】，组织：【{}】，库位：【{}】，SKU：【{}】，SKU编号：{}, 来源单据：{}, 业务类型：【{}】，状态【{}】在库存实时表中不存在数据", warehouseId, orgId, warehouseLocationId,skuId, skuNo, sourceTypeEnum.getName(), businessType.getName(), status.getName());
            throw new ServiceException(ApiError.ERROR_99035);
        }
        log.info("仓库【{}】，组织：【{}】，库位：【{}】，SKU：【{}】，SKU编号：{}, 来源单据：{}, 业务类型：【{}】，状态【{}】，操作数量：【{}】，库存状态数量：【{}】", warehouseId, orgId, warehouseLocationId,skuId, skuNo, sourceTypeEnum.getName(),
                businessType.getName(), status.getName(), qty, inventoryEntity.getQty());
        ValidatorUtil.isTrueCall(inventoryEntity.getQty() >= qty,()->new ServiceException(ApiError.ERROR_99035));
    }

}