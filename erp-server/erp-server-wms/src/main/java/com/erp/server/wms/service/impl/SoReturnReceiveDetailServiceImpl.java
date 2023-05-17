package com.erp.server.wms.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.common.business.enums.ApproveStatusEnum;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.oms.entity.SoReturnDetailEntity;
import com.erp.model.oms.entity.SoReturnEntity;
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
import com.erp.server.wms.service.SoDeliveryNoticeDetailService;
import com.erp.server.wms.service.SoOutstockDetailService;
import com.erp.server.wms.service.SoReturnReceiveDetailService;
import com.common.business.service.SuperServiceImpl;
import org.apache.commons.collections4.CollectionUtils;
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

    @Override
    public Boolean add(SoReturnReceiveDTO.Add dto, String id) {
        //获取退货详情
        List<SoReturnDetailEntity> returnDetailEntityList = soReturnFeign.listDetailByMainId(dto.getSourceId());
        //获取销售订单明细表id
        List<String> soDetailIds = returnDetailEntityList.stream().map(SoReturnDetailEntity::getSourceDetailId).collect(Collectors.toList());
        //根据销售单详情id获取发货通知单详情信息
        List<SoDeliveryNoticeDetailEntity> detailEntityList = soDeliveryNoticeDetailService.listDetailBySourceDetailIds(soDetailIds);
        //获取发货通知单明细表id
        List<String> deliveryNoticeDetailIdList = detailEntityList.stream().map(SoDeliveryNoticeDetailEntity::getId).collect(Collectors.toList());
        soDetailIds.addAll(deliveryNoticeDetailIdList);
        //根据销售单获取出库单
        List<SoOutstockDetailEntity> soOutstockDetailEntities = soOutstockDetailService.listDetailBySourceDetailId(soDetailIds);
        //获取退货单详情表id
        List<String> returnDetailIds = dto.getDetailList().stream().map(SoReturnReceiveDetailDTO.Add::getSourceDetailId).collect(Collectors.toList());
        List<SoReturnDetailEntity> soReturnDetailEntities = soReturnFeign.listDetailByIds(returnDetailIds);
        List<SoReturnReceiveDetailEntity> list = new ArrayList<>();
        for (SoReturnReceiveDetailDTO.Add detailDto : dto.getDetailList()) {
            SoReturnReceiveDetailEntity detailEntity = new SoReturnReceiveDetailEntity();
            SoReturnDetailEntity soReturnDetailEntity = soReturnDetailEntities.stream().filter(req -> req.getSourceDetailId().equals(detailDto.getSourceDetailId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(soReturnDetailEntity)) {
                throw new ServiceException(ApiError.ERROR_92016);
            }
            Integer actualQty = soOutstockDetailEntities.stream().filter(detail -> detail.getSkuId().equals(soReturnDetailEntity.getSkuId()) && detail.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())).map(SoOutstockDetailEntity::getActualQty).reduce(MathUtil.ZERO, Integer::sum);
            Integer returnQty = soReturnDetailEntities.stream().filter(req -> req.getSourceDetailId().equals(detailDto.getSourceDetailId())).map(SoReturnDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
            if (actualQty < detailDto.getReturnQty() + returnQty) {
                throw new ServiceException(ApiError.ERROR_92009);
            }
            if (detailDto.getReceiveQty() < returnQty) {
                throw new ServiceException(ApiError.ERROR_92009);
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
    public Boolean update(SoReturnReceiveDTO.Update dto) {
        return null;
    }

    @Override
    public Boolean delete(List<String> mainIds) {
        return null;
    }

    @Override
    public List<SoReturnReceiveDetailEntity> listDetailBySourceIds(List<String> sourceIds) {
        return null;
    }

    @Override
    public List<SoReturnReceiveDetailEntity> listDetailByMainId(String id) {
        return null;
    }
}
