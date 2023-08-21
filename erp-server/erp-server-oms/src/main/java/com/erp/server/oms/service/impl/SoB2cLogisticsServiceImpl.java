package com.erp.server.oms.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.erp.model.oms.dto.SoB2cLogisticsDTO;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.erp.server.oms.mapper.SoB2cLogisticsMapper;
import com.erp.server.oms.service.SoB2cLogisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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
        return null;
    }

    @Override
    public Boolean update(SoB2cLogisticsDTO.UpdateDTO logisticsDTO, String mainId) {
        return null;
    }
}
