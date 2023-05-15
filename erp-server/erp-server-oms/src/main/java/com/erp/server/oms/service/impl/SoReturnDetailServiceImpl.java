package com.erp.server.oms.service.impl;

import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.dto.SoReturnDTO;
import com.erp.model.oms.dto.SoReturnDetailDTO;
import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.model.oms.entity.SoReturnDetailEntity;
import com.erp.server.oms.mapper.SoReturnDetailMapper;
import com.erp.server.oms.service.SoDetailService;
import com.erp.server.oms.service.SoReturnDetailService;
import com.common.business.service.SuperServiceImpl;
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

    @Override
    public Boolean add(SoReturnDTO.Add dto, String id) {
        List<String> detailIds = dto.getDetailList().stream().map(SoReturnDetailDTO.Add::getSourceDetailId).collect(Collectors.toList());
        List<SoDetailEntity> soDetailEntitieList = soDetailService.listSoDetailByIds(detailIds);

        if (CollectionUtils.isEmpty(soDetailEntitieList)) {
            throw new ServiceException(ApiError.ERROR_92003);
        }
        List<SoReturnDetailEntity> list = new ArrayList<>();
        for (SoReturnDetailDTO.Add detailDto : dto.getDetailList()) {
            SoReturnDetailEntity soReturnDetailEntity = new SoReturnDetailEntity();
            SoDetailEntity soDetailEntity = soDetailEntitieList.stream().filter(req -> req.getId().equals(detailDto.getSourceDetailId())).findFirst().orElse(new SoDetailEntity());
            if (soDetailEntity.getQty() < detailDto.getReturnQty()) {
                throw new ServiceException(ApiError.ERROR_92009);
            }
            soReturnDetailEntity.setMainId(id);
            soReturnDetailEntity.setSkuId(soDetailEntity.getSkuId());
            soReturnDetailEntity.setSkuNo(soDetailEntity.getSkuNo());
            soReturnDetailEntity.setSalesQty(soDetailEntity.getQty());
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
        List<SoReturnDetailEntity> list = new ArrayList<>();
        for (SoReturnDetailDTO.Update detailDto : dto.getDetailList()) {
            SoReturnDetailEntity soReturnDetailEntity = new SoReturnDetailEntity();
            SoDetailEntity soDetailEntity = soDetailEntitieList.stream().filter(req -> req.getId().equals(detailDto.getSourceDetailId())).findFirst().orElse(new SoDetailEntity());
            if (soDetailEntity.getQty() < detailDto.getReturnQty()) {
                throw new ServiceException(ApiError.ERROR_92009);
            }
            soReturnDetailEntity.setSkuId(soDetailEntity.getSkuId());
            soReturnDetailEntity.setSkuNo(soDetailEntity.getSkuNo());
            soReturnDetailEntity.setSalesQty(soDetailEntity.getQty());
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
}
