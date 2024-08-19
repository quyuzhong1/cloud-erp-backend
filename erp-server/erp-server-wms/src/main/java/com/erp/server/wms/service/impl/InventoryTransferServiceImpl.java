package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
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

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Classname: InventoryTransferServiceImpl
 * @Description: 调拨类业务处理
 * @CreateTime: 2023-05-04  15:49
 * @Author: zhangchunlin
 */
@Service
@Slf4j
@InventoryHandler(InventoryBizTypeEnum.TRANSFER_STOCK)
public class InventoryTransferServiceImpl extends AbstractInventoryServiceImpl {

    @Autowired
    private WarehouseService warehouseService;

    @Autowired
    private WarehouseLocationService warehouseLocationService;

    @Override
    public <T extends InventoryStockBaseDTO> void checkParam(List<T> paramList, InventoryBusinessTypeEnum businessType, List<TransactionRuleDTO> transactionRules) {
        Map<String, WarehouseDTO.UpdateDTO> warehouseMap = Maps.newHashMap();
        Map<String, WarehouseLocationEntity> warehouseLocationMap = Maps.newHashMap();
        // 判断是否需要忽略计算库存的sku
        List<String>  ignoreInventorySkuIds = this.getIgnoreSkuIds();

        for(InventoryStockBaseDTO baseParam : paramList) {
            if(baseParam instanceof TransferDTO) { // 调拨走交易规则
                TransferDTO param = (TransferDTO)baseParam;
                ValidatorUtil.validateEntity(param);

                if(0 == param.getQty()) {
                    ServiceException.runError("库存变更数量不能等于0");
                }

                if(!this.allowNegativeQtyBusinessList.contains(param.getSourceType()) && param.getQty() < 0) {
                    ServiceException.runError("库存变更数量不能小于0");
                }

                if(ignoreInventorySkuIds.contains(param.getSkuId())) {
                    log.warn("sku id: {}，sku编号：{}产品属性是费用或服务，不参与库存出入库，不做库存验证", param.getSkuId(), param.getSkuNo());
                    continue;
                }

                // 调拨/移仓位 的仓库校验
                if(InventoryBusinessTypeEnum.warehouseLocationMoveInfo().contains(businessType)) {
                    //移仓位：当前仓和目的仓必须 一样
                    ValidatorUtil.isTrue(Objects.equals(param.getCurWarehouseId(), param.getTargetWarehouseId()), () -> new ServiceException(ApiError.CURRENT_TARGET_WAREHOUSE_SAME));
                }else {
                    //调拨：当前仓和目的仓必须 不一样
                    ValidatorUtil.isTrue(!Objects.equals(param.getCurWarehouseId(), param.getTargetWarehouseId()), () -> new ServiceException(ApiError.ERROR_99039));
                }

                // 当前仓仓库和仓位信息
                WarehouseDTO.UpdateDTO warehouseDetail = warehouseMap.computeIfAbsent(param.getCurWarehouseId(), v -> warehouseService.detailWithCache(v));
                if(Objects.isNull(warehouseDetail) || StrUtil.isEmpty(warehouseDetail.getId())) {
                    ServiceException.runError(ApiError.ERROR_99002);
                }

                if(StrUtils.isNotEmpty(param.getCurWarehouseLocation())) {
                    WarehouseLocationEntity warehouseLocation = warehouseLocationMap.computeIfAbsent(param.getCurWarehouseLocation(), v->warehouseLocationService.findByWarehouseIdAndCode(param.getCurWarehouseId(), v));
                    if(Objects.isNull(warehouseLocation) || StrUtil.isEmpty(warehouseLocation.getId())) {
                        ServiceException.runError("仓位信息不存在");
                    }
                }
                // 目的仓仓库和仓位信息
                warehouseDetail = warehouseMap.computeIfAbsent(param.getTargetWarehouseId(), v ->warehouseService.detailWithCache(v));
                if(Objects.isNull(warehouseDetail) || StrUtil.isEmpty(warehouseDetail.getId())) {
                    ServiceException.runError(ApiError.ERROR_99002);
                }
                if(StrUtils.isNotEmpty(param.getTargetWarehouseLocation())) {
                    WarehouseLocationEntity warehouseLocation = warehouseLocationMap.computeIfAbsent(param.getTargetWarehouseLocation(),v->warehouseLocationService.findByWarehouseIdAndCode(param.getTargetWarehouseId(), v));
                    if(Objects.isNull(warehouseLocation) || StrUtil.isEmpty(warehouseLocation.getId())) {
                        ServiceException.runError("仓位信息不存在");
                    }
                }

                InventoryBaseInfoDTO currInventoryBaseInfoDTO = new InventoryBaseInfoDTO();
                currInventoryBaseInfoDTO.setSourceType(param.getSourceType());
                currInventoryBaseInfoDTO.setBillDate(param.getBillDate());
                currInventoryBaseInfoDTO.setSourceId(param.getSourceId());
                currInventoryBaseInfoDTO.setWarehouseId(param.getCurWarehouseId());
                currInventoryBaseInfoDTO.setWarehouseLocation(param.getCurWarehouseLocation());
                currInventoryBaseInfoDTO.setSkuId(param.getSkuId());
                currInventoryBaseInfoDTO.setSkuNo(param.getSkuNo());
                currInventoryBaseInfoDTO.setQty(param.getQty());
                currInventoryBaseInfoDTO.setWarehouseOption(InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT);//当前仓

                this.checkStockByRule(currInventoryBaseInfoDTO, businessType, transactionRules);// 当前仓出库库存数量检查（因为有可能是当前仓入库）

                InventoryBaseInfoDTO targetInventoryBaseInfoDTO = new InventoryBaseInfoDTO();
                targetInventoryBaseInfoDTO.setSourceType(param.getSourceType());
                targetInventoryBaseInfoDTO.setBillDate(param.getBillDate());
                targetInventoryBaseInfoDTO.setSourceId(param.getSourceId());
                targetInventoryBaseInfoDTO.setWarehouseId(param.getTargetWarehouseId());
                targetInventoryBaseInfoDTO.setWarehouseLocation(param.getTargetWarehouseLocation());
                targetInventoryBaseInfoDTO.setSkuId(param.getSkuId());
                targetInventoryBaseInfoDTO.setSkuNo(param.getSkuNo());
                targetInventoryBaseInfoDTO.setQty(param.getQty());
                targetInventoryBaseInfoDTO.setWarehouseOption(InventoryWarehouseOptionEnum.WAREHOUSE_TARGET);//目的仓

                this.checkStockByRule(targetInventoryBaseInfoDTO, businessType, transactionRules);// 目的仓出库库存数量检查（因为有可能是目的仓出库）
            }
        }
    }

