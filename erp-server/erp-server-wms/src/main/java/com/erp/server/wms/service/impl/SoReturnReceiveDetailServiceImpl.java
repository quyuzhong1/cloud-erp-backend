package com.erp.server.wms.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.enums.ApproveStatusEnum;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.oms.entity.SoReturnDetailEntity;
import com.erp.model.oms.entity.SoReturnEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.SoReturnNoticeDetailDTO;
import com.erp.model.wms.dto.SoReturnReceiveDTO;
import com.erp.model.wms.dto.SoReturnReceiveDetailDTO;
import com.erp.model.wms.entity.SoDeliveryNoticeDetailEntity;
import com.erp.model.wms.entity.SoOutstockDetailEntity;
import com.erp.model.wms.entity.SoReturnNoticeDetailEntity;
import com.erp.model.wms.entity.SoReturnReceiveDetailEntity;
import com.erp.rpc.oms.feign.SoInfoFeign;
import com.erp.rpc.oms.feign.SoReturnFeign;
import com.erp.server.wms.mapper.SoReturnReceiveDetailMapper;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.SoDeliveryNoticeDetailService;
import com.erp.server.wms.service.SoOutstockDetailService;
import com.erp.server.wms.service.SoReturnReceiveDetailService;
import com.common.business.service.SuperServiceImpl;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 销售退货签收单明细表 服务实现类
 * @author LUO_WG
 * @since 2023-05-10
 */
@Service
public class SoReturnReceiveDetailServiceImpl extends SuperServiceImpl<SoReturnReceiveDetailMapper, SoReturnReceiveDetailEntity> implements SoReturnReceiveDetailService {
    @Resource
    private SoInfoFeign soInfoFeign;

    @Resource
    private SoReturnFeign soReturnFeign;

    @Resource
    private SoDeliveryNoticeDetailService soDeliveryNoticeDetailService;

    @Resource
    private SoOutstockDetailService soOutstockDetailService;

    @Resource
    private OperateLogService operateLogService;

