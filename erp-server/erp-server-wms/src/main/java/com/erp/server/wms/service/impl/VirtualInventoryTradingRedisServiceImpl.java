package com.erp.server.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.common.business.annotation.DistributeLocker;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.dto.WmsVirtualDetailMsgDTO;
import com.erp.model.wms.dto.inventory.InventoryTransactionDTO;
import com.erp.model.wms.dto.inventory.VirtualInventoryStockDTO;
import com.erp.model.wms.entity.VirtualInventoryEntity;
import com.erp.model.wms.entity.VirtualInventoryHisEntity;
import com.erp.model.wms.entity.VirtualTransFlowEntity;
import com.erp.model.wms.enums.VirtualDetailMsgStatusEnum;
import com.erp.model.wms.enums.inventory.InventorySourceTypeEnum;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.server.wms.service.*;
import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.IteratorUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class VirtualInventoryTradingRedisServiceImpl implements VirtualInventoryTradingService {

    @Resource
    private VirtualInventoryService virtualInventoryService;

    @Resource
    private VirtualInventoryHisService virtualInventoryHisService;

    @Resource
    private VirtualTransFlowService virtualTransFlowService;

    @Resource
    private VirtualTransFlowDetailService virtualTransFlowDetailService;

    @Resource
    private WmsVirtualDetailMsgService wmsVirtualDetailMsgService;

    @Resource
    private VirtualInventoryTransactionService virtualInventoryTransactionService;

    @Override
    @DistributeLocker(businessType = VirtualInventoryTransCoreService.BUSINESS_TYPE,keyName = "transactionList.skuId,transactionList.warehouseId,transactionList.virtualWarehouseId,transactionList.inventoryStatus",unlockAfterTx = false)
    public void doTransactionList(List<VirtualInventoryStockDTO.InventoryTransactionDTO> transactionList, String approveType) {
        //1、过滤掉不需要处理的数据
        transactionList.removeIf(VirtualInventoryStockDTO.InventoryTransactionDTO::isIgnoreTransaction);
        if(CollectionUtils.isEmpty(transactionList)) {
            log.warn("虚拟仓库存交易列表为空！");
            return;
        }
        // 计时器-开始
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            // 2、排序
            transactionList = this.sortVirtualInventoryTransactionList(transactionList);
            log.warn("stopwatch1 ={}",stopwatch.elapsed(TimeUnit.MILLISECONDS));

            log.warn("stopwatch2 ={}",stopwatch.elapsed(TimeUnit.MILLISECONDS));

            // 3-处理库存更新逻辑
            for (VirtualInventoryStockDTO.InventoryTransactionDTO transactionDTO : transactionList) {
                this.doTransaction(transactionDTO,approveType.equals(InventoryTradingService.APPROVE));
            }
            log.warn("stopwatch3 ={}",stopwatch.elapsed(TimeUnit.MILLISECONDS));

            //3.5-新增库存流水
            this.addInventoryTransaction(transactionList , approveType);
            
            // 4-反审核时，批量删除交易记录
            if(approveType.equals(InventoryTradingService.UNAPPROVE)){
                List<String> ids = this.getTransactionFlowIds(transactionList);
                this.deleteTransactionFlowList(ids);
            }
            log.warn("stopwatch4 ={}",stopwatch.elapsed(TimeUnit.MILLISECONDS));
        }catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            stopwatch.stop();
            // 计时器-结束
            if(stopwatch.elapsed(TimeUnit.SECONDS) > 30) {
                log.warn("单据编号：{}，虚拟仓库存交易耗时：{} ms", transactionList.get(0).getSourceCode(),stopwatch.elapsed(TimeUnit.MILLISECONDS));
            }
        }
    }

    public static void main(String[] args) {
        // 计时器-开始
        Stopwatch stopwatch = Stopwatch.createStarted();
        System.out.println("start...stopwatch1 =" + stopwatch.elapsed(TimeUnit.SECONDS));
        System.out.println("23123123");
        System.out.println("start...stopwatch1 =" + stopwatch.elapsed(TimeUnit.SECONDS));
        System.out.println("23123123333");
        System.out.println("start...stopwatch1 =" + stopwatch.elapsed(TimeUnit.SECONDS));
    }

    /**
     * 处理库存交易
     * @param transactionDTO 库存交易信息
     * @param isApprove     是否审批
     */
    public void doTransaction(VirtualInventoryStockDTO.InventoryTransactionDTO transactionDTO, boolean isApprove) {
        // 更新库存
        this.setInventoryId(transactionDTO);
        // 保存当前审批的交易记录
        if(isApprove){
            this.saveCurrTransactionFlow(transactionDTO);
        }
    }

    /**
     * 更新库存交易 的库存数量
     * @param transactionDTO    库存交易信息
     * @param isApprove     是否审批
     */
    private void updateInventoryTransaction(VirtualInventoryStockDTO.InventoryTransactionDTO transactionDTO,boolean isApprove) {
        if(null == transactionDTO.getVirtualInventoryId()) {
            ServiceException.runError("sku:[{}]仓库:[{}]虚拟仓:[{}]库存状态：[{}],inventory_id为空,请让【实施工程师】协调开发人员处理",
                    transactionDTO.getSkuNo(),transactionDTO.getWarehouseName(),transactionDTO.getVirtualWarehouseId(),transactionDTO.getInventoryStatusName());
        }
        // 日期大于当前单据日期的流水更新
        LambdaUpdateWrapper<VirtualTransFlowEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.setSql("cur_inventory_qty = cur_inventory_qty + " + transactionDTO.getQty())
                .set(VirtualTransFlowEntity::getUpdateTime, LocalDateTime.now())
                //条件
                .eq(VirtualTransFlowEntity::getVirtualInventoryId, transactionDTO.getVirtualInventoryId())
                .gt(VirtualTransFlowEntity::getBillDate, transactionDTO.getBillDate());
        virtualTransFlowService.update(wrapper);

        if(!isApprove) {
            // 反审核如果当天存在晚于当前流水创建的流水,需要进行流水重算
            LambdaUpdateWrapper<VirtualTransFlowEntity> wrapperToday = new LambdaUpdateWrapper<>();
            wrapperToday.setSql("cur_inventory_qty = cur_inventory_qty + " + transactionDTO.getQty())
                    .set(VirtualTransFlowEntity::getUpdateTime, LocalDateTime.now())
                    //条件
                    .eq(VirtualTransFlowEntity::getVirtualInventoryId, transactionDTO.getVirtualInventoryId())
                    .eq(VirtualTransFlowEntity::getBillDate, transactionDTO.getBillDate())
                    .gt(VirtualTransFlowEntity::getId, transactionDTO.getId());
            virtualTransFlowService.update(wrapperToday);
        }
    }


    /**
     * 获取交易记录id列表
     * @param transactionList   交易记录列表
     * @return  交易记录id列表
     */
    private List<String> getTransactionFlowIds(List<VirtualInventoryStockDTO.InventoryTransactionDTO> transactionList) {
        return transactionList.stream()
                .map(VirtualInventoryStockDTO.InventoryTransactionDTO::getId)
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
            //批量删除
            virtualTransFlowService.removeByIds(page);
            //查询库存明细本地消息表
            wmsVirtualDetailMsgService.updateStatusByBusinessIds(page, VirtualDetailMsgStatusEnum.WAIT_HANDLE.getCode());
        }

    }


    /**
     * 保存交易记录
     * @param transactionDTO   交易记录列表
     */
    private void saveCurrTransactionFlow(VirtualInventoryStockDTO.InventoryTransactionDTO transactionDTO) {
        VirtualTransFlowEntity virtualTransFlowEntity = new VirtualTransFlowEntity();

        // 交易头部信息
        virtualTransFlowEntity.setId(transactionDTO.getId());
        virtualTransFlowEntity.setTransactionNo(transactionDTO.getTransactionNo());
        virtualTransFlowEntity.setVirtualTransRuleId(transactionDTO.getVirtualTransRuleId());

        // 交易明细信息
        virtualTransFlowEntity.setVirtualInventoryId(transactionDTO.getVirtualInventoryId());
        virtualTransFlowEntity.setSkuId(transactionDTO.getSkuId());
        virtualTransFlowEntity.setSkuNo(transactionDTO.getSkuNo());
        virtualTransFlowEntity.setWarehouseId(transactionDTO.getWarehouseId());
        virtualTransFlowEntity.setVirtualWarehouseId(transactionDTO.getVirtualWarehouseId());
        virtualTransFlowEntity.setDictInventoryStatus(transactionDTO.getInventoryStatus());
        virtualTransFlowEntity.setOrgId(transactionDTO.getOrgId());

        // 交易时间 & 单据类型
        virtualTransFlowEntity.setBillDate(transactionDTO.getBillDate());
        virtualTransFlowEntity.setTradeTime(LocalDateTime.now());
        virtualTransFlowEntity.setDictBizType(transactionDTO.getDictBizType());
        virtualTransFlowEntity.setSourceType(transactionDTO.getSourceType());
        virtualTransFlowEntity.setSourceId(transactionDTO.getSourceId());
        virtualTransFlowEntity.setSourceCode(transactionDTO.getSourceCode());
        virtualTransFlowEntity.setSourceDetailId(transactionDTO.getSourceDetailId());

        // 交易数量
        virtualTransFlowEntity.setQty(transactionDTO.getQty());

        // 交易人员信息
        virtualTransFlowEntity.setCreateUserId(transactionDTO.getUserId());
        virtualTransFlowEntity.setCreateUserName(transactionDTO.getUserName());
        virtualTransFlowEntity.setCreateTime(LocalDateTime.now());

        virtualTransFlowEntity.setUpdateUserId(transactionDTO.getUserId());
        virtualTransFlowEntity.setUpdateUserName(transactionDTO.getUserName());
        virtualTransFlowEntity.setUpdateTime(LocalDateTime.now());

        virtualTransFlowEntity.setUserId(transactionDTO.getUserId());
        virtualTransFlowEntity.setTradeTime(LocalDateTime.now());
        virtualTransFlowEntity.setOperationMode("approve");

        virtualTransFlowService.save(virtualTransFlowEntity);
        transactionDTO.setId(virtualTransFlowEntity.getId());

        //可用入库时，生成库龄流水
        if (InventoryStatusEnum.USABLE.getCode().equals(transactionDTO.getInventoryStatus()) && transactionDTO.getQty() > 0) {
            //仅分货单计入库龄流水
            if (!CharSequenceUtil.equals(InventorySourceTypeEnum.VIRTUAL_WAREHOUSE_ALLOCATION.getCode(),transactionDTO.getSourceType())) {
                return;
            }
            //入库同步生成库龄数据
            virtualTransFlowDetailService.updateHandleVirtualTransFlow(virtualTransFlowEntity);
        }

        //出库添加本地任务表数据
        if ((0 > transactionDTO.getQty() && InventoryStatusEnum.USABLE.getCode().equals(transactionDTO.getInventoryStatus())
                && (CharSequenceUtil.equals(transactionDTO.getSourceType(),InventorySourceTypeEnum.VIRTUAL_WAREHOUSE_ALLOCATION.getCode())
                || CharSequenceUtil.equals(transactionDTO.getSourceType(),InventorySourceTypeEnum.SO_OUTSTOCK.getCode())))
                || (0 > transactionDTO.getQty() && InventoryStatusEnum.FROZEN.getCode().equals(transactionDTO.getInventoryStatus())
                && (CharSequenceUtil.equals(transactionDTO.getSourceType(),InventorySourceTypeEnum.MACHINE_INFO.getCode())
                || CharSequenceUtil.equals(transactionDTO.getSourceType(),InventorySourceTypeEnum.TRANSFER_INFO.getCode())
                || CharSequenceUtil.equals(transactionDTO.getSourceType(),InventorySourceTypeEnum.SO_OUTSTOCK.getCode())))) {
            addWmsVirtualDetailMsg(virtualTransFlowEntity);
        }
    }

    /**
     * 添加本地任务
     * @author will
     * @date 2024/12/9 18:28
     * @param transFlowEntity
     */
    private void addWmsVirtualDetailMsg (VirtualTransFlowEntity transFlowEntity) {
        //入库添加本地任务表数据
        WmsVirtualDetailMsgDTO.AddDTO addDTO = new WmsVirtualDetailMsgDTO.AddDTO();
        addDTO.setRemark("虚拟仓库存出入库");
        addDTO.setTradeTime(transFlowEntity.getTradeTime());
        addDTO.setStatus(VirtualDetailMsgStatusEnum.WAIT_HANDLE.getCode());
        addDTO.setBusinessId(transFlowEntity.getId());
        wmsVirtualDetailMsgService.add(addDTO);
    }

    /**
     * 获取最后一次交易记录的剩余库存数量
     * @param transactionDTO   交易记录
     * @return  最后一次交易记录的剩余库存数量
     */
    private int getLastTransactionInventoryQty(VirtualInventoryStockDTO.InventoryTransactionDTO transactionDTO) {
        int initInventoryQty = 0;
        LambdaQueryWrapper<VirtualTransFlowEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(VirtualTransFlowEntity::getVirtualInventoryId, transactionDTO.getVirtualInventoryId())
                .le(VirtualTransFlowEntity::getBillDate, transactionDTO.getBillDate())
                .eq(VirtualTransFlowEntity::getIsUnapproved, false)
                .orderByDesc(VirtualTransFlowEntity::getBillDate)
                .orderByDesc(VirtualTransFlowEntity::getId)
                .last("limit 1");

        VirtualTransFlowEntity transactionFlow = virtualTransFlowService.getOne(queryWrapper);
        return null == transactionFlow ? initInventoryQty : transactionFlow.getCurInventoryQty();
    }

    /**
     * 更新库存
     * @param transactionDTO    库存交易信息
     */
    private void setInventoryId(VirtualInventoryStockDTO.InventoryTransactionDTO transactionDTO) {
        VirtualInventoryEntity virtualInventoryEntity;
        if(null != transactionDTO.getVirtualInventoryId()) {
            virtualInventoryEntity = virtualInventoryService.getById(transactionDTO.getVirtualInventoryId());
        }else{
            virtualInventoryEntity = virtualInventoryService.getByTransaction(transactionDTO);
        }

        log.info("####VirtualInventoryTradingServiceImpl===>updateVirtualInventory====>virtualInventoryEntity = {}  transactionDTO={}", JSON.toJSONString(virtualInventoryEntity), JSON.toJSONString(transactionDTO));
        if(null == virtualInventoryEntity || null == virtualInventoryEntity.getId()) {
            log.warn("####VirtualInventoryTradingServiceImpl===>updateVirtualInventory====>virtualInventoryEntity = {}  transactionDTO={}", JSON.toJSONString(virtualInventoryEntity), JSON.toJSONString(transactionDTO));
            throw new ServiceException(ApiError.ERROR_INVENTORY_NOT_EXIST, transactionDTO.getWarehouseName(), transactionDTO.getSkuNo(), transactionDTO.getInventoryStatusName());
        }
        
        if(null == transactionDTO.getVirtualInventoryId()) {
            //方面后续记录流水与历史库存
            transactionDTO.setVirtualInventoryId(virtualInventoryEntity.getId());
        }

    }

    /**
     * 更新库存历史
     * @param transactionDTO    库存交易信息
     */
    private void updateInventoryHis(VirtualInventoryStockDTO.InventoryTransactionDTO transactionDTO) {
        if(null == transactionDTO.getVirtualInventoryId()) {
            ServiceException.runError("sku:[{}]仓库:[{}]虚拟仓:[{}]库存状态：[{}],inventory_id为空,请让【实施工程师】协调开发人员处理",
                    transactionDTO.getSkuNo(),transactionDTO.getWarehouseName(),transactionDTO.getVirtualWarehouseName(),transactionDTO.getInventoryStatusName());
        }

        // 查询当天历史库存
        saveInventoryCurrentday(transactionDTO);

        // 更新当天之后的历史库存
        LambdaUpdateWrapper<VirtualInventoryHisEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.setSql("qty = qty + " + transactionDTO.getQty())
                .set(VirtualInventoryHisEntity::getUpdateTime, LocalDateTime.now())
                .set(VirtualInventoryHisEntity::getUpdateUserId, transactionDTO.getUserId())
                .set(VirtualInventoryHisEntity::getUpdateUserName, transactionDTO.getUserName())
                //条件
                .eq(VirtualInventoryHisEntity::getVirtualInventoryId, transactionDTO.getVirtualInventoryId())
                .gt(VirtualInventoryHisEntity::getDate, transactionDTO.getBillDate());

        virtualInventoryHisService.update(wrapper);
    }


    /**
     * 保存当天历史库存
     * @param transactionDTO    库存交易信息
     */
    private void saveInventoryCurrentday(VirtualInventoryStockDTO.InventoryTransactionDTO transactionDTO) {
        VirtualInventoryHisEntity inventoryHis = this.queryInventoryHisLast(transactionDTO.getVirtualInventoryId(), transactionDTO.getBillDate(),true);

        if(null == inventoryHis) {
            // 查询当天以前的库存
            inventoryHis = this.queryInventoryHisLast(transactionDTO.getVirtualInventoryId(), transactionDTO.getBillDate(),false);
            int inventoryQty = (null == inventoryHis) ? 0 : inventoryHis.getQty();

            inventoryHis = new VirtualInventoryHisEntity();
            inventoryHis.setVirtualInventoryId(transactionDTO.getVirtualInventoryId());
            inventoryHis.setDate(transactionDTO.getBillDate());
            inventoryHis.setQty(inventoryQty+ transactionDTO.getQty());
            inventoryHis.setCreateUserId(transactionDTO.getUserId());
            inventoryHis.setCreateUserName(transactionDTO.getUserName());
            inventoryHis.setCreateTime(LocalDateTime.now());
            inventoryHis.setUpdateUserId(transactionDTO.getUserId());
            inventoryHis.setUpdateUserName(transactionDTO.getUserName());
            inventoryHis.setUpdateTime(LocalDateTime.now());

            virtualInventoryHisService.save(inventoryHis);
        }else {
            // 更新当天历史库存
            LambdaUpdateWrapper<VirtualInventoryHisEntity> wrapper = new LambdaUpdateWrapper<>();
            wrapper.setSql("qty = qty + " + transactionDTO.getQty())
                    .set(VirtualInventoryHisEntity::getUpdateTime, LocalDateTime.now())
                    .set(VirtualInventoryHisEntity::getUpdateUserId, transactionDTO.getUserId())
                    .set(VirtualInventoryHisEntity::getUpdateUserName, transactionDTO.getUserName())
                    //条件
                    .eq(VirtualInventoryHisEntity::getId, inventoryHis.getId());

            virtualInventoryHisService.update(wrapper);
        }
    }

    /**
     * 查询库存历史
     * @param inventoryId   库存id
     * @param billDate      交易日期
     * @param isOnlyCurrBillDate   是否只查询当天的历史
     * @return  库存历史
     */
    private VirtualInventoryHisEntity queryInventoryHisLast(String inventoryId, LocalDate billDate, boolean isOnlyCurrBillDate) {
        LambdaQueryWrapper<VirtualInventoryHisEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(VirtualInventoryHisEntity::getVirtualInventoryId, inventoryId)
                .orderByDesc(VirtualInventoryHisEntity::getDate)
                .orderByDesc(VirtualInventoryHisEntity::getId)
                .last("limit 1");
        if(isOnlyCurrBillDate) {
            queryWrapper.eq(VirtualInventoryHisEntity::getDate, billDate);
        }else{
            queryWrapper.lt(VirtualInventoryHisEntity::getDate, billDate);
        }
        return virtualInventoryHisService.getOne(queryWrapper);

    }


    /**
     * 检查所有库存交易，库存是否充足
     * @param transactionList   交易记录列表
     */
    private void checkVirtualInventoryList(List<VirtualInventoryStockDTO.InventoryTransactionDTO> transactionList) {
        Map<String, VirtualInventoryStockDTO.InventoryTransactionDTO> checkMap = new HashMap<>();
        for (VirtualInventoryStockDTO.InventoryTransactionDTO item : transactionList) {
            // 未找到inventory_id的数据使用仓库id，skuID，仓位，库存状态作为唯一键进行合并。
            String mapKey = CharSequenceUtil.isBlank(item.getVirtualInventoryId()) ?
                    CharSequenceUtil.format("{}_{}_{}_{}", item.getWarehouseId(), item.getSkuId(),item.getVirtualWarehouseId(),item.getInventoryStatus()) :
                    item.getVirtualInventoryId();
            if(!checkMap.containsKey(mapKey)){
                VirtualInventoryStockDTO.InventoryTransactionDTO itemCopy = new VirtualInventoryStockDTO.InventoryTransactionDTO();
                BeanUtil.copyProperties(item, itemCopy);
                checkMap.put(mapKey, itemCopy);
            }else {
                // 合并数量
                VirtualInventoryStockDTO.InventoryTransactionDTO inventoryTransactionDTO = checkMap.get(mapKey);
                inventoryTransactionDTO.setQty(inventoryTransactionDTO.getQty() + item.getQty());
                checkMap.put(mapKey, inventoryTransactionDTO);
            }
        }
        List<VirtualInventoryStockDTO.InventoryTransactionDTO> checkList = new ArrayList<>(checkMap.values());
        StringBuilder errList = new StringBuilder();
        for(VirtualInventoryStockDTO.InventoryTransactionDTO transactionDTO : checkList) {
            errList.append(checkInventory(transactionDTO));
        }

        if(errList.length() > 0) {
            ServiceException.runError(errList.toString());
        }
    }

    /**
     * 检查库存是否充足
     * @param transactionDTO    库存交易信息
     */
    private String checkInventory(VirtualInventoryStockDTO.InventoryTransactionDTO transactionDTO) {
        int inventoryQty = 0;

        if(transactionDTO.isAllowNegativeInventory()) {
            return "";
        }

        if(null != transactionDTO.getVirtualInventoryId()) {
            VirtualInventoryEntity virtualInventoryEntity = virtualInventoryService.getById(transactionDTO.getVirtualInventoryId());
            inventoryQty = (null == virtualInventoryEntity ? 0 : virtualInventoryEntity.getQty());

        }
        if(inventoryQty + transactionDTO.getQty() < 0) {
            return CharSequenceUtil.format("库存不足：sku=[{}],仓库=[{}],虚拟仓=[{}],库存状态=[{}],库存:{},交易数:{},缺少数：{}\n"
                    , transactionDTO.getSkuNo()
                    , transactionDTO.getWarehouseName()
                    , transactionDTO.getVirtualWarehouseName()
                    , transactionDTO.getInventoryStatusName()
                    , inventoryQty
                    , transactionDTO.getQty()
                    , -(inventoryQty + transactionDTO.getQty())
            );
        }
        return "";
    }


    /**
     * 排序
     * @param transactionList   交易记录列表
     * @return  排序后的交易记录列表
     */
    private List<VirtualInventoryStockDTO.InventoryTransactionDTO> sortVirtualInventoryTransactionList(List<VirtualInventoryStockDTO.InventoryTransactionDTO> transactionList) {
        Comparator<VirtualInventoryStockDTO.InventoryTransactionDTO> comparing = Comparator.comparing(VirtualInventoryStockDTO.InventoryTransactionDTO::getSkuId)
                .thenComparing(VirtualInventoryStockDTO.InventoryTransactionDTO::getWarehouseId)
                .thenComparing(VirtualInventoryStockDTO.InventoryTransactionDTO::getVirtualWarehouseId)
                .thenComparing(x -> CharSequenceUtil.isNotEmpty(x.getInventoryStatus()) ? x.getInventoryStatus() : "");

        transactionList = transactionList.stream().sorted(comparing).collect(Collectors.toList());
        return transactionList;
    }
    
    private void addInventoryTransaction(List<VirtualInventoryStockDTO.InventoryTransactionDTO> transactionList, String approveType) {
    	virtualInventoryTransactionService.addInventoryTransaction(transactionList, approveType);
	}
}
