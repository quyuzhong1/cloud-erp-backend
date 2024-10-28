package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.wms.dto.SoDeliveryNoticeChangeDTO;
import com.erp.model.wms.entity.PickingDetailEntity;
import com.erp.model.wms.entity.SoDeliveryNoticeChangeDetailEntity;
import com.erp.model.wms.entity.SoDeliveryNoticeChangeEntity;
import com.erp.model.wms.entity.SoDeliveryNoticeDetailEntity;
import com.erp.model.wms.enums.SoDeliveryNoticeChangeTypeEnum;
import com.erp.server.wms.mapper.SoDeliveryNoticeChangeDetailMapper;
import com.erp.server.wms.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import io.seata.common.util.StringUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.ehcache.impl.internal.store.heap.holders.SerializedOnHeapValueHolder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.SoDeliveryNoticeChangeDetailDTO;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;

/**
 * <p>
 * 发货通知变更单明细 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2024-10-23
 */
@Slf4j
@Service
public class SoDeliveryNoticeChangeDetailServiceImpl extends SuperServiceImpl<SoDeliveryNoticeChangeDetailMapper, SoDeliveryNoticeChangeDetailEntity> implements SoDeliveryNoticeChangeDetailService {
    @Autowired
    private OperateLogService operateLogService;

    @Resource
    private SoDeliveryNoticeService soDeliveryNoticeService;

    @Resource
    private SoDeliveryNoticeDetailService soDeliveryNoticeDetailService;

