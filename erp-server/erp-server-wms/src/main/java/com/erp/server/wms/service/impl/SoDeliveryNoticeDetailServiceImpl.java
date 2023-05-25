package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.service.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.dto.SoDetailDTO;
import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.SoDeliveryNoticeDTO;
import com.erp.model.wms.dto.SoDeliveryNoticeDetailDTO;
import com.erp.model.wms.entity.SoDeliveryNoticeDetailEntity;
import com.erp.rpc.oms.feign.SoInfoFeign;
import com.erp.server.wms.mapper.SoDeliveryNoticeDetailMapper;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.SoDeliveryNoticeDetailService;
import com.erp.server.wms.service.SoOutstockDetailService;
import com.erp.server.wms.service.WmsAttachmentService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 发货通知单主表明细表 服务实现类
 * </p>
 *
 * @author LUO_WG
 * @since 2023-05-10
 */
@Service
public class SoDeliveryNoticeDetailServiceImpl extends SuperServiceImpl<SoDeliveryNoticeDetailMapper, SoDeliveryNoticeDetailEntity> implements SoDeliveryNoticeDetailService {
    @Resource
    private SoInfoFeign soInfoFeign;

    @Resource
    private WmsAttachmentService wmsAttachmentService;

    @Resource
    private OperateLogService operateLogService;


    @Resource
    private SoOutstockDetailService soOutstockDetailService;

    @Override
    public Boolean add(SoDeliveryNoticeDTO.Add dto, String id) {
        List<String> detailIds = dto.getDetailList().stream().map(SoDeliveryNoticeDetailDTO.Add::getSourceDetailId).collect(Collectors.toList());
        List<SoDetailEntity> soDetailEntitieList = soInfoFeign.listSoDetailByIds(detailIds);
        if (CollectionUtils.isEmpty(soDetailEntitieList)) {
            throw new ServiceException(ApiError.ERROR_92003);
        }
        List<SoDeliveryNoticeDetailEntity> detailEntityList = this.listDetailBySourceIds(detailIds);
        List<SoDeliveryNoticeDetailEntity> list = new ArrayList<>();
        for (SoDeliveryNoticeDetailDTO.Add detailDto : dto.getDetailList()) {
            SoDeliveryNoticeDetailEntity soDeliveryNoticeDetailEntity = new SoDeliveryNoticeDetailEntity();
            SoDetailEntity soDetailEntity = soDetailEntitieList.stream().filter(req -> req.getId().equals(detailDto.getSourceDetailId())).findFirst().orElse(new SoDetailEntity());
            Integer deliveryQty = detailEntityList.stream().filter(req -> req.getSourceDetailId().equals(detailDto.getSourceDetailId())).map(SoDeliveryNoticeDetailEntity::getDeliveryQty).reduce(MathUtil.ZERO, Integer::sum);
            if (soDetailEntity.getQty() < detailDto.getDeliveryQty() + deliveryQty) {
                throw new ServiceException(ApiError.ERROR_92009);
            }
            String idStr = IdWorker.getIdStr();
            soDeliveryNoticeDetailEntity.setId(idStr);
            soDeliveryNoticeDetailEntity.setMainId(id);
            soDeliveryNoticeDetailEntity.setSkuId(soDetailEntity.getSkuId());
            soDeliveryNoticeDetailEntity.setSkuNo(soDetailEntity.getSkuNo());
            soDeliveryNoticeDetailEntity.setDeliveryQty(detailDto.getDeliveryQty());
            soDeliveryNoticeDetailEntity.setIsClose(detailDto.getIsClose());
            soDeliveryNoticeDetailEntity.setRemark(detailDto.getRemark());
            soDeliveryNoticeDetailEntity.setSourceDetailId(detailDto.getSourceDetailId());

            Class<SoDeliveryNoticeDetailEntity> detailEntityClass = SoDeliveryNoticeDetailEntity.class;
            TableName tableName = detailEntityClass.getDeclaredAnnotation(TableName.class);
            //获取到表名
            String type = tableName.value();
            //保存附件
            wmsAttachmentService.batchSave(detailDto.getAttachUrlList(), detailDto.getAttachNameList(), type, idStr);

            list.add(soDeliveryNoticeDetailEntity);
        }
        return this.saveBatch(list);
    }

