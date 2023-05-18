package com.erp.server.wms.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.common.business.enums.ApproveStatusEnum;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.dto.SoReturnDetailDTO;
import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.oms.entity.SoReturnDetailEntity;
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
import com.erp.server.wms.service.SoDeliveryNoticeDetailService;
import com.erp.server.wms.service.SoOutstockDetailService;
import com.erp.server.wms.service.SoReturnNoticeDetailService;
import com.common.business.service.SuperServiceImpl;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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

    @Resource
    private SoDeliveryNoticeDetailService soDeliveryNoticeDetailService;

    @Resource
    private SoOutstockDetailService soOutstockDetailService;

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean add(SoReturnNoticeDTO.Add dto, String id) {
        //获取退货单详情表id
        List<String> returnDetailIds = dto.getDetailList().stream().map(SoReturnNoticeDetailDTO.Add::getSourceDetailId).collect(Collectors.toList());
        List<SoReturnDetailEntity> soReturnDetailEntities = soReturnFeign.listDetailByIds(returnDetailIds);
        List<SoReturnNoticeDetailEntity> noticeDetailEntities = this.listDetailBySourceDetailIds(returnDetailIds);
        List<SoReturnNoticeDetailEntity> list = new ArrayList<>();
        for (SoReturnNoticeDetailDTO.Add detailDto : dto.getDetailList()) {
            SoReturnNoticeDetailEntity detailEntity = new SoReturnNoticeDetailEntity();
            SoReturnDetailEntity soReturnDetailEntity = soReturnDetailEntities.stream().filter(req -> req.getSourceDetailId().equals(detailDto.getSourceDetailId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(soReturnDetailEntity)) {
                throw new ServiceException(ApiError.ERROR_92016);
            }
            //退货通知单数量
            Integer returnNoticeQty = noticeDetailEntities.stream().filter(req -> req.getSourceDetailId().equals(detailDto.getSourceDetailId())).map(SoReturnNoticeDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
            //退货单数量
            Integer returnQty = soReturnDetailEntities.stream().filter(req -> req.getSourceDetailId().equals(detailDto.getSourceDetailId())).map(SoReturnDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
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
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean update(SoReturnNoticeDTO.Update dto) {
        //获取退货单详情表id
        List<String> returnDetailIds = dto.getDetailList().stream().map(SoReturnNoticeDetailDTO.Update::getSourceDetailId).collect(Collectors.toList());
        List<SoReturnDetailEntity> soReturnDetailEntities = soReturnFeign.listDetailByIds(returnDetailIds);
        List<SoReturnNoticeDetailEntity> list = new ArrayList<>();
        List<SoReturnNoticeDetailEntity> noticeDetailEntities = this.listDetailBySourceDetailIds(returnDetailIds);
        for (SoReturnNoticeDetailDTO.Update detailDto : dto.getDetailList()) {
            SoReturnNoticeDetailEntity detailEntity = new SoReturnNoticeDetailEntity();
            //退货通知单数量
            Integer returnNoticeQty = noticeDetailEntities.stream().filter(req -> req.getSourceDetailId().equals(detailDto.getSourceDetailId())).map(SoReturnNoticeDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
            if (StringUtils.isNotBlank(detailDto.getId())) {
                detailEntity.setId(detailDto.getId());
                returnNoticeQty = noticeDetailEntities.stream().filter(req -> req.getSourceDetailId().equals(detailDto.getSourceDetailId()) && req.getId() != detailDto.getId()).map(SoReturnNoticeDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
            }
            SoReturnDetailEntity soReturnDetailEntity = soReturnDetailEntities.stream().filter(req -> req.getSourceDetailId().equals(detailDto.getSourceDetailId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(soReturnDetailEntity)) {
                throw new ServiceException(ApiError.ERROR_92016);
            }
             //退货单数量
            Integer returnQty = soReturnDetailEntities.stream().filter(req -> req.getSourceDetailId().equals(detailDto.getSourceDetailId())).map(SoReturnDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
            if (returnQty <  detailDto.getReturnQty() + returnNoticeQty) {
                throw new ServiceException(ApiError.ERROR_92024);
            }
            detailEntity.setId(detailDto.getId());
            detailEntity.setMainId(detailDto.getMainId());
            detailEntity.setSkuId(soReturnDetailEntity.getSkuId());
            detailEntity.setSkuNo(soReturnDetailEntity.getSkuNo());
            detailEntity.setReturnQty(detailDto.getReturnQty());
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
