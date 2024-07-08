package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.common.business.annotation.DistributeLocker;
import com.common.business.enums.InventoryClosedRecordEnum;
import com.common.business.utils.RedisUtil;
import com.common.core.exception.ServiceException;
import com.common.message.constant.RedisKeyConstant;
import com.erp.model.wms.dto.StocktakingProfitLossDetailDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.inventory.InventoryTransactionDTO;
import com.erp.model.wms.entity.InventoryEntity;
import com.erp.model.wms.entity.InventoryHisEntity;
import com.erp.model.wms.entity.TransactionFlowEntity;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.server.wms.service.*;
import org.apache.commons.collections4.IteratorUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Classname: InventoryTradingServiceImpl
 * @Description: 库存交易辅助类
 * @CreateTime: 2024-07-04
 * @Author: Edison.Qu
 */
@Service
public class InventoryTradingServiceImpl implements InventoryTradingService {
    @Resource
    private RedisUtil redisUtil;
    @Resource
    private TransactionFlowService transactionFlowService;
    @Resource
    private InventoryHisService inventoryHisService;
    @Resource
    private InventoryService inventoryService;
    @Resource
    private WarehouseService warehouseService;
    @Resource
    private InventoryClosedRecordService inventoryClosedRecordService;
    @Resource
    private StocktakingProfitLossServiceImpl stocktakingProfitLossService;

    @Override
    @DistributeLocker(keyName = "skuId,warehouseId,warehouseLocation,inventoryStatus", businessType = InventoryTransCoreService.BUSINESS_TYPE )
    public void doTransactionList(List<InventoryTransactionDTO> transactionList, String approveType) {
        if(CollectionUtils.isEmpty(transactionList)) {
            return;
        }
        //校验单据是否已经审批
        if(approveType.equals(InventoryTradingService.APPROVE)) {
            this.checkHasApproved(transactionList.get(0));
        }

        // 移除忽略的sku
        transactionList.removeIf(InventoryTransactionDTO::isIgnoreTransaction);
        // 排序
        transactionList = this.sortInventoryTransactionList(transactionList);

        // 检查业务是否允许交易
        this.checkAllowTransactionList(transactionList);
        // 检查每日库存是否充足
        this.checkInventoryHisList(transactionList);
        // 检查库存是否充足
        this.checkInventoryList(transactionList);

        // 处理库存更新逻辑
        for (InventoryTransactionDTO transactionDTO : transactionList) {
            this.doTransaction(transactionDTO);
        }
        // 保存/删除交易记录
        if(approveType.equals(InventoryTradingService.APPROVE)){
            // 批量增加交易记录
            List<TransactionFlowEntity> transactionFlowList= this.convertTransactionFlowList(transactionList);
            this.saveTransactionFlowList(transactionFlowList);

        }else{
            // 批量删除交易记录
            List<String> ids = this.getTransactionFlowIds(transactionList);
            this.deleteTransactionFlowList(ids);
        }
    }

    /**
     * 处理库存交易
     * @param transactionDTO    库存交易信息
     */
    public void doTransaction(InventoryTransactionDTO transactionDTO) {
        this.checkInventoryList(Collections.singletonList(transactionDTO));
        this.updateInventory(transactionDTO);
        this.updateInventoryHis(transactionDTO);
        this.updateInventoryTransaction(transactionDTO);
    }

    /**
     * 校验单据是否已经审批
     * @param transactionDTO    交易记录
     */
    private void checkHasApproved(InventoryTransactionDTO transactionDTO) {
        TransactionFlowEntity transactionFlow = new TransactionFlowEntity();
        QueryWrapper<TransactionFlowEntity> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(TransactionFlowEntity::getSourceType, transactionDTO.getSourceType())
                .eq(TransactionFlowEntity::getSourceId, transactionDTO.getSourceId())
                .select(TransactionFlowEntity::getId)
                .last("limit 1");
        transactionFlow = transactionFlowService.getOne(wrapper);
        if(transactionFlow != null) {
            ServiceException.runError("单据已经审批,单据类型:{}编号:{},不能重复审批", transactionDTO.getSourceTypeName(), transactionDTO.getSourceCode());
        }
    }

