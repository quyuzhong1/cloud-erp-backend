package com.erp.server.wms.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.common.business.enums.ApproveStatusEnum;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.dto.SoReturnDTO;
import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.oms.entity.SoReturnDetailEntity;
import com.erp.model.oms.entity.SoReturnEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
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
import org.springframework.transaction.annotation.Transactional;

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean add(SoReturnReceiveDTO.Add dto, String id) {
        SoReturnEntity soReturnEntity = soReturnFeign.getSoReturnById(dto.getSourceId());
        //根据销售单获取出库单
        List<SoOutstockDetailEntity> soOutstockDetailEntities = soOutstockDetailService.listDetailBySoIds(Arrays.asList(soReturnEntity.getSourceId()));
        //获取退货单详情表id
        List<String> returnDetailIds = dto.getDetailList().stream().map(SoReturnReceiveDetailDTO.Add::getSourceDetailId).collect(Collectors.toList());
        List<SoReturnDetailEntity> soReturnDetailEntities = soReturnFeign.listDetailByIds(returnDetailIds);
        List<SoReturnReceiveDetailEntity> list = new ArrayList<>();
        List<SoReturnReceiveDetailEntity> soReturnReceiveDetailEntities = this.listDetailBySourceIds(Arrays.asList(dto.getSourceId()));
        List<String> soDetailIds = soReturnDetailEntities.stream().map(SoReturnDetailEntity::getSourceDetailId).collect(Collectors.toList());
        List<SoDetailEntity> soDetailEntities = soInfoFeign.listSoDetailByIds(soDetailIds);
        for (SoReturnReceiveDetailDTO.Add detailDto : dto.getDetailList()) {
            SoReturnReceiveDetailEntity detailEntity = new SoReturnReceiveDetailEntity();
            SoReturnDetailEntity soReturnDetailEntity = soReturnDetailEntities.stream().filter(req -> req.getId().equals(detailDto.getSourceDetailId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(soReturnDetailEntity)) {
                throw new ServiceException(ApiError.ERROR_92023);
            }
            SoDetailEntity soDetailEntity = soDetailEntities.stream().filter(detail -> detail.getId().equals(soReturnDetailEntity.getSourceDetailId())).findFirst().orElse(new SoDetailEntity());
            //此单历史退货数量
            Integer historyReturnQty = soReturnReceiveDetailEntities.stream().filter(req -> req.getSourceDetailId().equals(detailDto.getSourceDetailId())).map(SoReturnReceiveDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
            Integer actualQty = soOutstockDetailEntities.stream().filter(detail -> soDetailEntity.getMainId().equals(detail.getSoId()) && detail.getSkuId().equals(soReturnDetailEntity.getSkuId()) && detail.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())).map(SoOutstockDetailEntity::getActualQty).reduce(MathUtil.ZERO, Integer::sum);
            Integer returnQty = soReturnDetailEntities.stream().filter(req -> req.getId().equals(detailDto.getSourceDetailId())).map(SoReturnDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
            if (actualQty < detailDto.getReturnQty() + historyReturnQty) {
                throw new ServiceException(ApiError.ERROR_92009);
            }
            //此单历史签收数量
            Integer historyReceiveQty = soReturnReceiveDetailEntities.stream().filter(req -> req.getSourceDetailId().equals(detailDto.getSourceDetailId())).map(SoReturnReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
            if (detailDto.getReceiveQty() + historyReceiveQty > returnQty) {
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
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(SoReturnReceiveDTO.Update dto) {
        List<String> addList = dto.getDetailList().stream().filter(c -> StringUtils.isBlank(c.getId())).map(SoReturnReceiveDetailDTO.Update::getId).collect(Collectors.toList());
        SoReturnEntity soReturnEntity = soReturnFeign.getSoReturnById(dto.getSourceId());
        //根据销售单获取出库单
        List<SoOutstockDetailEntity> soOutstockDetailEntities = soOutstockDetailService.listDetailBySoIds(Arrays.asList(soReturnEntity.getSourceId()));
        //获取退货单详情表id
        List<String> returnDetailIds = dto.getDetailList().stream().map(SoReturnReceiveDetailDTO.Update::getSourceDetailId).collect(Collectors.toList());
        List<SoReturnDetailEntity> soReturnDetailEntities = soReturnFeign.listDetailByIds(returnDetailIds);
        List<SoReturnReceiveDetailEntity> list = new ArrayList<>();
        List<SoReturnReceiveDetailEntity> soReturnReceiveDetailEntities = this.listDetailBySourceIds(Arrays.asList(dto.getSourceId()));
        List<String> soDetailIds = soReturnDetailEntities.stream().map(SoReturnDetailEntity::getSourceDetailId).collect(Collectors.toList());
        List<SoDetailEntity> soDetailEntities = soInfoFeign.listSoDetailByIds(soDetailIds);
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
            if (ObjectUtil.isEmpty(soReturnDetailEntity)) {
                throw new ServiceException(ApiError.ERROR_92023);
            }
            SoDetailEntity soDetailEntity = soDetailEntities.stream().filter(detail -> detail.getId().equals(soReturnDetailEntity.getSourceDetailId())).findFirst().orElse(new SoDetailEntity());

            //此单历史退货数量
            Integer historyReturnQty = soReturnReceiveDetailEntities.stream().filter(req -> req.getSourceDetailId().equals(detailDto.getSourceDetailId())).map(SoReturnReceiveDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
            //此单历史签收数量
            Integer historyReceiveQty = soReturnReceiveDetailEntities.stream().filter(req -> req.getSourceDetailId().equals(detailDto.getSourceDetailId())).map(SoReturnReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
            if (StringUtils.isNotBlank(detailDto.getId())) {
                detailEntity.setId(detailDto.getId());
                historyReturnQty = soReturnReceiveDetailEntities.stream().filter(req -> req.getSourceDetailId().equals(detailDto.getSourceDetailId()) && !req.getId().equals(detailDto.getId())).map(SoReturnReceiveDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
                historyReceiveQty = soReturnReceiveDetailEntities.stream().filter(req -> req.getSourceDetailId().equals(detailDto.getSourceDetailId()) && !req.getId().equals(detailDto.getId())).map(SoReturnReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
            }
            Integer actualQty = soOutstockDetailEntities.stream().filter(detail -> soDetailEntity.getMainId().equals(detail.getSoId()) &&detail.getSkuId().equals(soReturnDetailEntity.getSkuId()) && detail.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())).map(SoOutstockDetailEntity::getActualQty).reduce(MathUtil.ZERO, Integer::sum);
            Integer returnQty = soReturnDetailEntities.stream().filter(req -> req.getId().equals(detailDto.getSourceDetailId())).map(SoReturnDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
            if (actualQty < detailDto.getReturnQty() + historyReturnQty) {
                throw new ServiceException(ApiError.ERROR_92009);
            }
            if (detailDto.getReceiveQty() + historyReceiveQty > returnQty) {
                throw new ServiceException(ApiError.ERROR_92020);
            }
            detailEntity.setId(detailDto.getId());
            detailEntity.setMainId(dto.getId());
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
        return this.saveOrUpdateBatch(list);
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
    public List<SoReturnReceiveDetailEntity> listDetailByIds(List<String> ids) {
        return baseMapper.listDetailByIds(ids);
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
