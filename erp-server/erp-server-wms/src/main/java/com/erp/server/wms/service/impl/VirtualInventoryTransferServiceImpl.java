package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.business.enums.DistributedLockEnum;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ValidatorUtil;
import com.erp.model.wms.dto.inventory.VirtualInventoryStockDTO;
import com.erp.model.wms.dto.inventory.VirtualTransRuleDTO;
import com.erp.model.wms.enums.inventory.*;
import com.erp.server.wms.annotation.InventoryHandler;
import com.erp.server.wms.service.WarehouseService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 调拨类业务处理
 * @author will
 * @date 2024/6/5 18:17
 */
@Service
@Slf4j
@InventoryHandler(InventoryBizTypeEnum.TRANSFER_STOCK)
public class VirtualInventoryTransferServiceImpl extends AbstractVirtualInventoryServiceImpl {

    @Resource
    private WarehouseService warehouseService;


    @Resource
    private RedissonClient redisson;

    @Override
    public <T extends VirtualInventoryStockDTO.StockBaseDTO> List<RLock> handleLockKey(List<T> paramList) {
        List<RLock> rLockList = new ArrayList<>();
        List<String> lockKeyList = new ArrayList<>();
        for(VirtualInventoryStockDTO.StockBaseDTO baseParam : paramList) {
            if (baseParam instanceof VirtualInventoryStockDTO.TransferStockDTO) {
                // 按照虚拟仓库+实体仓库+SKU+库存状态进行锁定
                String lockKey = CharSequenceUtil.format( "{}:{}:{}:{}", DistributedLockEnum.WMS_VIRTUAL_INVENTORY_SKU.getCode(), baseParam.getVirtualWarehouseId(), baseParam.getWarehouseId(),baseParam.getSkuId());
                if (lockKeyList.contains(lockKey)) {
                    continue;
                }
                RLock rLock = redisson.getLock(lockKey);
                rLockList.add(rLock);
                lockKeyList.add(lockKey);
            }
        }
        return rLockList;
    }

    @Override
    public <T extends VirtualInventoryStockDTO.StockBaseDTO> void checkParam(List<T> paramList, VirtualInventoryBusinessTypeEnum businessType, List<VirtualTransRuleDTO.StockParamDTO> transactionRules) {
        // 判断是否需要忽略计算库存的sku
        List<String>  ignoreInventorySkuIds = this.getIgnoreSkuIds();

        for(VirtualInventoryStockDTO.StockBaseDTO baseParam : paramList) {
            if(baseParam instanceof VirtualInventoryStockDTO.TransferStockDTO) {
                // 调拨走交易规则
                VirtualInventoryStockDTO.TransferStockDTO param = (VirtualInventoryStockDTO.TransferStockDTO)baseParam;
                ValidatorUtil.validateEntity(param);

                if(0 == param.getQty()) {
                    throw new ServiceException("库存变更数量不能等于0");
                }

                if(!this.allowNegativeQtyBusinessList.contains(param.getSourceType()) && param.getQty() < 0) {
                    throw new ServiceException("库存变更数量不能小于0");
                }

                if(ignoreInventorySkuIds.contains(param.getSkuId())) {
                    log.warn("sku id: {}，sku编号：{}产品属性是费用或服务，不参与库存出入库，不做库存验证", param.getSkuId(), param.getSkuNo());
                    continue;
                }
                //调拨：当前仓和目的仓必须 不一样
                ValidatorUtil.isTrue(!Objects.equals(param.getVirtualCurWarehouseId(), param.getVirtualTargetWarehouseId()), () -> new ServiceException(ApiError.ERROR_99039));

                VirtualInventoryStockDTO.InventoryDTO currInventoryDTO = new VirtualInventoryStockDTO.InventoryDTO();
                currInventoryDTO.setSourceType(param.getSourceType());
                currInventoryDTO.setBillDate(param.getBillDate());
                currInventoryDTO.setSourceId(param.getSourceId());
                currInventoryDTO.setWarehouseId(param.getWarehouseId());
                currInventoryDTO.setVirtualWarehouseId(param.getVirtualCurWarehouseId());
                currInventoryDTO.setSkuId(param.getSkuId());
                currInventoryDTO.setSkuNo(param.getSkuNo());
                currInventoryDTO.setQty(param.getQty());
                //当前仓
                currInventoryDTO.setWarehouseOption(InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT);

                // 当前仓出库库存数量检查（因为有可能是当前仓入库）
                this.checkStockByRule(currInventoryDTO, businessType, transactionRules);

                VirtualInventoryStockDTO.InventoryDTO targetInventoryDTO = new VirtualInventoryStockDTO.InventoryDTO();
                targetInventoryDTO.setSourceType(param.getSourceType());
                targetInventoryDTO.setBillDate(param.getBillDate());
                targetInventoryDTO.setSourceId(param.getSourceId());
                targetInventoryDTO.setWarehouseId(param.getWarehouseId());
                targetInventoryDTO.setVirtualWarehouseId(param.getVirtualTargetWarehouseId());
                targetInventoryDTO.setSkuId(param.getSkuId());
                targetInventoryDTO.setSkuNo(param.getSkuNo());
                targetInventoryDTO.setQty(param.getQty());
                //目的仓
                targetInventoryDTO.setWarehouseOption(InventoryWarehouseOptionEnum.WAREHOUSE_TARGET);

                // 目的仓出库库存数量检查（因为有可能是目的仓出库）
                this.checkStockByRule(targetInventoryDTO, businessType, transactionRules);
            }
        }
    }

