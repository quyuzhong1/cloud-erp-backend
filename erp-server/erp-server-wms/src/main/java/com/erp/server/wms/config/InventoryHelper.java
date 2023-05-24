package com.erp.server.wms.config;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.StrUtils;
import com.common.core.utils.ValidatorUtil;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.inventory.*;
import com.erp.model.wms.entity.CfgTransactionRulesEntity;
import com.erp.model.wms.entity.InventoryEntity;
import com.erp.model.wms.enums.inventory.*;
import com.erp.server.wms.service.CfgTransactionRulesService;
import com.erp.server.wms.service.InventoryService;
import com.erp.server.wms.service.InventoryStockService;
import com.erp.server.wms.service.WarehouseService;
import com.erp.server.wms.service.impl.AbstractInventoryServiceImpl;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
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

    @Autowired
    private WarehouseService warehouseService;

    @Resource
    private ApplicationContext applicationContext;

    private static Map<InventoryBizTypeEnum, InventoryStockService> inventoryServiceMap;

    @PostConstruct
    public void init() {
        Map<String,InventoryStockService> springInventoryServiceMap  = applicationContext.getBeansOfType(InventoryStockService.class);
        inventoryServiceMap = new ConcurrentHashMap<>();
        springInventoryServiceMap.forEach((key,value) -> inventoryServiceMap.put(value.handlerType(),value));
    }

    public AbstractInventoryServiceImpl getInventoryService(InventoryBizTypeEnum inventoryBizTypeEnum) {
        return (AbstractInventoryServiceImpl)inventoryServiceMap.get(inventoryBizTypeEnum);
    }

    /**
     * 通用业务验证
     * @param sourceTypeEnum
     * @param sourceId
     * @param billDate
     */
    public void checkCommonBiz(InventorySourceTypeEnum sourceTypeEnum, String sourceId, LocalDate billDate) {
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
    public void checkAllowTrade(InventorySourceTypeEnum sourceTypeEnum, String warehouseId, String skuNo) {
        // TODO 检查是否盘点中
        log.info("开始检查是否盘点中，仓库：【{}】，单据类型：【{}】，SKU：【{}】", warehouseId, sourceTypeEnum.getName(), skuNo);
        log.info("通过检查是否盘点中，仓库：【{}】，单据类型：【{}】，SKU：【{}】", warehouseId, sourceTypeEnum.getName(), skuNo);
    }

    /**
     * 出库检查库存是否足够（走交易规则，不能手工传输库存状态）
     */
    public void checkStockByRule(InventoryBaseInfoDTO param, InventoryBusinessTypeEnum businessType, List<TransactionRuleDTO> transactionRules) {
        InventorySourceTypeEnum sourceTypeEnum = param.getSourceType();
        String sourceId = param.getSourceId();
        LocalDate billDate = param.getBillDate();
        List<TransactionRuleDTO> outTransactionRules;
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
    }

    /**
     * 出库检查库存是否足够（手工传输库存状态）
     */
    public void checkStockByStatus(InventoryBaseInfoDTO param, InventoryBusinessTypeEnum businessType, List<TransactionRuleDTO> transactionRules) {
        InventorySourceTypeEnum sourceTypeEnum = param.getSourceType();
        String sourceId = param.getSourceId();
        LocalDate billDate = param.getBillDate();
        // 状态
        InventoryStatusEnum inventoryStatusEnum = param.getInventoryStatus();
        log.info("库存状态：【{}】，业务类型：【{}】，单据类型：【{}】，单据id：【{}】，单据日期：【{}】，SKU编号：【{}】", inventoryStatusEnum.getName(), businessType.getName(), sourceTypeEnum.getName(), sourceId, billDate, param.getSkuNo());
        log.info("参数已传库存状态：【{}】，业务类型：【{}】，单据类型：【{}】，单据id：【{}】，单据日期：【{}】，SKU编号：【{}】", inventoryStatusEnum.getName(), businessType.getName(), sourceTypeEnum.getName(), sourceId, billDate, param.getSkuNo());
        InventoryModeEnum inventoryModeEnum = param.getInventoryMode();
        ValidatorUtil.isTrue(Objects.nonNull(inventoryModeEnum),()->new ServiceException(ApiError.ERROR_400.code, "交易类型不能为空"));
        if(Objects.equals(InventoryModeEnum.OUT_STOCK, inventoryModeEnum)) { // 交易方向为-的检查
            this.checkStockQtyByWareLocalSkuStatus(businessType, param, inventoryStatusEnum);
        }
    }

    /**
     * 检查库存是否足够
     * @param businessType
     * @param param
     * @param status
     */
    public void checkStockQtyByWareLocalSkuStatus(InventoryBusinessTypeEnum businessType, InventoryBaseInfoDTO param, InventoryStatusEnum status) {
        // 仓库
        String warehouseId = param.getWarehouseId();
        // SKU
        String skuId = param.getSkuId();
        String skuNo = param.getSkuNo();
        // 库位
        String warehouseLocation = StrUtils.null2EmptyWithTrim(param.getWarehouseLocation());
        // 操作数量
        Integer qty = param.getQty();
        // 来源
        InventorySourceTypeEnum sourceTypeEnum = param.getSourceType();
        // 仓库信息
        WarehouseDTO.UpdateDTO warehouseDetail = warehouseService.detailWithCache(warehouseId);
        if(Objects.isNull(warehouseDetail) || StrUtil.isEmpty(warehouseDetail.getId())) {
            throw new ServiceException(ApiError.ERROR_99002);
        }
        // 仓库组织
        String orgId = warehouseDetail.getOrgId();

        InventoryEntity inventory = inventoryService.findInventoryLock(orgId, warehouseId,skuId,warehouseLocation,status.getCode());

        String inventoryStatusName = Optional.ofNullable(status).map(InventoryStatusEnum::getName).orElse("");

        if(Objects.isNull(inventory)) {
            log.warn("仓库【{}】，组织：【{}】，库位：【{}】，SKU：【{}】，SKU编号：【{}】, 出库时未找到库存数据", warehouseId, orgId, warehouseLocation,skuId, skuNo);
            throw new ServiceException(ApiError.ERROR_99035.code, StrUtil.format(ApiError.ERROR_99035.msg, warehouseDetail.getName(), warehouseLocation, skuNo, inventoryStatusName));
        }
        log.info("仓库【{}】，组织：【{}】，库位：【{}】，SKU：【{}】，SKU编号：【{}】, 来源单据：【{}】, 业务类型：【{}】，状态【{}】，操作数量：【{}】，库存状态对应的总数量：【{}】", warehouseId, orgId, warehouseLocation,skuId, skuNo, sourceTypeEnum.getName(),
                businessType.getName(), status.getName(), qty, inventory.getQty());
        if(inventory.getQty() < qty) {
            throw new ServiceException(ApiError.ERROR_99035.code, StrUtil.format(ApiError.ERROR_99035.msg, warehouseDetail.getName(), warehouseLocation, skuNo, inventoryStatusName));
        }
    }

    /**
     * 查询配置的交易规则
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



}