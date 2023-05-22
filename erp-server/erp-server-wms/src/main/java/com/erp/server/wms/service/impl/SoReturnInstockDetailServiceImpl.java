package com.erp.server.wms.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.entity.SoReturnDetailEntity;
import com.erp.model.wms.dto.SoReturnInstockDTO;
import com.erp.model.wms.dto.SoReturnInstockDetailDTO;
import com.erp.model.wms.dto.SoReturnNoticeDTO;
import com.erp.model.wms.dto.SoReturnNoticeDetailDTO;
import com.erp.model.wms.entity.SoReturnInstockDetailEntity;
import com.erp.model.wms.entity.SoReturnInstockEntity;
import com.erp.model.wms.entity.SoReturnNoticeDetailEntity;
import com.erp.model.wms.entity.SoReturnReceiveDetailEntity;
import com.erp.rpc.oms.feign.SoReturnFeign;
import com.erp.server.wms.mapper.SoReturnInstockDetailMapper;
import com.erp.server.wms.service.SoReturnInstockDetailService;
import com.common.business.service.SuperServiceImpl;
import com.erp.server.wms.service.SoReturnReceiveDetailService;
import com.erp.server.wms.service.SoReturnReceiveService;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 销售退货入库单明细表 服务实现类
 * @author LUO_WG
 * @since 2023-05-10
 */
@Service
public class SoReturnInstockDetailServiceImpl extends SuperServiceImpl<SoReturnInstockDetailMapper, SoReturnInstockDetailEntity> implements SoReturnInstockDetailService {
    @Resource
    private SoReturnFeign soReturnFeign;

    @Resource
    private SoReturnReceiveDetailService soReturnReceiveDetailService;

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean add(SoReturnInstockDTO.Add dto, String id) {
        //获取退货单详情表id
        List<String> returnDetailIds = dto.getDetailList().stream().map(SoReturnInstockDetailDTO.Add::getSourceDetailId).collect(Collectors.toList());
        List<SoReturnDetailEntity> soReturnDetailEntities = soReturnFeign.listDetailByIds(returnDetailIds);
        List<SoReturnReceiveDetailEntity> soReturnReceiveDetailEntities = soReturnReceiveDetailService.listDetailByIds(returnDetailIds);
        List<SoReturnInstockDetailEntity> soReturnInstockDetailEntities = this.listDetailBySourceDetailIds(returnDetailIds);
        List<SoReturnInstockDetailEntity> list = new ArrayList<>();
        for (SoReturnInstockDetailDTO.Add detailDto : dto.getDetailList()) {
            SoReturnInstockDetailEntity detailEntity = new SoReturnInstockDetailEntity();
            SoReturnDetailEntity soReturnDetailEntity = soReturnDetailEntities.stream().filter(req -> req.getId().equals(detailDto.getSourceDetailId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(soReturnDetailEntity)) {
                throw new ServiceException(ApiError.ERROR_92023);
            }
            //实退 入库数量
            Integer realQty = soReturnInstockDetailEntities.stream().filter(req -> req.getSourceDetailId().equals(detailDto.getSourceDetailId())).map(SoReturnInstockDetailEntity::getRealQty).reduce(MathUtil.ZERO, Integer::sum);
            //签收单数量
            Integer receiveQty = soReturnReceiveDetailEntities.stream().filter(req -> req.getId().equals(detailDto.getSourceDetailId())).map(SoReturnReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
            if (receiveQty <  detailDto.getRealQty() + realQty) {
                throw new ServiceException(ApiError.ERROR_92026);
            }
            detailEntity.setMainId(id);
            detailEntity.setSkuId(soReturnDetailEntity.getSkuId());
            detailEntity.setSkuNo(soReturnDetailEntity.getSkuNo());
            detailEntity.setMustQty(detailDto.getMustQty());
            detailEntity.setReceiveQty(detailDto.getReceiveQty());
            detailEntity.setRealQty(detailDto.getRealQty());
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
    public Boolean update(SoReturnInstockDTO.Update dto) {
//获取退货单详情表id
        List<String> returnDetailIds = dto.getDetailList().stream().map(SoReturnInstockDetailDTO.Update::getSourceDetailId).collect(Collectors.toList());
        List<SoReturnDetailEntity> soReturnDetailEntities = soReturnFeign.listDetailByIds(returnDetailIds);
        List<SoReturnReceiveDetailEntity> soReturnReceiveDetailEntities = soReturnReceiveDetailService.listDetailByIds(returnDetailIds);
        List<SoReturnInstockDetailEntity> soReturnInstockDetailEntities = this.listDetailBySourceDetailIds(returnDetailIds);
        List<SoReturnInstockDetailEntity> list = new ArrayList<>();
        for (SoReturnInstockDetailDTO.Update detailDto : dto.getDetailList()) {
            SoReturnInstockDetailEntity detailEntity = new SoReturnInstockDetailEntity();
            SoReturnDetailEntity soReturnDetailEntity = soReturnDetailEntities.stream().filter(req -> req.getId().equals(detailDto.getSourceDetailId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(soReturnDetailEntity)) {
                throw new ServiceException(ApiError.ERROR_92023);
            }
            //实退 入库数量
            Integer realQty = soReturnInstockDetailEntities.stream().filter(req -> req.getSourceDetailId().equals(detailDto.getSourceDetailId())).map(SoReturnInstockDetailEntity::getRealQty).reduce(MathUtil.ZERO, Integer::sum);
            //签收单数量
            Integer receiveQty = soReturnReceiveDetailEntities.stream().filter(req -> req.getId().equals(detailDto.getSourceDetailId())).map(SoReturnReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
            if (StringUtils.isBlank(detailDto.getId())) {
                realQty = soReturnInstockDetailEntities.stream().filter(req -> req.getSourceDetailId().equals(detailDto.getSourceDetailId()) && req.getId() != detailDto.getId()).map(SoReturnInstockDetailEntity::getRealQty).reduce(MathUtil.ZERO, Integer::sum);
            }
            if (receiveQty <  detailDto.getRealQty() + realQty) {
                throw new ServiceException(ApiError.ERROR_92026);
            }
            detailEntity.setMainId(detailDto.getId());
            detailEntity.setSkuId(soReturnDetailEntity.getSkuId());
            detailEntity.setSkuNo(soReturnDetailEntity.getSkuNo());
            detailEntity.setMustQty(detailDto.getMustQty());
            detailEntity.setReceiveQty(detailDto.getReceiveQty());
            detailEntity.setRealQty(detailDto.getRealQty());
            detailEntity.setReturnTypeDict(detailDto.getReturnTypeDict());
            detailEntity.setReturnReasonDict(detailDto.getReturnReasonDict());
            detailEntity.setRemark(detailDto.getRemark());
            detailEntity.setSourceDetailId(detailDto.getSourceDetailId());
            list.add(detailEntity);
        }
        return this.saveOrUpdateBatch(list);
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean delete(List<String> mainIds) {
        return null;
    }

    @Override
    public List<SoReturnInstockDetailEntity> listDetailBySourceIds(List<String> sourceIds) {
        return baseMapper.listDetailBySourceIds(sourceIds);
    }

    @Override
    public List<SoReturnInstockDetailEntity> listDetailByMainId(String id) {
        return null;
    }

    @Override
    public List<SoReturnInstockDetailEntity> listDetailBySourceDetailIds(List<String> sourceDetailIds) {
        return baseMapper.listDetailBySourceDetailIds(sourceDetailIds);
    }
}
