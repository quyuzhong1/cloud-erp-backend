package com.erp.server.oms.service.impl;

import com.common.business.enums.ApproveStatusEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.dto.SoReturnDTO;
import com.erp.model.oms.dto.SoReturnDetailDTO;
import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.oms.entity.SoReturnDetailEntity;
import com.erp.model.wms.entity.SoOutstockDetailEntity;
import com.erp.rpc.wms.feign.SoOutstockFeign;
import com.erp.server.oms.mapper.SoReturnDetailMapper;
import com.erp.server.oms.service.SoDetailService;
import com.erp.server.oms.service.SoReturnDetailService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 退货单详情 服务实现类
 * </p>
 *
 * @author LUO_WG
 * @since 2023-05-10
 */
@Service
public class SoReturnDetailServiceImpl extends SuperServiceImpl<SoReturnDetailMapper, SoReturnDetailEntity> implements SoReturnDetailService {

    @Resource
    private SoDetailService soDetailService;

    @Resource
    private SoOutstockFeign soOutstockFeign;

    @Override
    public Boolean add(SoReturnDTO.Add dto, String id) {
        List<String> detailIds = dto.getDetailList().stream().map(SoReturnDetailDTO.Add::getSourceDetailId).collect(Collectors.toList());
        List<SoDetailEntity> soDetailEntitieList = soDetailService.listSoDetailByIds(detailIds);
        if (CollectionUtils.isEmpty(soDetailEntitieList)) {
            throw new ServiceException(ApiError.ERROR_92003);
        }
        List<SoOutstockDetailEntity> soOutstockDetailEntities = soOutstockFeign.listDetailBySourceDetailId(detailIds);
        List<SoReturnDetailEntity> soReturnDetailEntities = this.listDetailBySourceId(detailIds);
        List<SoReturnDetailEntity> list = new ArrayList<>();
        for (SoReturnDetailDTO.Add detailDto : dto.getDetailList()) {
            SoReturnDetailEntity soReturnDetailEntity = new SoReturnDetailEntity();
            SoDetailEntity soDetailEntity = soDetailEntitieList.stream().filter(req -> req.getId().equals(detailDto.getSourceDetailId())).findFirst().orElse(new SoDetailEntity());
            Integer returnQty = soReturnDetailEntities.stream().filter(req -> req.getSourceDetailId().equals(detailDto.getSourceDetailId())).map(SoReturnDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
            Integer actualQty = soOutstockDetailEntities.stream().filter(detail -> detail.getSourceDetailId().equals(detailDto.getSourceDetailId()) && detail.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())).map(SoOutstockDetailEntity::getActualQty).reduce(MathUtil.ZERO, Integer::sum);
            if (actualQty < detailDto.getReturnQty() + returnQty) {
                throw new ServiceException(ApiError.ERROR_92009);
            }
            soReturnDetailEntity.setMainId(id);
            soReturnDetailEntity.setSkuId(soDetailEntity.getSkuId());
            soReturnDetailEntity.setSkuNo(soDetailEntity.getSkuNo());
            soReturnDetailEntity.setReturnQty(detailDto.getReturnQty());
            soReturnDetailEntity.setReturnTypeDict(detailDto.getReturnTypeDict());
            soReturnDetailEntity.setRemark(detailDto.getRemark());
            soReturnDetailEntity.setSourceDetailId(detailDto.getSourceDetailId());
            list.add(soReturnDetailEntity);
        }
        return this.saveBatch(list);
    }

    @Override
    public Boolean update(SoReturnDTO.Update dto) {
        List<String> detailIds = dto.getDetailList().stream().map(SoReturnDetailDTO.Update::getSourceDetailId).collect(Collectors.toList());
        List<SoDetailEntity> soDetailEntitieList = soDetailService.listSoDetailByIds(detailIds);

        if (CollectionUtils.isEmpty(soDetailEntitieList)) {
            throw new ServiceException(ApiError.ERROR_92003);
        }
        List<SoOutstockDetailEntity> soOutstockDetailEntities = soOutstockFeign.listDetailBySourceDetailId(detailIds);
        List<SoReturnDetailEntity> soReturnDetailEntities = this.listDetailBySourceId(detailIds);
        List<SoReturnDetailEntity> list = new ArrayList<>();
        for (SoReturnDetailDTO.Update detailDto : dto.getDetailList()) {
            SoReturnDetailEntity soReturnDetailEntity = new SoReturnDetailEntity();
            SoDetailEntity soDetailEntity = soDetailEntitieList.stream().filter(req -> req.getId().equals(detailDto.getSourceDetailId())).findFirst().orElse(new SoDetailEntity());
            Integer returnQty = soReturnDetailEntities.stream().filter(req -> req.getSourceDetailId().equals(detailDto.getSourceDetailId())).map(SoReturnDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
            Integer actualQty = soOutstockDetailEntities.stream().filter(detail -> detail.getSourceDetailId().equals(detailDto.getSourceDetailId()) && detail.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())).map(SoOutstockDetailEntity::getActualQty).reduce(MathUtil.ZERO, Integer::sum);
            if (actualQty < detailDto.getReturnQty() + returnQty) {
                throw new ServiceException(ApiError.ERROR_92009);
            }
            soReturnDetailEntity.setSkuId(soDetailEntity.getSkuId());
            soReturnDetailEntity.setSkuNo(soDetailEntity.getSkuNo());
            soReturnDetailEntity.setReturnQty(detailDto.getReturnQty());
            soReturnDetailEntity.setReturnTypeDict(detailDto.getReturnTypeDict());
            soReturnDetailEntity.setRemark(detailDto.getRemark());
            soReturnDetailEntity.setSourceDetailId(detailDto.getSourceDetailId());
            list.add(soReturnDetailEntity);
        }
        return this.saveOrUpdateBatch(list);
    }

    @Override
    public Boolean delete(List<String> mainIds) {
        return lambdaUpdate().set(SoReturnDetailEntity::getIsDeleted, Boolean.TRUE)
                .in(SoReturnDetailEntity::getMainId, mainIds)
                .remove();
    }

    @Override
    public List<SoReturnDetailEntity> listDetailByMainId(String mainId) {
        return lambdaQuery().eq(SoReturnDetailEntity::getMainId, mainId).list();
    }

    @Override
    public List<SoReturnDetailEntity> listDetailBySourceId(List<String> sourceIds) {
        return baseMapper.listDetailBySourceId(sourceIds);
    }

    @Override
    public List<SoReturnDetailEntity> listDetailByIds(List<String> ids) {
        return baseMapper.listDetailByIds(ids);
    }
}
