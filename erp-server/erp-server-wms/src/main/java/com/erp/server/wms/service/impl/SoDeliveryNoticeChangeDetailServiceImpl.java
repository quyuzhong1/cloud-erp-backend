package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.OperateLogDTO;
import com.erp.model.wms.dto.SoDeliveryNoticeChangeDTO;
import com.erp.model.wms.entity.PickingDetailEntity;
import com.erp.model.wms.entity.SoDeliveryNoticeChangeDetailEntity;
import com.erp.model.wms.entity.SoDeliveryNoticeChangeEntity;
import com.erp.model.wms.entity.SoDeliveryNoticeDetailEntity;
import com.erp.model.wms.enums.SoDeliveryNoticeChangeTypeEnum;
import com.erp.server.wms.mapper.SoDeliveryNoticeChangeDetailMapper;
import com.erp.server.wms.service.*;
import io.seata.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

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
        this.checkData(addDTO.getViewDetailList());
        List<SoDeliveryNoticeChangeDetailEntity> detailEntityList = this.buildDetail(addDTO.getViewDetailList(),soDeliveryNoticeChangeEntity);
        this.saveBatch(detailEntityList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SoDeliveryNoticeChangeDTO.ViewDTO addDTO,SoDeliveryNoticeChangeEntity soDeliveryNoticeChangeEntity) {
        this.checkData(addDTO.getViewDetailList());
        List<SoDeliveryNoticeChangeDetailEntity> dbDetailList = this.listByMainIds(Arrays.asList(addDTO.getId()));
        List<SoDeliveryNoticeChangeDTO.ViewDetail> detailViewList = addDTO.getViewDetailList();
        List<String> updateViewIds = detailViewList.stream().map(v->v.getDetailId()).collect(Collectors.toList());
        List<SoDeliveryNoticeChangeDTO.ViewDetail> addViewList = detailViewList.stream().filter(v-> StringUtils.isBlank(v.getDetailId())).collect(Collectors.toList());

        List<SoDeliveryNoticeChangeDetailEntity> addList = this.buildDetail(addViewList,soDeliveryNoticeChangeEntity);
        List<SoDeliveryNoticeChangeDetailEntity> updateList = new ArrayList<>();
        List<SoDeliveryNoticeChangeDetailEntity> deleteList = dbDetailList.stream().filter(v->!updateViewIds.contains(v.getId())).collect(Collectors.toList());

        List<OperateLogDTO.AddModuleOperateLogDTO> operateLogList = new ArrayList<>();
        for (SoDeliveryNoticeChangeDTO.ViewDetail viewDetail : detailViewList) {
            SoDeliveryNoticeChangeDetailEntity dbEntity = dbDetailList.stream().filter(v->v.getId().equals(viewDetail.getDetailId())).findFirst().orElse(null);
            if(Objects.isNull(dbEntity)){
                continue;
            }
            if(dbEntity.getSkuNo().equals(viewDetail.getSkuNo())){
                operateLogList.add(new OperateLogDTO.AddModuleOperateLogDTO(StrUtil.format("编辑了SKU的新发货通知sku【{}】数量从【{}】为【{}】",dbEntity.getSkuNo(),dbEntity.getNewQty(),viewDetail.getNewNoticeQty()), ModuleTypeEnum.DELIVERY_NOTICE_CHANGE.getCode(),dbEntity.getMainId(),"编辑操作"));
            }else{
                operateLogList.add(new OperateLogDTO.AddModuleOperateLogDTO(StrUtil.format("编辑了SKU的新发货通知sku从【{}】为【{}】,数量从【{}】为【{}】",dbEntity.getSkuNo(),viewDetail.getSkuNo(),dbEntity.getNewQty(),viewDetail.getNewNoticeQty()), ModuleTypeEnum.DELIVERY_NOTICE_CHANGE.getCode(),dbEntity.getMainId(),"编辑操作"));
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
        addList.forEach(v->{
            operateLogList.add(new OperateLogDTO.AddModuleOperateLogDTO(StrUtil.format("新增一行sku{}",v.getSkuNo()), ModuleTypeEnum.DELIVERY_NOTICE_CHANGE.getCode(),v.getMainId(),"编辑操作"));
        });
        deleteList.forEach(v->{
            operateLogList.add(new OperateLogDTO.AddModuleOperateLogDTO(StrUtil.format("删除一行sku{}",v.getSkuNo()), ModuleTypeEnum.DELIVERY_NOTICE_CHANGE.getCode(),v.getMainId(),"编辑操作"));
        });

        this.checkData(addDTO.getViewDetailList());
        this.update(addList,updateList,deleteList);
        operateLogService.batchAddModuleOperateLog(operateLogList);
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

    @Override
    public List<SoDeliveryNoticeChangeDetailEntity> listByMainId(String id) {
        if(StringUtils.isBlank(id)){
            return Collections.EMPTY_LIST;
        }
        return lambdaQuery().eq(SoDeliveryNoticeChangeDetailEntity::getMainId,id).list();
    }

    /**
     * 校验明细数据
     */
    @Override
    public void checkData( List<SoDeliveryNoticeChangeDTO.ViewDetail> viewDetailList) {
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
                if(viewDetail.getNewNoticeQty() < pickedQty){
                    throw new ServiceException("【{}】发货数量【{}】不能小于已拣货数量【{}】",viewDetail.getSkuNo(),viewDetail.getNewNoticeQty(),pickedQty);
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