    @Resource
    private PickingDetailService pickingDetailService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(SoDeliveryNoticeChangeDTO.ViewDTO addDTO, SoDeliveryNoticeChangeEntity soDeliveryNoticeChangeEntity) {
        this.checkData(addDTO.getViewDetailList(),soDeliveryNoticeChangeEntity);
        List<SoDeliveryNoticeChangeDetailEntity> detailEntityList = this.buildDetail(addDTO.getViewDetailList(),soDeliveryNoticeChangeEntity);
        this.saveBatch(detailEntityList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SoDeliveryNoticeChangeDTO.ViewDTO addDTO,SoDeliveryNoticeChangeEntity soDeliveryNoticeChangeEntity) {
        this.checkData(addDTO.getViewDetailList(),soDeliveryNoticeChangeEntity);
        List<SoDeliveryNoticeChangeDetailEntity> dbDetailList = this.listByMainIds(Arrays.asList(addDTO.getId()));
        List<SoDeliveryNoticeChangeDTO.ViewDetail> detailViewList = addDTO.getViewDetailList();
        List<String> updateViewIds = detailViewList.stream().map(v->v.getDetailId()).collect(Collectors.toList());
        List<SoDeliveryNoticeChangeDTO.ViewDetail> addViewList = detailViewList.stream().filter(v-> StringUtils.isBlank(v.getDetailId())).collect(Collectors.toList());

        List<SoDeliveryNoticeChangeDetailEntity> addList = this.buildDetail(addViewList,soDeliveryNoticeChangeEntity);
        List<SoDeliveryNoticeChangeDetailEntity> updateList = new ArrayList<>();
        List<SoDeliveryNoticeChangeDetailEntity> deleteList = dbDetailList.stream().filter(v->!updateViewIds.contains(v.getId())).collect(Collectors.toList());

        for (SoDeliveryNoticeChangeDTO.ViewDetail viewDetail : detailViewList) {
            SoDeliveryNoticeChangeDetailEntity dbEntity = dbDetailList.stream().filter(v->v.getId().equals(viewDetail.getDetailId())).findFirst().orElse(null);
            if(Objects.isNull(dbEntity)){
                continue;
            }
            dbEntity.setSkuId(viewDetail.getSkuId());
            dbEntity.setSkuNo(viewDetail.getSkuNo());
            dbEntity.setSourceDetailId(viewDetail.getSourceDetailId());
            dbEntity.setChangeType(viewDetail.getChangeType());
            dbEntity.setNewQty(viewDetail.getNewNoticeQty());
            dbEntity.setProductName(viewDetail.getProductName());
            dbEntity.setSoDetailId(viewDetail.getSoDetailId());
            updateList.add(dbEntity);
        }
        this.update(addList,updateList,deleteList);
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(List<SoDeliveryNoticeChangeDetailEntity> addList, List<SoDeliveryNoticeChangeDetailEntity> updateList, List<SoDeliveryNoticeChangeDetailEntity> deleteList) {
        if(CollectionUtils.isNotEmpty(addList)){
            this.saveBatch(addList);
        }
        if(CollectionUtils.isNotEmpty(updateList)){
            this.updateBatchById(updateList);
        }
        if(CollectionUtils.isNotEmpty(deleteList)){
            List<String> deleteIds = deleteList.stream().map(v->v.getId()).collect(Collectors.toList());
            this.removeByIds(deleteIds);
        }
    }

    @Override
    public List<SoDeliveryNoticeChangeDetailEntity> listByMainIds(List<String> mainIds) {
        if(CollectionUtils.isEmpty(mainIds)){
            return new ArrayList<>();
        }
        return lambdaQuery().in(SoDeliveryNoticeChangeDetailEntity::getMainId,mainIds).list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByMainId(String id) {
        if(StringUtils.isBlank(id)){
            return;
        }
        this.lambdaUpdate().eq(SoDeliveryNoticeChangeDetailEntity::getMainId,id).remove();
    }

    /**
     * 校验明细数据
     * @param soDeliveryNoticeChangeEntity
     */
    private void checkData( List<SoDeliveryNoticeChangeDTO.ViewDetail> viewDetailList, SoDeliveryNoticeChangeEntity soDeliveryNoticeChangeEntity) {
        List<String> noticeDetailIds = viewDetailList.stream().map(v->v.getSourceDetailId()).collect(Collectors.toList());
        List<SoDeliveryNoticeDetailEntity> soDeliveryNoticeDetailEntityList = soDeliveryNoticeDetailService.listByIds(noticeDetailIds);
        List<PickingDetailEntity> pickingDetailEntityList = pickingDetailService.listPickingDetailBySourceDetailIds(noticeDetailIds);
        for (SoDeliveryNoticeChangeDTO.ViewDetail viewDetail : viewDetailList) {
            if(Objects.nonNull(viewDetail.getNewNoticeQty()) && viewDetail.getNewNoticeQty() > viewDetail.getMaxCanChangeQty()){
                throw new ServiceException("{} 变更数量不能大于可变更数量",viewDetail.getSkuNo());
            }
            if(SoDeliveryNoticeChangeTypeEnum.UPDATE.getCode().equals(viewDetail.getChangeType())){
                SoDeliveryNoticeDetailEntity soDeliveryNoticeDetailEntity = soDeliveryNoticeDetailEntityList.stream().filter(v->v.getId().equals(viewDetail.getSourceDetailId())).findFirst().orElseThrow(()->new ServiceException("变更明细数据错误"));
                if(viewDetail.getNewNoticeQty() > soDeliveryNoticeDetailEntity.getDeliveryQty()){
                    continue;
                }
                List<PickingDetailEntity> currentPickList = pickingDetailEntityList.stream().filter(v -> v.getSourceDetailId().equals(viewDetail.getSourceDetailId())).collect(Collectors.toList());
                Integer pickedQty = currentPickList.stream().mapToInt(PickingDetailEntity::getPickedQty).sum();
                if(viewDetail.getNewNoticeQty() > pickedQty){
                    throw new ServiceException("【{}】发货数量不能小于已拣货数量【{}】",viewDetail.getSkuNo(),pickedQty);
                }
            }else if(SoDeliveryNoticeChangeTypeEnum.ADD.getCode().equals(viewDetail.getChangeType())){

            }else if(SoDeliveryNoticeChangeTypeEnum.DELETE.getCode().equals(viewDetail.getChangeType())){
                List<PickingDetailEntity> currentPickList = pickingDetailEntityList.stream().filter(v -> v.getSourceDetailId().equals(viewDetail.getSourceDetailId())).collect(Collectors.toList());
                if(CollectionUtils.isNotEmpty(currentPickList)){
                    Integer pickedQty = currentPickList.stream().mapToInt(PickingDetailEntity::getPickedQty).sum();
                    throw new ServiceException("【{}】已拣货【{}】，不允许删除",viewDetail.getSkuNo(),pickedQty);
                }
            }else {
                throw new ServiceException("变更类型错误");
            }
        }
    }

    private List<SoDeliveryNoticeChangeDetailEntity> buildDetail(List<SoDeliveryNoticeChangeDTO.ViewDetail> viewDetailList, SoDeliveryNoticeChangeEntity soDeliveryNoticeChangeEntity) {
        List<SoDeliveryNoticeChangeDetailEntity> list = new ArrayList<>();
        for (SoDeliveryNoticeChangeDTO.ViewDetail viewDetail : viewDetailList) {
            SoDeliveryNoticeChangeDetailEntity soDeliveryNoticeChangeDetailEntity = new SoDeliveryNoticeChangeDetailEntity();
            soDeliveryNoticeChangeDetailEntity.setMainId(soDeliveryNoticeChangeEntity.getId());
            soDeliveryNoticeChangeDetailEntity.setSkuId(viewDetail.getSkuId());
            soDeliveryNoticeChangeDetailEntity.setSkuNo(viewDetail.getSkuNo());
            soDeliveryNoticeChangeDetailEntity.setSourceDetailId(viewDetail.getSourceDetailId());
            soDeliveryNoticeChangeDetailEntity.setChangeType(viewDetail.getChangeType());
            soDeliveryNoticeChangeDetailEntity.setOriginQty(viewDetail.getCurrentNoticeQty());
            soDeliveryNoticeChangeDetailEntity.setNewQty(viewDetail.getNewNoticeQty());
            soDeliveryNoticeChangeDetailEntity.setProductName(viewDetail.getProductName());
            soDeliveryNoticeChangeDetailEntity.setSoDetailId(viewDetail.getSoDetailId());
            list.add(soDeliveryNoticeChangeDetailEntity);
        }
        return list;
    }
}
