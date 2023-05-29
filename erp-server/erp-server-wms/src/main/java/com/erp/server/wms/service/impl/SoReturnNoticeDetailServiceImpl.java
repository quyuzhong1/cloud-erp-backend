package com.erp.server.wms.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.enums.ApproveStatusEnum;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.dto.SoReturnDetailDTO;
import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.oms.entity.SoReturnDetailEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.SoDeliveryNoticeDTO;
import com.erp.model.wms.dto.SoDeliveryNoticeDetailDTO;
import com.erp.model.wms.dto.SoReturnNoticeDTO;
import com.erp.model.wms.dto.SoReturnNoticeDetailDTO;
import com.erp.model.wms.entity.SoDeliveryNoticeDetailEntity;
import com.erp.model.wms.entity.SoOutstockDetailEntity;
import com.erp.model.wms.entity.SoReturnNoticeDetailEntity;
import com.erp.model.wms.entity.SoReturnNoticeDetailEntity;
import com.erp.rpc.oms.feign.SoInfoFeign;
import com.erp.rpc.oms.feign.SoReturnFeign;
import com.erp.server.wms.mapper.SoReturnNoticeDetailMapper;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.SoDeliveryNoticeDetailService;
import com.erp.server.wms.service.SoOutstockDetailService;
import com.erp.server.wms.service.SoReturnNoticeDetailService;
import com.common.business.service.SuperServiceImpl;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 销售退货通知单明细表 服务实现类
 *
 * @author LUO_WG
 * @since 2023-05-10
 */
