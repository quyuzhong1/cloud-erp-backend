package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.ValidatorUtil;
import com.erp.model.wms.entity.InventoryHisEntity;
import com.erp.model.wms.entity.TransactionFlowEntity;
import com.erp.server.wms.mapper.InventoryHisMapper;
import com.erp.server.wms.service.InventoryHisService;
import com.xxl.job.core.context.XxlJobHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @CreateTime: 2023-04-27  17:04
 * @Author: zhangchunlin
 */
@Slf4j
@Service
public class InventoryHisServiceImpl extends SuperServiceImpl<InventoryHisMapper, InventoryHisEntity> implements InventoryHisService {


    @Override
    public InventoryHisEntity findInventory(String infoId, LocalDate billDate) {
        LambdaQueryWrapper<InventoryHisEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(InventoryHisEntity::getInfoId, infoId)
                .eq(InventoryHisEntity::getBillDate, billDate)
                .last("limit 1");
        return baseMapper.selectOne(queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int updateQtyById(String id, Integer qty) {
        LoginUser loginUser =  UserContext.getDefaultLoginUser();
        return baseMapper.updateQtyById(id, qty, LocalDateTime.now(), loginUser.getUid(), loginUser.getUserName());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addOrUpdate(String inventoryInfoId, LocalDate billDate, Integer qty) {
        InventoryHisEntity inventoryHis = this.findInventory(inventoryInfoId, billDate);
        if (Objects.isNull(inventoryHis)) {
            inventoryHis = new InventoryHisEntity();
            inventoryHis.setInfoId(inventoryInfoId);
            inventoryHis.setBillDate(billDate);
            inventoryHis.setQty(qty);
            inventoryHis.setVersion(1);
            boolean save = super.save(inventoryHis);
            ValidatorUtil.isTrue(save, ()->new ServiceException("库存数据保存失败"));
        } else {
            int updateCnt = this.updateQtyById(inventoryHis.getId(), qty);
            if(updateCnt != 1) {
                throw new ServiceException(ApiError.ERROR_1027);
            }
        }
    }

    @Override
    public InventoryHisEntity findLastInventory(String inventoryId, LocalDate localDate) {
        LambdaQueryWrapper<InventoryHisEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(InventoryHisEntity::getInfoId, inventoryId)
                .le(InventoryHisEntity::getBillDate, localDate)
                .orderByDesc(InventoryHisEntity::getBillDate)
                .last("limit 1");
        return baseMapper.selectOne(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void overrideInventoryHis(List<TransactionFlowEntity> flowList, InventoryHisEntity hisEntity) {
        // 查询历史库存
        List<InventoryHisEntity> lockInventoryHisList = lambdaQuery()
                .gt(InventoryHisEntity::getBillDate, hisEntity.getBillDate())
                .eq(InventoryHisEntity::getInfoId, hisEntity.getInfoId())
                .orderByAsc(InventoryHisEntity::getBillDate)
                .last("for update")
                .list();
        if(CollUtil.isEmpty(lockInventoryHisList)){
            log.info("需要修复历史库存列表为空！hisEntity={}", JSONUtil.toJsonStr(hisEntity));
            XxlJobHelper.log("需要修复历史库存列表为空！");
            return;
        }
        Map<String, InventoryHisEntity> inventoryHisMap = lockInventoryHisList
                .stream()
                .collect(Collectors.toMap(item -> CharSequenceUtil.format("{}_{}", item.getInfoId(), item.getBillDate()), item -> item));

        // 按单据日期统计流水
        Map<String, Integer> flowBillDataMap = flowList
                .stream()
                .sorted(Comparator.comparing(TransactionFlowEntity::getBillDate))
                .collect(Collectors.groupingBy(item -> CharSequenceUtil.format("{}_{}", item.getInventoryId(), item.getBillDate()),
                        Collectors.summingInt(TransactionFlowEntity::getQty)));
        // 遍历统计流水日期
        AtomicReference<Integer> curQty = new AtomicReference<>(hisEntity.getQty());
        List<InventoryHisEntity> insertList = new ArrayList<>();
        List<InventoryHisEntity> updateList = new ArrayList<>();
        flowBillDataMap.keySet().stream().sorted().forEach(key -> {
//            threadPoolTaskExecutor.execute();
                // 当天单据日期合计变更数量
                Integer tradeQty = flowBillDataMap.get(key);
                // 查询是否存在历史库存记录
                InventoryHisEntity inventoryHis = inventoryHisMap.get(key);
                curQty.updateAndGet(v -> v + tradeQty);
                if(ObjectUtil.isEmpty(inventoryHis)){
                    String billDateStr = key.split("_")[1];
                    String inventoryId = key.split("_")[0];
                    insertList.add(new InventoryHisEntity(inventoryId, LocalDate.parse(billDateStr), curQty.get()));
                }else {
                    updateList.add(new InventoryHisEntity(inventoryHis.getId(), curQty.get()));
                }
                });
        if(CollUtil.isNotEmpty(insertList)){
            saveBatch(insertList);
        }
        if(CollUtil.isNotEmpty(updateList)){
            updateBatchById(updateList);
        }
        // 查找 inventoryHisMap 中不包含在 flowBillDataMap 中的 key
        List<String> removeHisIdList = inventoryHisMap.entrySet().stream()
                .filter(entry -> !flowBillDataMap.containsKey(entry.getKey()))
                .map(item -> item.getValue().getId())
                // 过滤掉null值
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        // 删除不需要的历史库存
        if(CollUtil.isNotEmpty(removeHisIdList)){
            removeByIds(removeHisIdList);
        }
    }


}