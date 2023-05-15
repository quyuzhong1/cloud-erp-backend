package com.erp.server.wms.service.impl;

import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.oms.entity.SoReturnDetailEntity;
import com.erp.model.wms.dto.SoDeliveryNoticeDTO;
import com.erp.model.wms.dto.SoDeliveryNoticeDetailDTO;
import com.erp.model.wms.dto.SoReturnNoticeDTO;
import com.erp.model.wms.dto.SoReturnNoticeDetailDTO;
import com.erp.model.wms.entity.SoDeliveryNoticeDetailEntity;
import com.erp.model.wms.entity.SoReturnNoticeDetailEntity;
import com.erp.model.wms.entity.SoReturnNoticeDetailEntity;
import com.erp.rpc.oms.feign.SoInfoFeign;
import com.erp.rpc.oms.feign.SoReturnFeign;
import com.erp.server.wms.mapper.SoReturnNoticeDetailMapper;
import com.erp.server.wms.service.SoReturnNoticeDetailService;
import com.common.business.service.SuperServiceImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

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

    @Override
    public Boolean add(SoReturnNoticeDTO.Add dto, String id) {
        List<String> detailIds = dto.getDetailList().stream().map(SoReturnNoticeDetailDTO.Add::getSourceDetailId).collect(Collectors.toList());
        List<SoDetailEntity> soDetailEntitieList = soInfoFeign.listSoDetailByIds(detailIds);
        if (CollectionUtils.isEmpty(soDetailEntitieList)) {
            throw new ServiceException(ApiError.ERROR_92003);
        }
        List<SoReturnDetailEntity> soReturnDetailEntities = soReturnFeign.listDetailBySourceId(detailIds);
        List<SoReturnNoticeDetailEntity> list = new ArrayList<>();
        for (SoReturnNoticeDetailDTO.Add detailDto : dto.getDetailList()) {
            SoReturnNoticeDetailEntity detailEntity = new SoReturnNoticeDetailEntity();
            SoDetailEntity soDetailEntity = soDetailEntitieList.stream().filter(req -> req.getId().equals(detailDto.getSourceDetailId())).findFirst().orElse(new SoDetailEntity());
            Integer returnQty = soReturnDetailEntities.stream().filter(req -> req.getSourceDetailId().equals(detailDto.getSourceDetailId())).map(SoReturnDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);

            if (soDetailEntity.getQty() < detailDto.getReturnQty() + returnQty) {
                throw new ServiceException(ApiError.ERROR_92009);
            }
            detailEntity.setMainId(id);
            detailEntity.setSkuId(soDetailEntity.getSkuId());
            detailEntity.setSkuNo(soDetailEntity.getSkuNo());
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
    public Boolean update(SoReturnNoticeDTO.Update dto) {
        List<String> detailIds = dto.getDetailList().stream().map(SoReturnNoticeDetailDTO.Update::getSourceDetailId).collect(Collectors.toList());
        List<SoDetailEntity> soDetailEntitieList = soInfoFeign.listSoDetailByIds(detailIds);
        if (CollectionUtils.isEmpty(soDetailEntitieList)) {
            throw new ServiceException(ApiError.ERROR_92003);
        }
        List<SoReturnDetailEntity> soReturnDetailEntities = soReturnFeign.listDetailBySourceId(detailIds);
        List<SoReturnNoticeDetailEntity> list = new ArrayList<>();
        for (SoReturnNoticeDetailDTO.Update detailDto : dto.getDetailList()) {
            SoReturnNoticeDetailEntity detailEntity = new SoReturnNoticeDetailEntity();
            SoDetailEntity soDetailEntity = soDetailEntitieList.stream().filter(req -> req.getId().equals(detailDto.getSourceDetailId())).findFirst().orElse(new SoDetailEntity());
            Integer returnQty = soReturnDetailEntities.stream().filter(req -> req.getSourceDetailId().equals(detailDto.getSourceDetailId())).map(SoReturnDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
            if (soDetailEntity.getQty() < detailDto.getReturnQty() + returnQty) {
                throw new ServiceException(ApiError.ERROR_92009);
            }
            detailEntity.setSkuId(soDetailEntity.getSkuId());
            detailEntity.setSkuNo(soDetailEntity.getSkuNo());
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
}