    /**
     * 排序
     * @param transactionList   交易记录列表
     * @return  排序后的交易记录列表
     */
    private List<InventoryTransactionDTO> sortInventoryTransactionList(List<InventoryTransactionDTO> transactionList) {
        Comparator<InventoryTransactionDTO> comparing = Comparator.comparing(InventoryTransactionDTO::getSkuId)
                .thenComparing(InventoryTransactionDTO::getWarehouseId)
                .thenComparing(x -> StrUtil.isNotEmpty(x.getWarehouseLocation()) ? x.getWarehouseLocation() : "")
                .thenComparing(x -> StrUtil.isNotEmpty(x.getInventoryStatus()) ? x.getInventoryStatus() : "");

        transactionList = transactionList.stream().sorted(comparing).collect(Collectors.toList());
        return transactionList;
    }

    /**
     * 检查业务是否允许交易
     * @param transactionList   交易记录列表
     */
    private void checkAllowTransactionList(List<InventoryTransactionDTO> transactionList) {
        // 检查关账
        this.checkClosePeriod(transactionList);
        // 检查盘点中
        this.checkStocktaking(transactionList);
        // 检查是否晚与盘盈盘亏
        this.checkAfterProfitLoss(transactionList);
    }

    /**
     * 检查关账
     * @param transactionList   交易记录列表
     */
    private void checkClosePeriod(List<InventoryTransactionDTO> transactionList) {
        StringBuilder errList = new StringBuilder();

        if(CollectionUtils.isEmpty(transactionList)) {
            return;
        }
        LocalDate billDate = transactionList.get(0).getBillDate();

        // 查询最新库存关账记录
        Map<String, LocalDate> closedDateMap = inventoryClosedRecordService.mapByOrgId(InventoryClosedRecordEnum.STK.getCode());

        for(InventoryTransactionDTO transactionDTO:transactionList) {
            LocalDate closeDate = closedDateMap.get(transactionDTO.getOrgId());

            // 存在关账时间并非在途库存
            if(null != closeDate && !InventoryStatusEnum.IN_TRANSIT.getCode().equals(transactionDTO.getInventoryStatus())){
                if (!billDate.isAfter(closeDate)) {
                    errList.append(StrUtil.format("库存组织:[{}]交易时间:[{}]已关账目，sku:[{}]仓库:[{}]仓位:[{}]库存状态：[{}] 不允许交易\n"
                            , transactionDTO.getOrgName()
                            , billDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                            , transactionDTO.getSkuNo()
                            , transactionDTO.getWarehouseName()
                            , transactionDTO.getWarehouseLocationName()
                            , transactionDTO.getInventoryStatusName()));
                }
            }
        }

        if(errList.length() > 0) {
            ServiceException.runError(errList.toString());
        }
    }

    /**
     * 检查盘点中
     * @param transactionList   交易记录列表
     */
    private void checkStocktaking(List<InventoryTransactionDTO> transactionList) {
        StringBuilder errList = new StringBuilder();
        for(InventoryTransactionDTO transactionDTO:transactionList) {
            String redisKey = StrUtil.format(RedisKeyConstant.INVENTORY_LOCK,
                    "*"
                    , transactionDTO.getOrgId()
                    , transactionDTO.getWarehouseId()
                    , transactionDTO.getWarehouseLocation()
                    , transactionDTO.getSkuId()
                    , transactionDTO.getInventoryStatus());
            Collection<String> keys = redisUtil.keys(redisKey);
            if (CollUtil.isEmpty(keys)) {
                continue;
            }
            WarehouseDTO.UpdateDTO updateDTO = warehouseService.detailWithCache(transactionDTO.getWarehouseId());
            String warehouseName = ObjectUtil.isNotEmpty(updateDTO) ? updateDTO.getName() : transactionDTO.getWarehouseId();

            errList.append(StrUtil.format("sku:[{}]仓库:[{}]仓位:[{}]库存状态：[{}]正在盘点中,不允许交易\n"
                    , transactionDTO.getSkuNo()
                    , warehouseName
                    , transactionDTO.getWarehouseLocationName()
                    , transactionDTO.getInventoryStatusName()));

        }

        if(errList.length() > 0) {
            ServiceException.runError(errList.toString());
        }
    }

