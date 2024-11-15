package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.ValidatorUtil;
import com.erp.model.wms.entity.InventoryDetailEntity;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.server.wms.mapper.InventoryDetailMapper;
import com.erp.server.wms.service.InventoryDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Classname: InventoryDetailServiceImpl

 * @CreateTime: 2023-04-25  19:00
 * @Author: zhangchunlin
 */
@Slf4j
@Service
public class InventoryDetailServiceImpl extends SuperServiceImpl<InventoryDetailMapper, InventoryDetailEntity> implements InventoryDetailService {

    @Override
    public InventoryDetailEntity findOneDetail(String inventoryInfoId, LocalDate instockBatchDate, InventoryStatusEnum inventoryStatusEnum) {
        LambdaQueryWrapper<InventoryDetailEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(InventoryDetailEntity::getInfoId, inventoryInfoId);
        if (Objects.equals(inventoryStatusEnum, InventoryStatusEnum.IN_TRANSIT)) {
            queryWrapper.isNull(InventoryDetailEntity::getInstockBatchDate).last("limit 1");
        } else {
            queryWrapper.eq(InventoryDetailEntity::getInstockBatchDate, instockBatchDate).last("limit 1");
        }
        return baseMapper.selectOne(queryWrapper);
    }

    @Override
    public List<InventoryDetailEntity> findListQtyGreatZero(String inventoryInfoId) {
        List<InventoryDetailEntity> inventoryDetails = lambdaQuery().eq(InventoryDetailEntity::getInfoId, inventoryInfoId).gt(InventoryDetailEntity::getQty, 0).list();
        if(CollUtil.isNotEmpty(inventoryDetails)) {
            // 先按入库批次时间排序，再按id排序，防止时间冲突
            inventoryDetails = inventoryDetails.stream().sorted(Comparator.comparing(InventoryDetailEntity::getInstockBatchDate, Comparator.nullsFirst(LocalDate::compareTo)).thenComparing(InventoryDetailEntity::getId)).collect(Collectors.toList());

        }
        return inventoryDetails;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean updateQtyById(String id, Integer qty) {
//        LoginUser loginUser = UserContext.getDefaultLoginUser();
        boolean flag = lambdaUpdate()
                .setSql(CharSequenceUtil.format("{}={}+{}", "qty","qty", qty))
//                .setSql(CharSequenceUtil.format("{}={}+{}", "version","version", 1))
//                .setSql(StrUtils.isNotEmpty(loginUser.getUid()), CharSequenceUtil.format("update_user_id='{}'", loginUser.getUid()))
//                .setSql(StrUtils.isNotEmpty(loginUser.getUserName()), CharSequenceUtil.format("update_user_name='{}'", loginUser.getUserName()))
                .eq(InventoryDetailEntity::getId, id)
                .update(new InventoryDetailEntity());

        if(!flag) {
            throw new ServiceException(ApiError.ERROR_1027);
        }

        return flag;
        // return inventoryDetailMapper.updateQtyById(id, qty, version, LocalDateTime.now(), loginUser.getUid(), loginUser.getUserName());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public InventoryDetailEntity addOrUpdate(String inventoryInfoId, LocalDate billDate, Integer qty, InventoryStatusEnum inventoryStatusEnum) {
        // 入库批次日期取单据日期
        // 在途库存入库批次日期置为空
        LocalDate instockBatchDate = Objects.equals(InventoryStatusEnum.IN_TRANSIT, inventoryStatusEnum) ? null : billDate;
        InventoryDetailEntity inventoryDetail =  this.findOneDetail(inventoryInfoId, instockBatchDate, inventoryStatusEnum);
        if (Objects.isNull(inventoryDetail)) {
            log.info("单据日期：【{}】,批次日期：【{}】，库存表id：【{}】，不存在库存明细数据，新增数据", billDate, instockBatchDate, inventoryInfoId);
            inventoryDetail = new InventoryDetailEntity();
            inventoryDetail.setInfoId(inventoryInfoId);
            inventoryDetail.setInstockBatchDate(instockBatchDate);
            inventoryDetail.setQty(qty);
            inventoryDetail.setVersion(1);
            boolean save = super.save(inventoryDetail);
            ValidatorUtil.isTrue(save, ()->new ServiceException("库存数据保存失败"));
        } else {
            Integer originInventoryDetailQty = inventoryDetail.getQty(); // 库存明细原数量
            Integer afterInventoryDetailQty = originInventoryDetailQty + qty;
            log.info("单据日期：【{}】,库存表id：【{}】，原库存明细数量：【{}】，操作数量：【{}】，操作后库存明细数量：【{}】，修改库存明细数据", inventoryInfoId, billDate, originInventoryDetailQty, qty, afterInventoryDetailQty);
            // 更新库存明细数量
            boolean updateFlag = this.updateQtyById(inventoryDetail.getId(), qty);
            if(!updateFlag) {
                throw new ServiceException(ApiError.ERROR_1027);
            }
        }
        return inventoryDetail;
    }

    @Override
    public List<InventoryDetailEntity> findListQtyLeZero(String inventoryInfoId) {
        List<InventoryDetailEntity> inventoryDetails = lambdaQuery().eq(InventoryDetailEntity::getInfoId, inventoryInfoId).le(InventoryDetailEntity::getQty, 0).list();
        if(CollUtil.isNotEmpty(inventoryDetails)) {
            // 先按入库批次时间排序，再按id排序，防止时间冲突
            inventoryDetails = inventoryDetails.stream().sorted(Comparator.comparing(InventoryDetailEntity::getInstockBatchDate).thenComparing(InventoryDetailEntity::getId)).collect(Collectors.toList());

        }
        return inventoryDetails;
    }

    @Override
    public List<InventoryDetailEntity> listByFIFO(String infoId, Integer curQty, List<String> filterDetailIdList) {
        if(curQty >= 0){
            log.warn("需要扣减为[{}]，无需扣减，跳过查询操作！", curQty);
            return Collections.emptyList();
        }
        // 查询所有大于 0 的明细
        List<InventoryDetailEntity> listGreatZero = findListQtyGreatZero(infoId);
        if(CollUtil.isEmpty(listGreatZero)){
            return Collections.emptyList();
        }
        if(CollectionUtil.isNotEmpty(filterDetailIdList)){
            listGreatZero = listGreatZero.stream().filter(item -> filterDetailIdList.contains(item.getId())).collect(Collectors.toList());
        }
        // 循环扣减
        List<InventoryDetailEntity> waitOutList = new ArrayList<>();
        for (InventoryDetailEntity detail : listGreatZero) {
            if (curQty >= 0) {
                break;
            }
            Integer qty = detail.getQty();
            Integer tradeQty = (qty + curQty) < 0 ? -qty : curQty;
            curQty = qty + curQty;
            waitOutList.add(new InventoryDetailEntity(detail.getId(), tradeQty));
        }
        // 如果分摊了所有库存依旧无法完成足量扣减则直接返回空
        return waitOutList;
    }

}
