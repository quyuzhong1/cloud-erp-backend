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
import java.util.*;
import java.util.stream.Collectors;

/**
 * 虚拟库存出入库处理
 * @author will
 * @date 2024/6/4 11:28
 */
@Slf4j
@Service
@InventoryHandler(InventoryBizTypeEnum.IN_OUT_STOCK)
public class VirtualInventoryStockServiceImpl extends AbstractVirtualInventoryServiceImpl {

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private RedissonClient redisson;

    @Override
    public <T extends VirtualInventoryStockDTO.StockBaseDTO> List<RLock> handleLockKey(List<T> paramList) {
        List<RLock> rLockList = new ArrayList<>();
        List<String> lockKeyList = new ArrayList<>();
        for(VirtualInventoryStockDTO.StockBaseDTO baseParam : paramList) {
            if (baseParam instanceof VirtualInventoryStockDTO.OutInStockDTO) {
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
    public <T extends VirtualInventoryStockDTO.StockBaseDTO> void checkParam(List<T> paramList, VirtualInventoryBusinessTypeEnum businessType, List<VirtualTransRuleDTO.StockParamDTO> ruleList) {
        // 判断是否需要忽略计算库存的sku
        List<String>  ignoreInventorySkuIds = this.getIgnoreSkuIds();
        for(VirtualInventoryStockDTO.StockBaseDTO baseParam : paramList) {
            if(baseParam instanceof VirtualInventoryStockDTO.OutInStockDTO) {
                // 出入库业务-走交易规则
                VirtualInventoryStockDTO.OutInStockDTO param = (VirtualInventoryStockDTO.OutInStockDTO) baseParam;
                ValidatorUtil.validateEntity(param);
                if(0 == param.getQty()) {
                    throw new ServiceException("库存变更数量不能等于0");
                }

                if(param.getQty() < 0 && !this.allowNegativeQtyBusinessList.contains(param.getSourceType())) {
                    throw new ServiceException("库存变更数量不能小于0");
                }

                if(ignoreInventorySkuIds.contains(param.getSkuId())) {
                    log.warn("sku id: {}，sku编号：{}产品属性是费用或服务，不参与库存出入库，不做库存验证", param.getSkuId(), param.getSkuNo());
                    continue;
                }

                VirtualInventoryStockDTO.InventoryDTO inventoryDTO = new VirtualInventoryStockDTO.InventoryDTO();
                inventoryDTO.setSourceType(param.getSourceType());
                inventoryDTO.setBillDate(param.getBillDate());
                inventoryDTO.setSourceId(param.getSourceId());
                inventoryDTO.setVirtualWarehouseId(param.getVirtualWarehouseId());
                inventoryDTO.setWarehouseId(param.getWarehouseId());
                inventoryDTO.setSkuId(param.getSkuId());
                inventoryDTO.setSkuNo(param.getSkuNo());
                inventoryDTO.setQty(param.getQty());
                inventoryDTO.setWarehouseOption(InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT);
                // 出库库存数量检查
                this.checkStockByRule(inventoryDTO, businessType, ruleList);
            }
        }
    }

    /**
     * 循环处理出入库业务
     * @param paramList     业务参数
     * @param businessType  业务类型
     * @param transactionRuleParams 交易规则
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public <T extends VirtualInventoryStockDTO.StockBaseDTO> void stockHandler(List<T> paramList, VirtualInventoryBusinessTypeEnum businessType, List<VirtualTransRuleDTO.StockParamDTO> transactionRuleParams, String transactionNo) {
        // 获取忽略库存计算的sku
        List<String> ignoreInventorySkuIds = this.getIgnoreSkuIds();
        // 通过对sku id顺序执行, 避免多线程死锁
        Comparator<VirtualInventoryStockDTO.StockBaseDTO> comparing = Comparator.comparing(VirtualInventoryStockDTO.StockBaseDTO::getSkuId)
                .thenComparing(VirtualInventoryStockDTO.StockBaseDTO::getVirtualWarehouseId)
                .thenComparing(VirtualInventoryStockDTO.StockBaseDTO::getWarehouseId);
        paramList = paramList.stream().sorted(comparing).collect(Collectors.toList());
        for(VirtualInventoryStockDTO.StockBaseDTO baseParam : paramList) {
            VirtualInventoryStockDTO.OutInStockDTO param = (VirtualInventoryStockDTO.OutInStockDTO)baseParam;
            if(ignoreInventorySkuIds.contains(param.getSkuId())) {
                log.warn("sku id: {}，sku编号：{}产品属性是费用或服务，不参与库存出入库", param.getSkuId(), param.getSkuNo());
                continue;
            }
            this.singleHandler(param, businessType, transactionRuleParams, transactionNo);
        }


    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public <T extends VirtualInventoryStockDTO.StockBaseDTO> void singleHandler(T baseParam, VirtualInventoryBusinessTypeEnum businessType, List<VirtualTransRuleDTO.StockParamDTO> transactionRuleParams, String transactionNo) {
        VirtualInventoryStockDTO.OutInStockDTO param = (VirtualInventoryStockDTO.OutInStockDTO)baseParam;
        if(CollUtil.isEmpty(transactionRuleParams)) {
            throw new ServiceException(ApiError.ERROR_99034.code, CharSequenceUtil.format(ApiError.ERROR_99034.msg, businessType.getName()));
        }
        log.warn("从配置读取库存交易规则，业务类型：【{}】，单据类型：【{}】，单据id：【{}】，单据日期：【{}】,SKU编号：【{}】,交易配置信息：【{}】", businessType.getName(), param.getSourceType().getName(), param.getSourceId(), param.getBillDate(), param.getSkuNo(), JSONObject.toJSONString(transactionRuleParams));
        // 交易规则按照状态排序
        transactionRuleParams = transactionRuleParams.stream()
                .sorted(Comparator.comparing(x -> ObjectUtil.isNotEmpty(x.getInventoryStatus()) ? x.getInventoryStatus().getCode(): ""))
                .collect(Collectors.toList());
        for(VirtualTransRuleDTO.StockParamDTO stockParamDTO : transactionRuleParams) {
            //验证枚举是否必填
            checkTransRule(stockParamDTO);

            // 可能某个业务类型在同一个仓库即需要做入也需要做出，分别调用逻辑
            VirtualInventoryStockDTO.StockCoreDTO stockCoreDTO = BeanMapperUtils.map(VirtualInventoryStockDTO.StockCoreDTO.class, param);
            stockCoreDTO.setOperationMode(InventoryOperationModeEnum.APPROVE);

            InventoryModeEnum inventoryModeEnum = stockParamDTO.getTransactionMode();
            InventoryStatusEnum inventoryStatusEnum = stockParamDTO.getInventoryStatus();

            if(Objects.equals(InventoryModeEnum.IN_STOCK, inventoryModeEnum)) {
                // 入库
                this.inStockCore(stockCoreDTO, businessType, inventoryStatusEnum, stockParamDTO.getId(), transactionNo);
            } else if (Objects.equals(InventoryModeEnum.OUT_STOCK, inventoryModeEnum)) {
                // 出库
                this.outStockCore(stockCoreDTO, businessType, inventoryStatusEnum,  stockParamDTO.getId(), transactionNo);
            }
        }
    }


    /**
     * 验证枚举是否必填
     * @author will
     * @date 2024/6/4 17:37
     * @param stockParamDTO
     */
    private  void checkTransRule(VirtualTransRuleDTO.StockParamDTO stockParamDTO) {
        // 交易规则-选项错误
        InventoryWarehouseOptionEnum inventoryWarehouseOptionEnum = stockParamDTO.getWarehouseOption();
        ValidatorUtil.isTrue(Objects.nonNull(inventoryWarehouseOptionEnum), () -> new ServiceException(ApiError.ERROR_99033));

        // 交易规则-库存状态错误
        InventoryStatusEnum inventoryStatusEnum = stockParamDTO.getInventoryStatus();
        ValidatorUtil.isTrue(Objects.nonNull(inventoryStatusEnum), () -> new ServiceException(ApiError.ERROR_99036));

        // 交易规则-交易类型错误
        InventoryModeEnum inventoryModeEnum = stockParamDTO.getTransactionMode();
        ValidatorUtil.isTrue(Objects.nonNull(inventoryModeEnum), () -> new ServiceException(ApiError.ERROR_99038));
    }
}