    /**
     * 检查是否晚与盘盈盘亏
     * @param transactionList   交易记录列表
     */
    private void checkAfterProfitLoss(List<InventoryTransactionDTO> transactionList) {
        StringBuilder errList = new StringBuilder();

        if(CollectionUtils.isEmpty(transactionList)) {
            return;
        }
        LocalDate billDate = transactionList.get(0).getBillDate();

        List<String> warehouseIds = transactionList.stream().map(InventoryTransactionDTO::getWarehouseId).distinct().collect(Collectors.toList());
        List<String> orgIds = transactionList.stream().map(InventoryTransactionDTO::getOrgId).distinct().collect(Collectors.toList());
        List<String> skuIds = transactionList.stream().map(InventoryTransactionDTO::getSkuId).distinct().collect(Collectors.toList());

        List<StocktakingProfitLossDetailDTO.LastDTO> lastStocktakingProfitLossList = stocktakingProfitLossService.maxDateByParams(warehouseIds, orgIds, skuIds);
        if(CollectionUtils.isEmpty(lastStocktakingProfitLossList)) {
            return;
        }

        for(InventoryTransactionDTO transactionDTO:transactionList) {
            if (!InventoryStatusEnum.IN_TRANSIT.getCode().equals(transactionDTO.getInventoryStatus())){
                // 盘盈盘亏单 匹配 仓库ID, 组织ID，仓位，skuId
                StocktakingProfitLossDetailDTO.LastDTO lastDTO = lastStocktakingProfitLossList.stream()
                        .filter(e -> e.getSkuId().equalsIgnoreCase(transactionDTO.getSkuId())
                                && e.getWarehouseOrgId().equalsIgnoreCase(transactionDTO.getOrgId())
                                && e.getWarehouseId().equalsIgnoreCase(transactionDTO.getWarehouseId())
                                && e.getWarehouseLocation().equals(null == transactionDTO.getWarehouseLocation() ? "" : transactionDTO.getWarehouseLocation())
                                )
                        .findFirst()
                        .orElse(null);
                if (null != lastDTO && (billDate.isBefore(lastDTO.getBillDate()) || billDate.equals(lastDTO.getBillDate()))){
                    // 已有盘盈盘亏单【{}】不允许操作【{}】之前单据
                    errList.append(StrUtil.format("sku:[{}]仓库:[{}]仓位:[{}]库存状态：[{}]单据日期:[{}],已有盘盈盘亏单【{}】不允许操作【{}】之前单据\n"
                            , transactionDTO.getSkuNo()
                            , transactionDTO.getWarehouseName()
                            , transactionDTO.getWarehouseLocationName()
                            , transactionDTO.getInventoryStatusName()
                            , billDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                            , lastDTO.getCode()
                            , lastDTO.getBillDate().format(DateTimeFormatter.ISO_LOCAL_DATE)));
                }
            }
        }
        if (errList.length() > 0) {
            ServiceException.runError(errList.toString());
        }
    }

    /**
     * 检查所有库存交易，库存是否充足
     * @param transactionList   交易记录列表
     */
    private void checkInventoryList(List<InventoryTransactionDTO> transactionList) {
        StringBuilder errList = new StringBuilder();
        for(InventoryTransactionDTO transactionDTO:transactionList) {
            errList.append(checkInventory(transactionDTO));
        }

        if(errList.length() > 0) {
            ServiceException.runError(errList.toString());
        }
    }

    /**
     * 检查所有库存交易，每日库存是否充足
     * @param transactionList   交易记录列表
     */
    private void checkInventoryHisList(List<InventoryTransactionDTO> transactionList) {
        StringBuilder errList = new StringBuilder();
        for(InventoryTransactionDTO transactionDTO:transactionList) {
            errList.append(checkInventoryHis(transactionDTO));
        }
        if(errList.length() > 0) {
            ServiceException.runError(errList.toString());
        }
    }

    /**
     * 检查库存是否充足
     * @param transactionDTO    库存交易信息
     */
    private String checkInventory(InventoryTransactionDTO transactionDTO) {
        int inventoryQty = 0;

        if(transactionDTO.isAllowNegativeInventory()) {
            return "";
        }

        if(null != transactionDTO.getInventoryId()) {
            InventoryEntity inventoryEntity = inventoryService.getById(transactionDTO.getInventoryId());
            inventoryQty = (null == inventoryEntity ? 0 : inventoryEntity.getQty());

        }
        if(inventoryQty + transactionDTO.getQty() < 0) {
            return StrUtil.format("sku:[{}]仓库:[{}]仓位:[{}]库存状态：[{}]库存不足,库存:{},交易数:{}\n"
                    , transactionDTO.getSkuNo()
                    , transactionDTO.getWarehouseName()
                    , transactionDTO.getWarehouseLocationName()
                    , transactionDTO.getInventoryStatusName()
                    , inventoryQty
                    , transactionDTO.getQty());
        }
        return "";
    }

