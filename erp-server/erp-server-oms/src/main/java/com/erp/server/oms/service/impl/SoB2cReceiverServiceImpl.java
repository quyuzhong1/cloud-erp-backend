package com.erp.server.oms.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.dto.SoB2cReceiverDTO;
import com.erp.model.oms.entity.CustomerB2cEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.erp.model.oms.entity.SoB2cReceiverEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.server.oms.mapper.SoB2cReceiverMapper;
import com.erp.server.oms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

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

    @Resource
    private CommonService commonService;

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private SoB2cService soB2cService;

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
        SoB2cReceiverEntity old = super.getById(receiverDTO.getId());
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "B2C销售订单买家信息表"));
        SoB2cReceiverEntity entity = new SoB2cReceiverEntity();
        BeanMapperUtils.copy(receiverDTO,entity);
        //处理买家信息
        handleSoB2cReceiver(entity,mainId);
        boolean update = this.updateById(entity);
        //主表信息
        SoB2cEntity soB2cEntity = soB2cService.getById(old.getMainId());
        // 记录主单操作日志
        log.info("编辑 开始记录B2C销售订单表日志数据，单号：【{}】", soB2cEntity.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), soB2cEntity.getCode(), "B2C销售订单表");
        operateLogService.addModuleOperateLogByObj(old, soB2cEntity, ModuleTypeEnum.SO_B2C.getCode(), soB2cEntity.getId(), msg);
        return update;
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
