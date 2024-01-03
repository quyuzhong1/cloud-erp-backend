package com.erp.server.wms.service.impl;

import com.common.business.enums.UnitEnum;
import com.common.core.constant.EnumMessage;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.wms.dto.WeightingOutboundDTO;
import com.erp.model.wms.entity.SoB2cDeliveryEntity;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.server.wms.service.SoB2cDeliveryService;
import com.erp.server.wms.service.SoOutstockService;
import com.erp.server.wms.service.WeightingOutboundService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Objects;

/**
 * @author liuruipeng
 * @date 2023年12月13日 20:03
 */
@Slf4j
@Service
public class WeightingOutboundServiceImpl implements WeightingOutboundService {

    @Resource
    private SoB2cDeliveryService soB2cDeliveryService;

    @Resource
    private SoOutstockService soOutstockService;

    @Resource
    private SoB2cFeign soB2cFeign;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WeightingOutboundDTO.ViewDTO scan(WeightingOutboundDTO.ScanDTO dto) {
        SoB2cDeliveryEntity entity = soB2cDeliveryService.getByBusinessCode(dto.getBusinessCode());
        if(Objects.isNull(entity)){
            throw new ServiceException("单号在系统不存在");
        }
        if(Objects.nonNull(dto.getWeight())){
            if(Objects.isNull(dto.getWeightUnit())){
                throw new ServiceException("称重单位不能为空");
            }
            if(Objects.isNull(EnumMessage.getNameByCode(UnitEnum.WeightUnitEnum.class,dto.getWeightUnit()))){
                throw new ServiceException("非法称重单位");
            }
            entity.setWeight(dto.getWeight());
            entity.setWeightUnit(dto.getWeightUnit());
            entity.setIsWeigh(true);
            if(!soB2cDeliveryService.updateById(entity)){
                throw new ServiceException("发货单更新失败");
            }
        }
        if(dto.getIsAutoDelivery() && entity.getIsWeigh()){
            //将发货状态更新为已发货
            entity.setStatus(SoB2cBillStatusEnum.ENUM_SHIPPED.getCode());
            if (!soB2cDeliveryService.updateById(entity)) {
                throw new ServiceException("发货单更新失败");
            }
            soB2cFeign.updateSoB2cStatus(Collections.singletonList(entity.getSourceId()),SoB2cBillStatusEnum.ENUM_SHIPPED.getCode());
            soOutstockService.generateB2cSoOutstock(entity.getSourceId());
        }
        return this.buildViewDTO(entity);
    }

    @Override
    public void reset(String id) {
        SoB2cDeliveryEntity entity = soB2cDeliveryService.getById(id);
        if(Objects.isNull(entity)){
            throw new ServiceException("查询的发货单为空");
        }
        entity.setIsWeigh(false);
        entity.setWeightUnit(UnitEnum.WeightUnitEnum.G.getCode());
        entity.setWeight(BigDecimal.ZERO);
        if(!soB2cDeliveryService.updateById(entity)){
            throw new ServiceException("发货单更新失败");
        }
    }

    private WeightingOutboundDTO.ViewDTO buildViewDTO(SoB2cDeliveryEntity entity) {
        return WeightingOutboundDTO.ViewDTO.builder()
                .id(entity.getId())
                .code(entity.getSoCode())
                .transportNo(entity.getTransportNo())
                .weight(entity.getWeight())
                .weightUnit(entity.getWeightUnit())
                .status(entity.getIsWeigh())
                .build();
    }
}