    /**
     * 根据退货单获取销售单已出库数量
     * @Author Luo_WG
     * @Date 2023/5/19 12:21
     * @param returnId
     * @return java.util.List<com.erp.model.wms.entity.SoOutstockDetailEntity>
     **/
    private  List<SoOutstockDetailEntity> listSoOutstockByReturnId(String returnId) {
        //获取退货详情
        List<SoReturnDetailEntity> returnDetailEntityList = soReturnFeign.listDetailByMainId(returnId);
        //获取销售订单明细表id
        List<String> soDetailIds = returnDetailEntityList.stream().map(SoReturnDetailEntity::getSourceDetailId).collect(Collectors.toList());
        //根据销售单详情id获取发货通知单详情信息
        List<SoDeliveryNoticeDetailEntity> detailEntityList = soDeliveryNoticeDetailService.listDetailBySourceDetailIds(soDetailIds);
        //获取发货通知单明细表id
        List<String> deliveryNoticeDetailIdList = detailEntityList.stream().map(SoDeliveryNoticeDetailEntity::getId).collect(Collectors.toList());
        soDetailIds.addAll(deliveryNoticeDetailIdList);
        //根据销售单获取出库单
        List<SoOutstockDetailEntity> soOutstockDetailEntities = soOutstockDetailService.listDetailBySourceDetailId(soDetailIds);
        return soOutstockDetailEntities;
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean add(SoReturnReceiveDTO.Add dto, String id) {
        List<SoOutstockDetailEntity> soOutstockDetailEntities = listSoOutstockByReturnId(dto.getSourceId());
        //获取退货单详情表id
        List<String> returnDetailIds = dto.getDetailList().stream().map(SoReturnReceiveDetailDTO.Add::getSourceDetailId).collect(Collectors.toList());
        List<SoReturnDetailEntity> soReturnDetailEntities = soReturnFeign.listDetailByIds(returnDetailIds);
        List<SoReturnReceiveDetailEntity> list = new ArrayList<>();
        for (SoReturnReceiveDetailDTO.Add detailDto : dto.getDetailList()) {
            SoReturnReceiveDetailEntity detailEntity = new SoReturnReceiveDetailEntity();
            SoReturnDetailEntity soReturnDetailEntity = soReturnDetailEntities.stream().filter(req -> req.getId().equals(detailDto.getSourceDetailId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(soReturnDetailEntity)) {
                throw new ServiceException(ApiError.ERROR_92016);
            }
            Integer actualQty = soOutstockDetailEntities.stream().filter(detail -> detail.getSkuId().equals(soReturnDetailEntity.getSkuId()) && detail.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())).map(SoOutstockDetailEntity::getActualQty).reduce(MathUtil.ZERO, Integer::sum);
            Integer returnQty = soReturnDetailEntities.stream().filter(req -> req.getId().equals(detailDto.getSourceDetailId())).map(SoReturnDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
            if (actualQty < detailDto.getReturnQty()) {
                throw new ServiceException(ApiError.ERROR_92009);
            }
            if (detailDto.getReceiveQty() < returnQty) {
                throw new ServiceException(ApiError.ERROR_92020);
            }
            detailEntity.setMainId(id);
            detailEntity.setSkuId(soReturnDetailEntity.getSkuId());
            detailEntity.setSkuNo(soReturnDetailEntity.getSkuNo());
            detailEntity.setReturnQty(detailDto.getReturnQty());
            detailEntity.setReturnTypeDict(detailDto.getReturnTypeDict());
            detailEntity.setReturnReasonDict(detailDto.getReturnReasonDict());
            detailEntity.setRemark(detailDto.getRemark());
            detailEntity.setSourceDetailId(detailDto.getSourceDetailId());
            list.add(detailEntity);
        }
        return this.saveBatch(list);
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean update(SoReturnReceiveDTO.Update dto) {
        List<String> addList = dto.getDetailList().stream().filter(c -> StringUtils.isBlank(c.getId())).map(SoReturnReceiveDetailDTO.Update::getId).collect(Collectors.toList());
        List<SoOutstockDetailEntity> soOutstockDetailEntities = listSoOutstockByReturnId(dto.getSourceId());
        //获取退货单详情表id
        List<String> returnDetailIds = dto.getDetailList().stream().map(SoReturnReceiveDetailDTO.Update::getSourceDetailId).collect(Collectors.toList());
        List<SoReturnDetailEntity> soReturnDetailEntities = soReturnFeign.listDetailByIds(returnDetailIds);
        List<SoReturnReceiveDetailEntity> list = new ArrayList<>();
        //原明细数据
        List<SoReturnReceiveDetailEntity> oldList = this.listDetailByMainId(dto.getId());
        List<String> deleteIds = getDeleteIds(dto.getDetailList(), oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            List<SoReturnReceiveDetailEntity> removeList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
            //操作日志
            List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(obj.getMainId(), obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("删除了一个SKU【%s】", ModuleTypeEnum.SO_RETURN_RECEIVE.getCode(),pairList,"编辑操作");
            this.removeByIds(deleteIds);
        }
        for (SoReturnReceiveDetailDTO.Update detailDto : dto.getDetailList()) {
            SoReturnReceiveDetailEntity detailEntity = new SoReturnReceiveDetailEntity();
            SoReturnDetailEntity soReturnDetailEntity = soReturnDetailEntities.stream().filter(req -> req.getId().equals(detailDto.getSourceDetailId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(soReturnDetailEntity)) {
                throw new ServiceException(ApiError.ERROR_92016);
            }
            Integer actualQty = soOutstockDetailEntities.stream().filter(detail -> detail.getSkuId().equals(soReturnDetailEntity.getSkuId()) && detail.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())).map(SoOutstockDetailEntity::getActualQty).reduce(MathUtil.ZERO, Integer::sum);
            Integer returnQty = soReturnDetailEntities.stream().filter(req -> req.getId().equals(detailDto.getSourceDetailId())).map(SoReturnDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
            if (actualQty < detailDto.getReturnQty()) {
                throw new ServiceException(ApiError.ERROR_92009);
            }
            if (detailDto.getReceiveQty() < returnQty) {
                throw new ServiceException(ApiError.ERROR_92020);
            }
            detailEntity.setId(dto.getId());
            detailEntity.setMainId(detailDto.getMainId());
            detailEntity.setSkuId(soReturnDetailEntity.getSkuId());
            detailEntity.setSkuNo(soReturnDetailEntity.getSkuNo());
            detailEntity.setReturnQty(detailDto.getReturnQty());
            detailEntity.setReturnTypeDict(detailDto.getReturnTypeDict());
            detailEntity.setReturnReasonDict(detailDto.getReturnReasonDict());
            detailEntity.setRemark(detailDto.getRemark());
            detailEntity.setSourceDetailId(detailDto.getSourceDetailId());
            list.add(detailEntity);
            //修改操作日志
            if (StringUtils.isNotBlank(detailEntity.getId())) {
                SoReturnReceiveDetailEntity old = this.getById(detailEntity.getId());
                operateLogService.addModuleOperateLogByObj(old,detailEntity, ModuleTypeEnum.SO_RETURN_RECEIVE.getCode(),dto.getId(),"",String.format("【%s】",old.getSkuNo()));
            }
        }
        //添加操作日志
        if (CollectionUtils.isNotEmpty(addList)) {
            List<SoReturnReceiveDetailEntity> returnNoticeDetailEntities = this.listByIds(addList);
            List<Pair<String, String>> addPairList = returnNoticeDetailEntities.stream().map(obj -> new Pair<>(dto.getId(), obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("添加了一个SKU【%s】", ModuleTypeEnum.SO_RETURN_RECEIVE.getCode(), addPairList, "编辑操作");
        }
        return this.saveBatch(list);
    }

    private List<String> getDeleteIds(List<SoReturnReceiveDetailDTO.Update> newList, List<SoReturnReceiveDetailEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(SoReturnReceiveDetailDTO.Update::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(SoReturnReceiveDetailEntity
                ::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean delete(List<String> mainIds) {
        return lambdaUpdate().set(SoReturnReceiveDetailEntity::getIsDeleted, Boolean.TRUE)
                .in(SoReturnReceiveDetailEntity::getMainId, mainIds)
                .remove();
    }

    @Override
    public List<SoReturnReceiveDetailEntity> listDetailBySourceIds(List<String> sourceIds) {
        return baseMapper.listDetailBySourceIds(sourceIds);
    }

    @Override
    public List<SoReturnReceiveDetailEntity> listSoDetailByIds(List<String> ids) {
        return baseMapper.listSoDetailByIds(ids);
    }

    @Override
    public List<SoReturnReceiveDetailEntity> listDetailByMainId(String id) {
        return lambdaQuery().eq(SoReturnReceiveDetailEntity::getMainId, id).list();
    }

    @Override
    public List<SoReturnReceiveDetailEntity> listDetailByMainIds(List<String> ids) {
        return lambdaQuery().in(SoReturnReceiveDetailEntity::getMainId, ids).list();
    }
}