    @Override
    public <T extends InventoryStockBaseDTO> void stockHandler(List<T> paramList, InventoryBusinessTypeEnum businessType, List<TransactionRuleDTO> transactionRuleParams, String transactionNo) {
        // 获取忽略库存计算的sku
        List<String> ignoreInventorySkuIds = this.getIgnoreSkuIds();
        // 通过对sku id 仓库id 仓位 顺序执行, 避免多线程死锁
        Comparator<InventoryStockBaseDTO> comparing = Comparator.comparing(InventoryStockBaseDTO::getSkuId)
                .thenComparing(x -> ObjectUtil.isNotEmpty(x.getInventoryStatus()) ? x.getInventoryStatus().getCode() : "");
        paramList = paramList.stream().sorted(comparing).collect(Collectors.toList());

        for(InventoryStockBaseDTO baseParam : paramList) {
            // 当前仓出入库业务处理
            TransferDTO param = (TransferDTO)baseParam;
            if(Objects.nonNull(param.getCurWarehouseLocation())) {
                param.setCurWarehouseLocation(StrUtils.null2EmptyWithTrim(param.getCurWarehouseLocation()));
            }
            if(Objects.nonNull(param.getTargetWarehouseLocation())) {
                param.setTargetWarehouseLocation(StrUtils.null2EmptyWithTrim(param.getTargetWarehouseLocation()));
            }
            InOutStockTransformDTO curWareInOrOutStock = InventoryUtils.wrapInOutStockByTransfer(param, InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT, InventoryOperationModeEnum.APPROVE);
            if(ignoreInventorySkuIds.contains(param.getSkuId())) {
                log.warn("sku id: {}，sku编号：{}产品属性是费用或服务，不参与库存出入库", param.getSkuId(), param.getSkuNo());
                continue;
            }
            // 目的仓出入库业务处理
            InOutStockTransformDTO targetWareInOrOutStock = InventoryUtils.wrapInOutStockByTransfer(param, InventoryWarehouseOptionEnum.WAREHOUSE_TARGET, InventoryOperationModeEnum.APPROVE);
            Stream.of(curWareInOrOutStock, targetWareInOrOutStock)
                    .sorted(Comparator.comparing(InOutStockTransformDTO::getWarehouseId)
                            .thenComparing(x -> ObjectUtil.isNotEmpty(x.getWarehouseLocation()) ? x.getWarehouseLocation() : "")
                            .thenComparing(x -> ObjectUtil.isNotEmpty(x.getInventoryStatus()) ? x.getInventoryStatus().getCode(): "")
                    ).forEach(wareInOrOutStock -> this.singleHandler(wareInOrOutStock, businessType, transactionRuleParams, transactionNo));
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public <T extends InventoryStockBaseDTO> void singleHandler(T baseParam, InventoryBusinessTypeEnum businessType, List<TransactionRuleDTO> transactionRuleParams, String transactionNo) {
        InOutStockTransformDTO param = (InOutStockTransformDTO)baseParam;
        // 状态
        if(Objects.nonNull(param.getInventoryStatus())) { // 参数传输了要改的状态
            log.info("参数已传库存状态：【{}】，业务类型：【{}】，单据类型：【{}】，单据id：【{}】，单据日期：【{}】,SKU编号：【{}】", param.getInventoryStatus().getName(), businessType.getName(), param.getSourceType().getName(), param.getSourceId(), param.getBillDate(), param.getSkuNo());
            InventoryModeEnum inventoryModeEnum = param.getInventoryMode();
            ValidatorUtil.isTrue(Objects.nonNull(inventoryModeEnum),()->new ServiceException(ApiError.ERROR_99999.code, "交易类型不能为空"));
            // 转换成出入库参数
            InOutStockCoreDTO inOutStockCoreDTO = InventoryUtils.wrapCoreParamByTransfer(param, InventoryOperationModeEnum.APPROVE);
            if(Objects.equals(InventoryModeEnum.IN_STOCK, inventoryModeEnum)) { //入库
                this.inStockCore(inOutStockCoreDTO, businessType, param.getInventoryStatus(), "",  transactionNo);
            } else if (Objects.equals(InventoryModeEnum.OUT_STOCK, inventoryModeEnum)) { // 出库
                this.outStockCore(inOutStockCoreDTO, businessType, param.getInventoryStatus(), "", transactionNo);
            }
        } else {
            if(CollUtil.isEmpty(transactionRuleParams)) {
                ServiceException.runError(ApiError.ERROR_99034.code, StrUtil.format(ApiError.ERROR_99034.msg, businessType.getName()));
            }
            log.info("参数未传库存状态，从配置读取，业务类型：【{}】，单据类型：【{}】，单据id：【{}】，单据日期：【{}】,SKU编号：【{}】,交易配置信息：【{}】", businessType.getName(), param.getSourceType().getName(), param.getSourceId(), param.getBillDate(), param.getSkuNo(), JSONObject.toJSONString(transactionRuleParams));
            // 判断当前仓是入库还是出库
            transactionRuleParams = transactionRuleParams.stream().filter(r->Objects.equals(r.getWarehouseOption(), param.getWarehouseOptionEnum())).collect(Collectors.toList());
            if(CollUtil.isEmpty(transactionRuleParams)) {
                ServiceException.runError(ApiError.ERROR_99034.code, StrUtil.format(ApiError.ERROR_99034.msg, businessType.getName()));
            }
            transactionRuleParams = transactionRuleParams.stream()
                    .sorted(Comparator.comparing(inventoryStatus -> inventoryStatus.getInventoryStatus().getCode()))
                    .collect(Collectors.toList());
            for(TransactionRuleDTO transactionRule : transactionRuleParams) {
                InventoryWarehouseOptionEnum inventoryWarehouseOptionEnum = transactionRule.getWarehouseOption();
                ValidatorUtil.isTrue(Objects.nonNull(inventoryWarehouseOptionEnum), () -> new ServiceException(ApiError.ERROR_99033));
                InventoryStatusEnum inventoryStatusEnum = transactionRule.getInventoryStatus();
                ValidatorUtil.isTrue(Objects.nonNull(inventoryStatusEnum), () -> new ServiceException(ApiError.ERROR_99036));
                InventoryModeEnum inventoryModeEnum = transactionRule.getTransactionMode();
                ValidatorUtil.isTrue(Objects.nonNull(inventoryModeEnum), () -> new ServiceException(ApiError.ERROR_99038));
                // 可能某个业务类型在同一个仓库即需要做入也需要做出，分别调用逻辑
                InOutStockCoreDTO inOutStockCoreDTO = InventoryUtils.wrapCoreParamByTransfer(param, InventoryOperationModeEnum.APPROVE);
                if(Objects.equals(InventoryModeEnum.IN_STOCK, inventoryModeEnum)) { // 入库
                    this.inStockCore(inOutStockCoreDTO, businessType, inventoryStatusEnum, transactionRule.getId(), transactionNo);
                } else if (Objects.equals(InventoryModeEnum.OUT_STOCK, inventoryModeEnum)) { // 出库
                    this.outStockCore(inOutStockCoreDTO, businessType, inventoryStatusEnum,  transactionRule.getId(), transactionNo);
                }
            }
        }
    }

}