@Service
public class SoReturnNoticeDetailServiceImpl extends SuperServiceImpl<SoReturnNoticeDetailMapper, SoReturnNoticeDetailEntity> implements SoReturnNoticeDetailService {
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
    public Boolean add(SoReturnNoticeDTO.Add dto, String id) {
        //获取退货单详情表id
        List<String> returnDetailIds = dto.getDetailList().stream().map(SoReturnNoticeDetailDTO.Add::getSourceDetailId).collect(Collectors.toList());
        List<SoReturnDetailEntity> soReturnDetailEntities = soReturnFeign.listDetailByIds(returnDetailIds);
        List<SoReturnNoticeDetailEntity> noticeDetailEntities = this.listDetailBySourceDetailIds(returnDetailIds);
        List<SoReturnNoticeDetailEntity> list = new ArrayList<>();
        for (SoReturnNoticeDetailDTO.Add detailDto : dto.getDetailList()) {
            SoReturnNoticeDetailEntity detailEntity = new SoReturnNoticeDetailEntity();
            SoReturnDetailEntity soReturnDetailEntity = soReturnDetailEntities.stream().filter(req -> req.getId().equals(detailDto.getSourceDetailId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(soReturnDetailEntity)) {
                throw new ServiceException(ApiError.ERROR_92023);
            }
            //退货通知单数量
            Integer returnNoticeQty = noticeDetailEntities.stream().filter(req -> req.getSourceDetailId().equals(detailDto.getSourceDetailId())).map(SoReturnNoticeDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
            //退货单数量
            Integer returnQty = soReturnDetailEntities.stream().filter(req -> req.getId().equals(detailDto.getSourceDetailId())).map(SoReturnDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
            if (returnQty <  detailDto.getReturnQty() + returnNoticeQty) {
                throw new ServiceException(ApiError.ERROR_92024);
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
    public Boolean update(SoReturnNoticeDTO.Update dto) {
        List<String> addList = dto.getDetailList().stream().filter(c -> StringUtils.isBlank(c.getId())).map(SoReturnNoticeDetailDTO.Update::getId).collect(Collectors.toList());
        //获取退货单详情表id
        List<String> returnDetailIds = dto.getDetailList().stream().map(SoReturnNoticeDetailDTO.Update::getSourceDetailId).collect(Collectors.toList());
        List<SoReturnDetailEntity> soReturnDetailEntities = soReturnFeign.listDetailByIds(returnDetailIds);
        List<SoReturnNoticeDetailEntity> list = new ArrayList<>();
        List<SoReturnNoticeDetailEntity> noticeDetailEntities = this.listDetailBySourceDetailIds(returnDetailIds);

        //原明细数据
        List<SoReturnNoticeDetailEntity> oldList = this.listDetailByMainId(dto.getId());
        List<String> deleteIds = getDeleteIds(dto.getDetailList(), oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            List<SoReturnNoticeDetailEntity> removeList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
            //操作日志
            List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(obj.getMainId(), obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("删除了一个SKU【%s】", ModuleTypeEnum.SO_RETURN_NOTICE.getCode(),pairList,"编辑操作");
            this.removeByIds(deleteIds);
        }
        for (SoReturnNoticeDetailDTO.Update detailDto : dto.getDetailList()) {
            SoReturnNoticeDetailEntity detailEntity = new SoReturnNoticeDetailEntity();
            //退货通知单数量
            Integer returnNoticeQty = noticeDetailEntities.stream().filter(req -> req.getSourceDetailId().equals(detailDto.getSourceDetailId())).map(SoReturnNoticeDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
            if (StringUtils.isNotBlank(detailDto.getId())) {
                detailEntity.setId(detailDto.getId());
                returnNoticeQty = noticeDetailEntities.stream().filter(req -> req.getSourceDetailId().equals(detailDto.getSourceDetailId()) && !req.getId().equals(detailDto.getId())).map(SoReturnNoticeDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
            }
            SoReturnDetailEntity soReturnDetailEntity = soReturnDetailEntities.stream().filter(req -> req.getId().equals(detailDto.getSourceDetailId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(soReturnDetailEntity)) {
                throw new ServiceException(ApiError.ERROR_92023);
            }
             //退货单数量
            Integer returnQty = soReturnDetailEntities.stream().filter(req -> req.getId().equals(detailDto.getSourceDetailId())).map(SoReturnDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
            if (returnQty <  detailDto.getReturnQty() + returnNoticeQty) {
                throw new ServiceException(ApiError.ERROR_92024);
            }
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
                SoReturnNoticeDetailEntity old = this.getById(detailEntity.getId());
                operateLogService.addModuleOperateLogByObj(old, detailEntity, ModuleTypeEnum.SO_RETURN_NOTICE.getCode(), dto.getId(), "", String.format("【%s】", old.getSkuNo()));
            }
        }
        //添加操作日志
        if (CollectionUtils.isNotEmpty(addList)) {
            List<SoReturnNoticeDetailEntity> returnNoticeDetailEntities = this.listByIds(addList);
            List<Pair<String, String>> addPairList = returnNoticeDetailEntities.stream().map(obj -> new Pair<>(dto.getId(), obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("添加了一个SKU【%s】", ModuleTypeEnum.SO_RETURN_NOTICE.getCode(), addPairList, "编辑操作");
        }
        return this.saveOrUpdateBatch(list);
    }

    private List<String> getDeleteIds(List<SoReturnNoticeDetailDTO.Update> newList, List<SoReturnNoticeDetailEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(SoReturnNoticeDetailDTO.Update::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(SoReturnNoticeDetailEntity
                ::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(List<String> mainIds) {
        return lambdaUpdate().set(SoReturnNoticeDetailEntity::getIsDeleted, Boolean.TRUE)
                .in(SoReturnNoticeDetailEntity::getMainId, mainIds)
                .remove();
    }

    @Override
    public List<SoReturnNoticeDetailEntity> listDetailBySourceIds(List<String> sourceIds) {
        return baseMapper.listDetailBySourceIds(sourceIds);
    }

    @Override
    public List<SoReturnNoticeDetailEntity> listDetailByMainId(String id) {
        return lambdaQuery().eq(SoReturnNoticeDetailEntity::getMainId, id).list();
    }

    @Override
    public List<SoReturnNoticeDetailEntity> listDetailBySourceDetailIds(List<String> sourceDetailIds) {
        return baseMapper.listDetailBySourceDetailIds(sourceDetailIds);
    }

}
