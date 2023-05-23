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

    @Autowired
    private WarehouseService warehouseService;

    @Autowired
    private WarehouseLocationService warehouseLocationService;

    @Override
    public <T extends InventoryStockBaseDTO> void checkParam(List<T> paramList, InventoryBusinessTypeEnum businessType, List<TransactionRuleDTO> transactionRules) {
        Map<String, WarehouseDTO.UpdateDTO> warehouseMap = Maps.newHashMap();
        Map<String, WarehouseLocationEntity> warehouseLocationMap = Maps.newHashMap();
        for(InventoryStockBaseDTO baseParam : paramList) {
            if(baseParam instanceof TransferDTO) { // 调拨走交易规则
                TransferDTO param = (TransferDTO)baseParam;
                ValidatorUtil.validateEntity(param);

                //当前仓和目的仓不能一样
                ValidatorUtil.isTrue(!Objects.equals(param.getCurWarehouseId(), param.getTargetWarehouseId()),()->new ServiceException(ApiError.ERROR_99039));

                // 当前仓仓库和仓位信息
                WarehouseDTO.UpdateDTO warehouseDetail = warehouseMap.computeIfAbsent(param.getCurWarehouseId(),(v)->warehouseService.detailWithCache(v));
                if(Objects.isNull(warehouseDetail) || StrUtil.isEmpty(warehouseDetail.getId())) {
                    throw new ServiceException(ApiError.ERROR_99002);
                }
                if(StrUtils.isNotEmpty(param.getCurWarehouseLocation())) {
                    WarehouseLocationEntity warehouseLocation = warehouseLocationMap.computeIfAbsent(param.getCurWarehouseLocation(),(v)->warehouseLocationService.findByWarehouseIdAndCode(param.getCurWarehouseId(), v));
                    if(Objects.isNull(warehouseLocation) || StrUtil.isEmpty(warehouseLocation.getId())) {
                        throw new ServiceException("仓位信息不存在");
                    }
                }
                // 目的仓仓库和仓位信息
                warehouseDetail = warehouseMap.computeIfAbsent(param.getTargetWarehouseId(),(v)->warehouseService.detailWithCache(v));
                if(Objects.isNull(warehouseDetail) || StrUtil.isEmpty(warehouseDetail.getId())) {
                    throw new ServiceException(ApiError.ERROR_99002);
                }
                if(StrUtils.isNotEmpty(param.getTargetWarehouseLocation())) {
                    WarehouseLocationEntity warehouseLocation = warehouseLocationMap.computeIfAbsent(param.getTargetWarehouseLocation(),(v)->warehouseLocationService.findByWarehouseIdAndCode(param.getTargetWarehouseId(), v));
                    if(Objects.isNull(warehouseLocation) || StrUtil.isEmpty(warehouseLocation.getId())) {
                        throw new ServiceException("仓位信息不存在");
                    }
                }

                inventoryHelper.checkCommonBiz(param.getSourceType(), param.getSourceId(), param.getBillDate());// 通用检查
                inventoryHelper.checkAllowTrade(param.getSourceType(), param.getCurWarehouseId(), param.getSkuNo());// 当前仓关账检查
                inventoryHelper.checkAllowTrade(param.getSourceType(), param.getTargetWarehouseId(), param.getSkuNo());// 目的仓关账检查

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

                inventoryHelper.checkStockByRule(currInventoryBaseInfoDTO, businessType, transactionRules);// 当前仓出库库存数量检查（因为有可能是当前仓入库）

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

                inventoryHelper.checkStockByRule(targetInventoryBaseInfoDTO, businessType, transactionRules);// 目的仓出库库存数量检查（因为有可能是目的仓出库）
            } else if (baseParam instanceof TransferCustomDTO) { //调拨自定义规则
                TransferCustomDTO param = (TransferCustomDTO)baseParam;
                ValidatorUtil.validateEntity(param);

                //当前仓和目的仓不能一样
                ValidatorUtil.isTrue(!Objects.equals(param.getCurWarehouseId(), param.getTargetWarehouseId()),()->new ServiceException(ApiError.ERROR_99039));

                // 当前仓仓库和仓位信息
                WarehouseDTO.UpdateDTO warehouseDetail = warehouseMap.computeIfAbsent(param.getCurWarehouseId(),(v)->warehouseService.detailWithCache(v));
                if(Objects.isNull(warehouseDetail) || StrUtil.isEmpty(warehouseDetail.getId())) {
                    throw new ServiceException(ApiError.ERROR_99002);
                }
                if(StrUtils.isNotEmpty(param.getCurWarehouseLocation())) {
                    WarehouseLocationEntity warehouseLocation = warehouseLocationMap.computeIfAbsent(param.getCurWarehouseLocation(),(v)->warehouseLocationService.findByWarehouseIdAndCode(param.getCurWarehouseId(), v));
                    if(Objects.isNull(warehouseLocation) || StrUtil.isEmpty(warehouseLocation.getId())) {
                        throw new ServiceException("仓位信息不存在");
                    }
                }
                // 目的仓仓库和仓位信息
                warehouseDetail = warehouseMap.computeIfAbsent(param.getTargetWarehouseId(),(v)->warehouseService.detailWithCache(v));
                if(Objects.isNull(warehouseDetail) || StrUtil.isEmpty(warehouseDetail.getId())) {
                    throw new ServiceException(ApiError.ERROR_99002);
                }
                if(StrUtils.isNotEmpty(param.getTargetWarehouseLocation())) {
                    WarehouseLocationEntity warehouseLocation = warehouseLocationMap.computeIfAbsent(param.getTargetWarehouseLocation(),(v)->warehouseLocationService.findByWarehouseIdAndCode(param.getTargetWarehouseId(), v));
                    if(Objects.isNull(warehouseLocation) || StrUtil.isEmpty(warehouseLocation.getId())) {
                        throw new ServiceException("仓位信息不存在");
                    }
                }

                inventoryHelper.checkCommonBiz(param.getSourceType(), param.getSourceId(), param.getBillDate());// 通用检查
                inventoryHelper.checkAllowTrade(param.getSourceType(), param.getCurWarehouseId(), param.getSkuNo());// 当前仓关账检查
                inventoryHelper.checkAllowTrade(param.getSourceType(), param.getTargetWarehouseId(), param.getSkuNo());// 目的仓关账检查

                InventoryBaseInfoDTO currInventoryBaseInfoDTO = new InventoryBaseInfoDTO();
                currInventoryBaseInfoDTO.setSourceType(param.getSourceType());
                currInventoryBaseInfoDTO.setBillDate(param.getBillDate());
                currInventoryBaseInfoDTO.setSourceId(param.getSourceId());
                currInventoryBaseInfoDTO.setWarehouseId(param.getCurWarehouseId());
                currInventoryBaseInfoDTO.setWarehouseLocation(param.getCurWarehouseLocation());
                currInventoryBaseInfoDTO.setSkuId(param.getSkuId());
                currInventoryBaseInfoDTO.setSkuNo(param.getSkuNo());
                currInventoryBaseInfoDTO.setQty(param.getQty());
                currInventoryBaseInfoDTO.setInventoryStatus(param.getCurInventoryStatus()); // 自定义调拨需手动传输库存状态
                currInventoryBaseInfoDTO.setInventoryMode(param.getCurInventoryMode()); // 自定义调拨需手动传输库存交易方向
                currInventoryBaseInfoDTO.setWarehouseOption(InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT);//当前仓

                inventoryHelper.checkStockByStatus(currInventoryBaseInfoDTO, businessType, transactionRules);// 当前仓出库库存数量检查（因为有可能是当前仓入库）

                InventoryBaseInfoDTO targetInventoryBaseInfoDTO = new InventoryBaseInfoDTO();
                targetInventoryBaseInfoDTO.setSourceType(param.getSourceType());
                targetInventoryBaseInfoDTO.setBillDate(param.getBillDate());
                targetInventoryBaseInfoDTO.setSourceId(param.getSourceId());
                targetInventoryBaseInfoDTO.setWarehouseId(param.getTargetWarehouseId());
                targetInventoryBaseInfoDTO.setWarehouseLocation(param.getTargetWarehouseLocation());
                targetInventoryBaseInfoDTO.setSkuId(param.getSkuId());
                targetInventoryBaseInfoDTO.setSkuNo(param.getSkuNo());
                targetInventoryBaseInfoDTO.setQty(param.getQty());
                targetInventoryBaseInfoDTO.setInventoryStatus(param.getTargetCurInventoryStatus()); // 自定义调拨需手动传输库存状态
                targetInventoryBaseInfoDTO.setInventoryMode(param.getTargetInventoryMode()); // 自定义调拨需手动传输库存交易方向
                targetInventoryBaseInfoDTO.setWarehouseOption(InventoryWarehouseOptionEnum.WAREHOUSE_TARGET);//目的仓

                inventoryHelper.checkStockByStatus(targetInventoryBaseInfoDTO, businessType, transactionRules);// 目的仓出库库存数量检查（因为有可能是目的仓出库）
            }
        }
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
        for(InventoryStockBaseDTO baseParam : paramLis) {
            // 当前仓出入库业务处理
            TransferDTO param = (TransferDTO)baseParam;
            InOutStockTransformDTO curWareInOrOutStock = InventoryUtils.wrapInOutStockByTransfer(param, InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT, InventoryOperationModeEnum.APPROVE);
            this.singleHandler(curWareInOrOutStock, businessType, transactionRuleParams, transactionNo);
            // 目的仓出入库业务处理
            InOutStockTransformDTO targetWareInOrOutStock = InventoryUtils.wrapInOutStockByTransfer(param, InventoryWarehouseOptionEnum.WAREHOUSE_TARGET, InventoryOperationModeEnum.APPROVE);
            this.singleHandler(targetWareInOrOutStock, businessType, transactionRuleParams, transactionNo);
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
            ValidatorUtil.isTrue(Objects.nonNull(inventoryModeEnum),()->new ServiceException(ApiError.ERROR_400.code, "交易类型不能为空"));
            // 转换成出入库参数
            InOutStockCoreDTO inOutStockCoreDTO = InventoryUtils.wrapCoreParamByTransfer(param, InventoryOperationModeEnum.APPROVE);
            if(Objects.equals(InventoryModeEnum.IN_STOCK, inventoryModeEnum)) { //入库
                this.inStockCore(inOutStockCoreDTO, businessType, param.getInventoryStatus(), "",  transactionNo);
            } else if (Objects.equals(InventoryModeEnum.OUT_STOCK, inventoryModeEnum)) { // 出库
                this.outStockCore(inOutStockCoreDTO, businessType, param.getInventoryStatus(), "", transactionNo);
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
                InOutStockCoreDTO inOutStockCoreDTO = InventoryUtils.wrapCoreParamByTransfer(param, InventoryOperationModeEnum.APPROVE);
                if(Objects.equals(InventoryModeEnum.IN_STOCK, inventoryModeEnum)) { // 入库
                    this.inStockCore(inOutStockCoreDTO, businessType, inventoryStatusEnum, transactionRule.getId(), transactionNo);
                } else if (Objects.equals(InventoryModeEnum.OUT_STOCK, inventoryModeEnum)) { // 出库
                    this.outStockCore(inOutStockCoreDTO, businessType, inventoryStatusEnum,  transactionRule.getId(), transactionNo);
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