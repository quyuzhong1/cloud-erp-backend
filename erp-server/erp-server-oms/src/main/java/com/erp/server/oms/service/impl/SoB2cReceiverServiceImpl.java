package com.erp.server.oms.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.dto.SoB2cReceiverDTO;
import com.erp.model.oms.entity.SoB2cReceiverEntity;
import com.erp.server.oms.mapper.SoB2cReceiverMapper;
import com.erp.server.oms.service.SoB2cReceiverService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
/**
 * <p>
 * B2C销售订单买家信息表 服务实现类
 * </p>
 *
 * @author Will
 * @since 2023-08-18
 */
@Slf4j
@Service
public class SoB2cReceiverServiceImpl extends SuperServiceImpl<SoB2cReceiverMapper, SoB2cReceiverEntity> implements SoB2cReceiverService {


    @Override
    public Boolean add(SoB2cReceiverDTO.AddDTO receiverDTO, String mainId) {
        SoB2cReceiverEntity entity = new SoB2cReceiverEntity();
        BeanMapperUtils.copy(receiverDTO,entity);
        entity.setMainId(mainId);
        return this.save(entity);
    }

    @Override
    public Boolean update(SoB2cReceiverDTO.UpdateDTO receiverDTO, String mainId) {
        SoB2cReceiverEntity entity = new SoB2cReceiverEntity();
        BeanMapperUtils.copy(receiverDTO,entity);
        entity.setMainId(mainId);
        return this.updateById(entity);
    }

    @Override
    public SoB2cReceiverEntity getByMainId(String mainId) {
        return lambdaQuery().eq(SoB2cReceiverEntity::getId,mainId).one();
    }
}