    @Override
    public Boolean update(SoDeliveryNoticeDTO.Update dto) {
        List<String> addList = dto.getDetailList().stream().filter(c -> StringUtils.isBlank(c.getId())).map(SoDeliveryNoticeDetailDTO.Update::getId).collect(Collectors.toList());
        List<String> detailIds = dto.getDetailList().stream().map(SoDeliveryNoticeDetailDTO.Update::getSourceDetailId).collect(Collectors.toList());
        List<SoDetailEntity> soDetailEntitieList = soInfoFeign.listSoDetailByIds(detailIds);
        if (CollectionUtils.isEmpty(soDetailEntitieList)) {
            throw new ServiceException(ApiError.ERROR_92003);
        }
        //原明细数据
        List<SoDeliveryNoticeDetailEntity> oldList = this.listDetailByMainId(dto.getId());
        List<String> deleteIds = getDeleteIds(dto.getDetailList(), oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            List<SoDeliveryNoticeDetailEntity> removeList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
            //操作日志
            List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(obj.getMainId(), obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("删除了一个SKU【%s】", ModuleTypeEnum.SO_DELIVERY_NOTICE.getCode(), pairList, "编辑操作");
            this.removeByIds(deleteIds);
        }

        List<SoDeliveryNoticeDetailEntity> list = new ArrayList<>();
        List<SoDeliveryNoticeDetailEntity> detailEntityList = this.listDetailBySourceDetailIds(detailIds);
        for (SoDeliveryNoticeDetailDTO.Update detailDto : dto.getDetailList()) {
            SoDeliveryNoticeDetailEntity soDeliveryNoticeDetailEntity = new SoDeliveryNoticeDetailEntity();
            SoDetailEntity soDetailEntity = soDetailEntitieList.stream().filter(req -> req.getId().equals(detailDto.getSourceDetailId())).findFirst().orElse(new SoDetailEntity());
            Integer deliveryQty = detailEntityList.stream().filter(req -> req.getSourceDetailId().equals(detailDto.getSourceDetailId())).map(SoDeliveryNoticeDetailEntity::getDeliveryQty).reduce(MathUtil.ZERO, Integer::sum);
            if (StringUtils.isNotBlank(detailDto.getId())) {
                soDeliveryNoticeDetailEntity.setId(detailDto.getId());
                deliveryQty = detailEntityList.stream().filter(req -> req.getSourceDetailId().equals(detailDto.getSourceDetailId()) && !req.getId().equals(detailDto.getId())).map(SoDeliveryNoticeDetailEntity::getDeliveryQty).reduce(MathUtil.ZERO, Integer::sum);
            } else {
                String idStr = IdWorker.getIdStr();
                soDeliveryNoticeDetailEntity.setId(idStr);
            }
            if (soDetailEntity.getQty() < detailDto.getDeliveryQty() + deliveryQty) {
                throw new ServiceException(ApiError.ERROR_92010);
            }
            soDeliveryNoticeDetailEntity.setMainId(dto.getId());
            soDeliveryNoticeDetailEntity.setSkuId(soDetailEntity.getSkuId());
            soDeliveryNoticeDetailEntity.setSkuNo(soDetailEntity.getSkuNo());
            detailDto.setSkuNo(soDetailEntity.getSkuNo());
            soDeliveryNoticeDetailEntity.setDeliveryQty(detailDto.getDeliveryQty());
            soDeliveryNoticeDetailEntity.setIsClose(detailDto.getIsClose());
            soDeliveryNoticeDetailEntity.setRemark(detailDto.getRemark());
            soDeliveryNoticeDetailEntity.setSourceDetailId(detailDto.getSourceDetailId());
            Class<SoDeliveryNoticeDetailEntity> detailEntityClass = SoDeliveryNoticeDetailEntity.class;
            TableName tableName = detailEntityClass.getDeclaredAnnotation(TableName.class);
            //获取到表名
            String type = tableName.value();
            //保存附件
            wmsAttachmentService.batchSaveNotDel(detailDto.getAttachUrlList(), detailDto.getAttachNameList(), type, soDeliveryNoticeDetailEntity.getId());
            //修改操作日志
            if (StringUtils.isNotBlank(detailDto.getId())) {
                SoDeliveryNoticeDetailEntity old = this.getById(soDeliveryNoticeDetailEntity.getId());
                if (ObjectUtils.isNotEmpty(old)) {
                    operateLogService.addModuleOperateLogByObj(old, soDeliveryNoticeDetailEntity, ModuleTypeEnum.SO_DELIVERY_NOTICE.getCode(), dto.getId(), "", String.format("【%s】", old.getSkuNo()));

                }
            }
            list.add(soDeliveryNoticeDetailEntity);
        }

        //添加操作日志
        if (CollectionUtils.isNotEmpty(addList)) {
            List<SoDeliveryNoticeDetailEntity> soDeliveryNoticeDetailEntities = this.listByIds(addList);
            List<Pair<String, String>> addPairList = soDeliveryNoticeDetailEntities.stream().map(obj -> new Pair<>(dto.getId(), obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("添加了一个SKU【%s】", ModuleTypeEnum.SO_DELIVERY_NOTICE.getCode(), addPairList, "编辑操作");
        }
        return this.saveOrUpdateBatch(list);
    }


    private List<String> getDeleteIds(List<SoDeliveryNoticeDetailDTO.Update> newList, List<SoDeliveryNoticeDetailEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(SoDeliveryNoticeDetailDTO.Update::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(SoDeliveryNoticeDetailEntity
                ::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }

    @Override
    public Boolean delete(List<String> mainIds) {
        return lambdaUpdate().set(SoDeliveryNoticeDetailEntity::getIsDeleted, Boolean.TRUE)
                .in(SoDeliveryNoticeDetailEntity::getMainId, mainIds)
                .remove();
    }

    @Override
    public List<SoDeliveryNoticeDetailEntity> listDetailByMainId(String mainId) {
        return lambdaQuery().eq(SoDeliveryNoticeDetailEntity::getMainId, mainId).list();
    }

    @Override
    public List<SoDeliveryNoticeDetailEntity> listDetailByMainIds(List<String> mainIdList) {
        if (CollectionUtils.isEmpty(mainIdList)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(SoDeliveryNoticeDetailEntity::getMainId, mainIdList).list();
    }

    @Override
    public List<SoDeliveryNoticeDetailEntity> listDetailBySourceIds(List<String> sourceIds) {
        return baseMapper.listDetailBySourceIds(sourceIds);
    }

    @Override
    public List<SoDeliveryNoticeDetailEntity> listDetailBySourceDetailIds(List<String> sourceDetailIds) {
        return baseMapper.listDetailBySourceDetailIds(sourceDetailIds);
    }


    /**
     * 处理数据 更改销售订单的发货状态
     *
     * @param ids
     * @return void
     * @author yl
     * @date 2023-05-23 10:02
     */
    @Override
    public void handleData(List<String> ids) {
        if (CollectionUtils.isNotEmpty(ids)) {
            List<SoDetailDTO.UpdateDeliveryStatusDTO> paramList = new ArrayList<>(ids.size());
            List<SoDeliveryNoticeDetailEntity> soDeliveryNoticeDetailList = this.listByIds(ids);
            for (SoDeliveryNoticeDetailEntity item : soDeliveryNoticeDetailList) {
                SoDetailDTO.UpdateDeliveryStatusDTO param = new SoDetailDTO.UpdateDeliveryStatusDTO();
                param.setId(item.getSourceDetailId());
                param.setDeliveryQty(item.getDeliveryQty());
                paramList.add(param);
            }
            soInfoFeign.updateDeliveryStatus(paramList);

        }


    }

    /**
     * 根据销售订单详情id 获取对应 下推的数据
     *
     * @param soDetailIds
     * @return java.lang.Integer
     * @author yl
     * @date 2023-05-25 10:30
     */
    @Override
    public Integer getPushDownBySoDetailIds(List<String> soDetailIds) {
        if (CollectionUtils.isEmpty(soDetailIds)) {
            return 0;
        }
        //发货通知的
        Integer deliveryNoticeCount = this.lambdaQuery().in(SoDeliveryNoticeDetailEntity::getSourceDetailId, soDetailIds).count();

        Integer soOutstockCount = soOutstockDetailService.getPushDownCountBySoDetailIds(soDetailIds);
        return deliveryNoticeCount+soOutstockCount;
    }
}