    /**
     * 检查每日库存是否充足
     * @param transactionDTO    库存交易信息
     * @return  错误信息
     */
    private String checkInventoryHis(InventoryTransactionDTO transactionDTO) {
        if(transactionDTO.isAllowNegativeInventory()) {
            return "";
        }

        if(null != transactionDTO.getInventoryId()) {
            List<InventoryHisEntity> inventoryHisList = inventoryHisService.list(new QueryWrapper<InventoryHisEntity>()
                    .eq("info_id", transactionDTO.getInventoryId())
                    .gt("bill_date", transactionDTO.getBillDate()));

            for (InventoryHisEntity inventoryHisEntity : inventoryHisList) {
                int inventoryQty = inventoryHisEntity.getQty();
                if (inventoryQty + transactionDTO.getQty() < 0) {
                    return StrUtil.format("sku:[{}]仓库:[{}]仓位:[{}]库存状态：[{}]交易会导致[{}]库存不足,库存:{},交易数:{}\n"
                            , transactionDTO.getSkuNo()
                            , transactionDTO.getWarehouseName()
                            , transactionDTO.getWarehouseLocationName()
                            , transactionDTO.getInventoryStatusName()
                            , inventoryHisEntity.getBillDate()
                            , inventoryQty
                            , transactionDTO.getQty());
                }
            }
        }

        return "";
    }

    /**
     * 更新库存
     * @param transactionDTO    库存交易信息
     */
    private void updateInventory(InventoryTransactionDTO transactionDTO) {
        if(null == transactionDTO.getInventoryId()) {
            ServiceException.runError("sku:[{}]仓库:[{}]仓位:[{}]库存状态：[{}],inventory_id为空,请让【实施工程师】协调开发人员处理",
                    transactionDTO.getSkuNo(),transactionDTO.getWarehouseName(),transactionDTO.getWarehouseLocationName(),transactionDTO.getInventoryStatusName());
        }

        UpdateWrapper<InventoryEntity> wrapper = new UpdateWrapper<>();
        wrapper.setSql("qty = qty + #{transactionDTO.getQty()}" +
                ",update_time = now()" +
                ",update_user_id = #{transactionDTO.getUserId()}" +
                ",update_user_name = #{transactionDTO.getUserName");

        wrapper.lambda().eq(InventoryEntity::getId, transactionDTO.getInventoryId());
        inventoryService.update(wrapper);
    }

    /**
     * 更新库存历史
     * @param transactionDTO    库存交易信息
     */
    private void updateInventoryHis(InventoryTransactionDTO transactionDTO) {
        if(null == transactionDTO.getInventoryId()) {
            ServiceException.runError("sku:[{}]仓库:[{}]仓位:[{}]库存状态：[{}],inventory_id为空,请让【实施工程师】协调开发人员处理",
                    transactionDTO.getSkuNo(),transactionDTO.getWarehouseName(),transactionDTO.getWarehouseLocationName(),transactionDTO.getInventoryStatusName());
        }

        UpdateWrapper<InventoryHisEntity> wrapper = new UpdateWrapper<>();
        wrapper.setSql("qty = qty + #{transactionDTO.getQty()}" +
                ",update_time = now()" +
                ",update_user_id = #{transactionDTO.getUserId()}" +
                ",update_user_name = #{transactionDTO.getUserName");

        wrapper.lambda().eq(InventoryHisEntity::getInfoId, transactionDTO.getInventoryId())
                .gt(InventoryHisEntity::getBillDate, transactionDTO.getBillDate());
        inventoryHisService.update(wrapper);
    }

    /**
     * 更新库存交易 的库存数量
     * @param transactionDTO    库存交易信息
     */
    private void updateInventoryTransaction(InventoryTransactionDTO transactionDTO) {
        if(null == transactionDTO.getInventoryId()) {
            ServiceException.runError("sku:[{}]仓库:[{}]仓位:[{}]库存状态：[{}],inventory_id为空,请让【实施工程师】协调开发人员处理",
                    transactionDTO.getSkuNo(),transactionDTO.getWarehouseName(),transactionDTO.getWarehouseLocationName(),transactionDTO.getInventoryStatusName());
        }

        UpdateWrapper<TransactionFlowEntity> wrapper = new UpdateWrapper<>();
        wrapper.setSql("cur_inventory_qty = cur_inventory_qty + #{transactionDTO.getQty()} " +
                ",update_time = now()" +
                ",update_user_id = #{transactionDTO.getUserId()}" +
                ",update_user_name = #{transactionDTO.getUserName");
        wrapper.lambda().eq(TransactionFlowEntity::getInventoryId, transactionDTO.getInventoryId())
                .gt(TransactionFlowEntity::getBillDate, transactionDTO.getBillDate());
        transactionFlowService.update(wrapper);

    }

