package com.erp.server.wms.service.impl;

import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.ero.rpc.oms.feign.SoInfoFeign;
import com.erp.model.oms.dto.SoReturnDetailDTO;
import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.oms.entity.SoReturnDetailEntity;
import com.erp.model.wms.dto.SoDeliveryNoticeDTO;
import com.erp.model.wms.dto.SoDeliveryNoticeDetailDTO;
import com.erp.model.wms.entity.SoDeliveryNoticeDetailEntity;
import com.erp.model.wms.entity.WarehouseReceiveDetailEntity;
import com.erp.server.wms.mapper.SoDeliveryNoticeDetailMapper;
import com.erp.server.wms.service.SoDeliveryNoticeDetailService;
import com.common.business.service.SuperServiceImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
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

    @Override
    public Boolean add(SoDeliveryNoticeDTO.Add dto, String id) {
        List<String> detailIds = dto.getDetailList().stream().map(SoDeliveryNoticeDetailDTO.Add::getSourceDetailId).collect(Collectors.toList());
        List<SoDetailEntity> soDetailEntitieList = soInfoFeign.listSoDetailByIds(detailIds);
        if (CollectionUtils.isEmpty(soDetailEntitieList)) {
            throw new ServiceException(ApiError.ERROR_92003);
        }
        List<SoDeliveryNoticeDetailEntity> list = new ArrayList<>();
        for (SoDeliveryNoticeDetailDTO.Add detailDto : dto.getDetailList()) {
            SoDeliveryNoticeDetailEntity soDeliveryNoticeDetailEntity = new SoDeliveryNoticeDetailEntity();
            SoDetailEntity soDetailEntity = soDetailEntitieList.stream().filter(req -> req.getId().equals(detailDto.getSourceDetailId())).findFirst().orElse(new SoDetailEntity());
            soDeliveryNoticeDetailEntity.setMainId(id);
            soDeliveryNoticeDetailEntity.setSkuId(soDetailEntity.getSkuId());
            soDeliveryNoticeDetailEntity.setSkuNo(soDetailEntity.getSkuNo());
            soDeliveryNoticeDetailEntity.setDeliveryQty(detailDto.getDeliveryQty());
            soDeliveryNoticeDetailEntity.setIsClose(detailDto.getIsClose());
            soDeliveryNoticeDetailEntity.setRemark(detailDto.getRemark());
            soDeliveryNoticeDetailEntity.setSourceDetailId(detailDto.getSourceDetailId());
            list.add(soDeliveryNoticeDetailEntity);
        }
        return this.saveBatch(list);
    }

    @Override
    public Boolean update(SoDeliveryNoticeDTO.Update dto) {
        List<String> detailIds = dto.getDetailList().stream().map(SoDeliveryNoticeDetailDTO.Update::getSourceDetailId).collect(Collectors.toList());
        List<SoDetailEntity> soDetailEntitieList = soInfoFeign.listSoDetailByIds(detailIds);
        if (CollectionUtils.isEmpty(soDetailEntitieList)) {
            throw new ServiceException(ApiError.ERROR_92003);
        }
        List<SoDeliveryNoticeDetailEntity> list = new ArrayList<>();
        for (SoDeliveryNoticeDetailDTO.Update detailDto : dto.getDetailList()) {
            SoDeliveryNoticeDetailEntity soDeliveryNoticeDetailEntity = new SoDeliveryNoticeDetailEntity();
            SoDetailEntity soDetailEntity = soDetailEntitieList.stream().filter(req -> req.getId().equals(detailDto.getSourceDetailId())).findFirst().orElse(new SoDetailEntity());
            if (StringUtils.isNotBlank(detailDto.getId())) {
                soDeliveryNoticeDetailEntity.setId(detailDto.getId());
            }
            soDeliveryNoticeDetailEntity.setMainId(detailDto.getMainId());
            soDeliveryNoticeDetailEntity.setSkuId(soDetailEntity.getSkuId());
            soDeliveryNoticeDetailEntity.setSkuNo(soDetailEntity.getSkuNo());
            soDeliveryNoticeDetailEntity.setDeliveryQty(detailDto.getDeliveryQty());
            soDeliveryNoticeDetailEntity.setIsClose(detailDto.getIsClose());
            soDeliveryNoticeDetailEntity.setRemark(detailDto.getRemark());
            soDeliveryNoticeDetailEntity.setSourceDetailId(detailDto.getSourceDetailId());
            list.add(soDeliveryNoticeDetailEntity);
        }
        return this.saveOrUpdateBatch(list);
    }

    @Override
    public Boolean delete(List<String> mainIds) {
        return lambdaUpdate().set(SoDeliveryNoticeDetailEntity::getIsDeleted, Boolean.TRUE)
                .in(SoDeliveryNoticeDetailEntity::getMainId, mainIds)
                .remove();
    }

    @Override
    public List<SoDeliveryNoticeDetailEntity> getDetailByMainId(String mainId) {
        return lambdaQuery().eq(SoDeliveryNoticeDetailEntity::getMainId, mainId).list();
    }
}
