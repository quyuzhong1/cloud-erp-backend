package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.service.SuperServiceImpl;
import com.common.business.vo.LoginUser;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.StrUtils;
import com.common.core.utils.ValidatorUtil;
import com.erp.model.wms.entity.InventoryDetailEntity;
import com.erp.server.wms.mapper.InventoryDetailMapper;
import com.erp.server.wms.service.CommonService;
import com.erp.server.wms.service.InventoryDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Classname: InventoryDetailServiceImpl

 * @CreateTime: 2023-04-25  19:00
 * @Author: zhangchunlin
 */
@Slf4j
@Service
public class InventoryDetailServiceImpl extends SuperServiceImpl<InventoryDetailMapper, InventoryDetailEntity> implements InventoryDetailService {

    @Autowired
    private CommonService commonService;

    @Override
    public InventoryDetailEntity findOneDetail(String inventoryInfoId, LocalDate instockBatchDate) {
        LambdaQueryWrapper<InventoryDetailEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(InventoryDetailEntity::getInfoId, inventoryInfoId)
                .eq(InventoryDetailEntity::getInstockBatchDate, instockBatchDate).last("limit 1");
        return baseMapper.selectOne(queryWrapper);
    }

    @Override
    public List<InventoryDetailEntity> findListQtyGreatZero(String inventoryInfoId) {
        List<InventoryDetailEntity> inventoryDetails = lambdaQuery().eq(InventoryDetailEntity::getInfoId, inventoryInfoId).gt(InventoryDetailEntity::getQty, 0).list();
        if(CollUtil.isNotEmpty(inventoryDetails)) {
            // 先按入库批次时间排序，再按id排序，防止时间冲突
            inventoryDetails = inventoryDetails.stream().sorted(Comparator.comparing(InventoryDetailEntity::getInstockBatchDate).thenComparing(InventoryDetailEntity::getId)).collect(Collectors.toList());

        }
        return inventoryDetails;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean updateQtyById(String id, Integer qty) {
        LoginUser loginUser = commonService.getUserInfo();
        boolean flag = lambdaUpdate()
//                .set(InventoryDetailEntity::getQty, qty)
                .setSql(StrUtil.format("{}={}+{}", "qty","qty", qty))
//                .setSql(StrUtil.format("{}={}+{}", "version","version", 1))
//                .setSql(StrUtils.isNotEmpty(loginUser.getUid()), StrUtil.format("update_user_id='{}'", loginUser.getUid()))
//                .setSql(StrUtils.isNotEmpty(loginUser.getUserName()), StrUtil.format("update_user_name='{}'", loginUser.getUserName()))
                .eq(InventoryDetailEntity::getId, id)
                .update(new InventoryDetailEntity());
        return flag;
        // return inventoryDetailMapper.updateQtyById(id, qty, version, LocalDateTime.now(), loginUser.getUid(), loginUser.getUserName());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public InventoryDetailEntity addOrUpdate(String inventoryInfoId, LocalDate billDate, Integer qty) {
        // 入库批次日期取单据日期
        InventoryDetailEntity inventoryDetail =  this.findOneDetail(inventoryInfoId, billDate);
        if (Objects.isNull(inventoryDetail)) {
            log.info("单据日期：【{}】,库存表id：【{}】，不存在库存明细数据，新增数据", billDate, inventoryInfoId);
            inventoryDetail = new InventoryDetailEntity();
            inventoryDetail.setInfoId(inventoryInfoId);
            inventoryDetail.setInstockBatchDate(billDate);
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

}
