package com.erp.server.oms.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.dto.SoB2cLogisticsDTO;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.erp.server.oms.mapper.SoB2cLogisticsMapper;
import com.erp.server.oms.service.SoB2cLogisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * B2C销售订单物流信息表 服务实现类
 * </p>
 *
 * @author Will
 * @since 2023-08-18
 */
@Slf4j
@Service
public class SoB2cLogisticsServiceImpl extends SuperServiceImpl<SoB2cLogisticsMapper, SoB2cLogisticsEntity> implements SoB2cLogisticsService {


    @Override
    public Boolean add(SoB2cLogisticsDTO.AddDTO logisticsDTO, String mainId) {
        SoB2cLogisticsEntity entity = new SoB2cLogisticsEntity();
        BeanMapperUtils.copy(logisticsDTO,entity);
        entity.setMainId(mainId);
        return this.save(entity);
    }

    @Override
    public Boolean update(SoB2cLogisticsDTO.UpdateDTO logisticsDTO, String mainId) {
        SoB2cLogisticsEntity entity = new SoB2cLogisticsEntity();
        BeanMapperUtils.copy(logisticsDTO,entity);
        entity.setMainId(mainId);
        return this.updateById(entity);
    }

    @Override
    public SoB2cLogisticsEntity getByMainId(String mainId) {
        return lambdaQuery().eq(SoB2cLogisticsEntity::getId,mainId).one();
    }

    @Override
    public List<SoB2cLogisticsEntity> listByMainIds(List<String> mainIds) {
        return lambdaQuery().in(SoB2cLogisticsEntity::getId,mainIds).list();
    }

    @Override
    public Boolean deleteByMainIds(List<String> mainIds) {
        return lambdaUpdate().in(SoB2cLogisticsEntity::getMainId,mainIds).remove();
    }

    @Override
    public Boolean updateLogisticsCode(String mainId, String logisticsCode) {
        return lambdaUpdate().eq(SoB2cLogisticsEntity::getMainId,mainId).set(SoB2cLogisticsEntity::getCode,logisticsCode).update(new SoB2cLogisticsEntity());
    }
}