    @Override
    public <T extends VirtualInventoryStockDTO.StockBaseDTO> void stockHandler(List<T> paramList, VirtualInventoryBusinessTypeEnum businessType, List<VirtualTransRuleDTO.StockParamDTO> transactionRuleParams, String transactionNo) {
        // 获取忽略库存计算的sku
        List<String> ignoreInventorySkuIds = this.getIgnoreSkuIds();
        // 通过对sku id 仓库id 仓位 顺序执行, 避免多线程死锁
        Comparator<VirtualInventoryStockDTO.StockBaseDTO> comparing = Comparator.comparing(VirtualInventoryStockDTO.StockBaseDTO::getSkuId)
                .thenComparing(x -> x.getVirtualWarehouseId())
                .thenComparing(x -> x.getWarehouseId())
                .thenComparing(x -> ObjectUtil.isNotEmpty(x.getInventoryStatus()) ? x.getInventoryStatus().getCode() : "");
        paramList = paramList.stream().sorted(comparing).collect(Collectors.toList());
        for(VirtualInventoryStockDTO.StockBaseDTO baseParam : paramList) {
            // 当前仓出入库业务处理
            VirtualInventoryStockDTO.TransferStockDTO param = (VirtualInventoryStockDTO.TransferStockDTO)baseParam;

            VirtualInventoryStockDTO.TransferDTO curWareInOrOutStock = this.wrapInOutStockByTransfer(param, InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT, InventoryOperationModeEnum.APPROVE);
            if(ignoreInventorySkuIds.contains(param.getSkuId())) {
                log.warn("sku id: {}，sku编号：{}产品属性是费用或服务，不参与库存出入库", param.getSkuId(), param.getSkuNo());
                continue;
            }
            // 目的仓出入库业务处理
            VirtualInventoryStockDTO.TransferDTO targetWareInOrOutStock = this.wrapInOutStockByTransfer(param, InventoryWarehouseOptionEnum.WAREHOUSE_TARGET, InventoryOperationModeEnum.APPROVE);
            Stream.of(curWareInOrOutStock, targetWareInOrOutStock)
                    .sorted(Comparator.comparing(VirtualInventoryStockDTO.TransferDTO::getWarehouseId)
                            .thenComparing(x -> ObjectUtil.isNotEmpty(x.getVirtualWarehouseId()) ? x.getVirtualWarehouseId() : "")
                            .thenComparing(x -> ObjectUtil.isNotEmpty(x.getInventoryStatus()) ? x.getInventoryStatus().getCode(): "")
                    ).forEach(wareInOrOutStock -> this.singleHandler(wareInOrOutStock, businessType, transactionRuleParams, transactionNo));
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public <T extends VirtualInventoryStockDTO.StockBaseDTO> void singleHandler(T baseParam, VirtualInventoryBusinessTypeEnum businessType, List<VirtualTransRuleDTO.StockParamDTO> transactionRuleParams, String transactionNo) {
        VirtualInventoryStockDTO.TransferDTO param = (VirtualInventoryStockDTO.TransferDTO)baseParam;
        // 状态
        if(Objects.nonNull(param.getInventoryStatus())) {
            // 参数传输了要改的状态
            log.info("参数已传库存状态：【{}】，业务类型：【{}】，单据类型：【{}】，单据id：【{}】，单据日期：【{}】,SKU编号：【{}】", param.getInventoryStatus().getName(), businessType.getName(), param.getSourceType().getName(), param.getSourceId(), param.getBillDate(), param.getSkuNo());
            InventoryModeEnum inventoryModeEnum = param.getInventoryMode();
            ValidatorUtil.isTrue(Objects.nonNull(inventoryModeEnum),()->new ServiceException(ApiError.ERROR_99999.code, "交易类型不能为空"));
            // 转换成出入库参数
            VirtualInventoryStockDTO.StockCoreDTO inOutStockCoreDTO = BeanMapperUtils.map(VirtualInventoryStockDTO.StockCoreDTO.class, param);
            inOutStockCoreDTO.setOperationMode(InventoryOperationModeEnum.APPROVE);

            if(Objects.equals(InventoryModeEnum.IN_STOCK, inventoryModeEnum)) {
                //入库
                this.inStockCore(inOutStockCoreDTO, businessType, param.getInventoryStatus(), "",  transactionNo);
            } else if (Objects.equals(InventoryModeEnum.OUT_STOCK, inventoryModeEnum)) {
                // 出库
                this.outStockCore(inOutStockCoreDTO, businessType, param.getInventoryStatus(), "", transactionNo);
            }
        } else {
            if(CollUtil.isEmpty(transactionRuleParams)) {
                throw new ServiceException(ApiError.ERROR_99034.code, CharSequenceUtil.format(ApiError.ERROR_99034.msg, businessType.getName()));
            }
            log.info("参数未传库存状态，从配置读取，业务类型：【{}】，单据类型：【{}】，单据id：【{}】，单据日期：【{}】,SKU编号：【{}】,交易配置信息：【{}】", businessType.getName(), param.getSourceType().getName(), param.getSourceId(), param.getBillDate(), param.getSkuNo(), JSONObject.toJSONString(transactionRuleParams));
            // 判断当前仓是入库还是出库
            transactionRuleParams = transactionRuleParams.stream().filter(r->Objects.equals(r.getWarehouseOption(), param.getWarehouseOptionEnum())).collect(Collectors.toList());
            if(CollUtil.isEmpty(transactionRuleParams)) {
                throw new ServiceException(ApiError.ERROR_99034.code, CharSequenceUtil.format(ApiError.ERROR_99034.msg, businessType.getName()));
            }
            transactionRuleParams = transactionRuleParams.stream()
                    .sorted(Comparator.comparing(inventoryStatus -> inventoryStatus.getInventoryStatus().getCode()))
                    .collect(Collectors.toList());
            for(VirtualTransRuleDTO.StockParamDTO transactionRule : transactionRuleParams) {
                InventoryWarehouseOptionEnum inventoryWarehouseOptionEnum = transactionRule.getWarehouseOption();
                ValidatorUtil.isTrue(Objects.nonNull(inventoryWarehouseOptionEnum), () -> new ServiceException(ApiError.ERROR_99033));
                InventoryStatusEnum inventoryStatusEnum = transactionRule.getInventoryStatus();
                ValidatorUtil.isTrue(Objects.nonNull(inventoryStatusEnum), () -> new ServiceException(ApiError.ERROR_99036));
                InventoryModeEnum inventoryModeEnum = transactionRule.getTransactionMode();
                ValidatorUtil.isTrue(Objects.nonNull(inventoryModeEnum), () -> new ServiceException(ApiError.ERROR_99038));
                // 可能某个业务类型在同一个仓库即需要做入也需要做出，分别调用逻辑
                VirtualInventoryStockDTO.StockCoreDTO inOutStockCoreDTO = BeanMapperUtils.map(VirtualInventoryStockDTO.StockCoreDTO.class, param);
                inOutStockCoreDTO.setOperationMode(InventoryOperationModeEnum.APPROVE);
                if(Objects.equals(InventoryModeEnum.IN_STOCK, inventoryModeEnum)) {
                    // 入库
                    this.inStockCore(inOutStockCoreDTO, businessType, inventoryStatusEnum, transactionRule.getId(), transactionNo);
                } else if (Objects.equals(InventoryModeEnum.OUT_STOCK, inventoryModeEnum)) {
                    // 出库
                    this.outStockCore(inOutStockCoreDTO, businessType, inventoryStatusEnum,  transactionRule.getId(), transactionNo);
                }
            }
        }
    }

    /**
     * 格式话调拨对象
     *
     *
     *
     *
     *
     * @author will
     * @date 2024/6/5 20:00
     * @param param
     * @param warehouseOption
     * @param operationMode
     * @return TransferDTO
     */
    private  VirtualInventoryStockDTO.TransferDTO wrapInOutStockByTransfer(VirtualInventoryStockDTO.TransferStockDTO param, InventoryWarehouseOptionEnum warehouseOption, InventoryOperationModeEnum operationMode) {
        VirtualInventoryStockDTO.TransferDTO transferDTO = new VirtualInventoryStockDTO.TransferDTO();

        if(Objects.equals(warehouseOption, InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT)) {
            transferDTO.setVirtualWarehouseId(param.getVirtualCurWarehouseId());
        } else if (Objects.equals(warehouseOption, InventoryWarehouseOptionEnum.WAREHOUSE_TARGET)) {
            transferDTO.setVirtualWarehouseId(param.getVirtualTargetWarehouseId());
        }
        transferDTO.setWarehouseId(param.getWarehouseId());
        transferDTO.setSourceType(param.getSourceType());
        transferDTO.setSourceId(param.getSourceId());
        transferDTO.setSourceDetailId(param.getSourceDetailId());
        transferDTO.setSourceCode(param.getSourceCode());
        transferDTO.setBillDate(param.getBillDate());
        transferDTO.setSkuId(param.getSkuId());
        transferDTO.setSkuNo(param.getSkuNo());

        transferDTO.setQty(param.getQty());
        transferDTO.setOperationMode(operationMode);
        transferDTO.setWarehouseOptionEnum(warehouseOption);
        return transferDTO;
    }
}