    /**
     * 转换交易记录列表
     * @param transactionList   交易记录列表
     * @return  交易记录列表
     */
    private List<TransactionFlowEntity> convertTransactionFlowList(List<InventoryTransactionDTO> transactionList) {
        List<TransactionFlowEntity> result = new ArrayList<>();

        for(InventoryTransactionDTO transactionDTO:transactionList) {
            TransactionFlowEntity transactionFlowEntity = new TransactionFlowEntity();

            // 交易头部信息
            transactionFlowEntity.setId(transactionDTO.getId());
            transactionFlowEntity.setTransactionNo(transactionDTO.getTransactionNo());
            transactionFlowEntity.setTransactionRuleId(transactionDTO.getTransactionRuleId());

            // 交易明细信息
            transactionFlowEntity.setInventoryId(transactionDTO.getInventoryId());
            transactionFlowEntity.setSkuId(transactionDTO.getSkuId());
            transactionFlowEntity.setSkuNo(transactionDTO.getSkuNo());
            transactionFlowEntity.setOrgId(transactionDTO.getOrgId());
            transactionFlowEntity.setWarehouseId(transactionDTO.getWarehouseId());
            transactionFlowEntity.setWarehouseLocation(transactionDTO.getWarehouseLocation());
            transactionFlowEntity.setDictInventoryStatus(transactionDTO.getInventoryStatus());

            // 交易时间 & 单据类型
            transactionFlowEntity.setBillDate(transactionDTO.getBillDate());
            transactionFlowEntity.setDictBizType(transactionDTO.getDictBizType());
            transactionFlowEntity.setSourceType(transactionDTO.getSourceType());
            transactionFlowEntity.setSourceId(transactionDTO.getSourceId());
            transactionFlowEntity.setSourceCode(transactionDTO.getSourceCode());
            transactionFlowEntity.setSourceDetailId(transactionDTO.getSourceDetailId());

            // 交易数量
            transactionFlowEntity.setQty(transactionDTO.getQty());

            // 交易人员信息
            transactionFlowEntity.setCreateUserId(transactionDTO.getUserId());
            transactionFlowEntity.setCreateUserName(transactionDTO.getUserName());
            transactionFlowEntity.setCreateTime(LocalDateTime.now());

            transactionFlowEntity.setUpdateUserId(transactionDTO.getUserId());
            transactionFlowEntity.setUpdateUserName(transactionDTO.getUserName());
            transactionFlowEntity.setUpdateTime(LocalDateTime.now());

            transactionFlowEntity.setUserId(transactionDTO.getUserId());
            transactionFlowEntity.setTradeTime(LocalDateTime.now());
            transactionFlowEntity.setOperationMode("approve");

            result.add(transactionFlowEntity);
        }
        return result;
    }

    /**
     * 批量保存交易记录
     * @param transactionFlowList   交易记录列表
     */
    private void saveTransactionFlowList(List<TransactionFlowEntity> transactionFlowList) {
        // 设置每页大小
        int pageSize = 200;

        // 创建迭代器
        Iterator<TransactionFlowEntity> iterator = transactionFlowList.iterator();

        // 按页批量保存
        while (iterator.hasNext()) {
            List<TransactionFlowEntity> page = IteratorUtils.toList(IteratorUtils.boundedIterator(iterator, pageSize));
            transactionFlowService.saveBatch(page);
        }

    }

    /**
     * 获取交易记录id列表
     * @param transactionList   交易记录列表
     * @return  交易记录id列表
     */
    private List<String> getTransactionFlowIds(List<InventoryTransactionDTO> transactionList) {
        return transactionList.stream()
                .map(InventoryTransactionDTO::getId)
                .collect(Collectors.toList());
    }

    /**
     * 批量删除交易记录
     * @param idList    交易记录id列表
     */
    private void deleteTransactionFlowList(List<String> idList) {
        // 设置每页大小
        int pageSize = 1000;

        // 创建迭代器
        Iterator<String> iterator = idList.iterator();

        // 按页批量保存
        while (iterator.hasNext()) {
            List<String> page = IteratorUtils.toList(IteratorUtils.boundedIterator(iterator, pageSize));
            transactionFlowService.removeByIds(page);
        }

    }

}