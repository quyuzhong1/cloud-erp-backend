package com.erp.server.oms.service.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.dto.SoB2cReceiverDTO;
import com.erp.model.oms.entity.CustomerB2cEntity;
import com.erp.model.oms.entity.SoB2cReceiverEntity;
import com.erp.server.oms.mapper.SoB2cReceiverMapper;
import com.erp.server.oms.service.CustomerB2cService;
import com.erp.server.oms.service.SoB2cReceiverService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

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

    @Resource
    private CustomerB2cService customerB2cService;

    @Override
    public Boolean add(SoB2cReceiverDTO.AddDTO receiverDTO, String mainId) {
        SoB2cReceiverEntity entity = new SoB2cReceiverEntity();
        BeanMapperUtils.copy(receiverDTO,entity);
        //处理买家信息
        handleSoB2cReceiver(entity,mainId);
        return this.save(entity);
    }

    @Override
    public Boolean update(SoB2cReceiverDTO.UpdateDTO receiverDTO, String mainId) {
        SoB2cReceiverEntity entity = new SoB2cReceiverEntity();
        BeanMapperUtils.copy(receiverDTO,entity);
        //处理买家信息
        handleSoB2cReceiver(entity,mainId);
        return this.updateById(entity);
    }

    @Override
    public SoB2cReceiverEntity getByMainId(String mainId) {
        return lambdaQuery().eq(SoB2cReceiverEntity::getMainId,mainId).one();
    }

    @Override
    public List<SoB2cReceiverEntity> listByMainIds(List<String> mainIds) {
        return lambdaQuery().in(SoB2cReceiverEntity::getMainId,mainIds).list();
    }

    @Override
    public Boolean deleteByMainIds(List<String> mainIds) {
        return lambdaUpdate().in(SoB2cReceiverEntity::getMainId,mainIds).remove();
    }

    /**
     * @description: 
     * @author Will
     * @date: 2023/8/31 9:56
     * @param entity
     
     */
    private void handleSoB2cReceiver (SoB2cReceiverEntity entity,String mainId) {
        //验证地址信息
        if (StringUtils.isBlank(entity.getFirstAddress()) && StringUtils.isBlank(entity.getSecondAddress())
                && StringUtils.isBlank(entity.getFullAddress())) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_RECEIVER_ADDRESS_NOT_NULL);
        }

        CustomerB2cEntity customerB2cEntity = customerB2cService.getById(entity.getCustomerId());
        if (ObjectUtils.isNotEmpty(customerB2cEntity)) {
            entity.setName(customerB2cEntity.getName());
        }
        entity.setMainId(mainId);
    }
}
