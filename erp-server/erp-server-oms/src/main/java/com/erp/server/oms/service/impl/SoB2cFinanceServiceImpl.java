package com.erp.server.oms.service.impl;


import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.dto.SoB2cFinanceDTO;
import com.erp.model.oms.entity.SoB2cFinanceEntity;
import com.erp.server.oms.mapper.SoB2cFinanceMapper;
import com.erp.server.oms.service.CommonService;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.SoB2cFinanceService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
/**
 * <p>
 * B2C销售订单财务信息表 服务实现类
 * </p>
 *
 * @author will
 * @since 2023-09-08
 */
@Slf4j
@Service
public class SoB2cFinanceServiceImpl extends SuperServiceImpl<SoB2cFinanceMapper, SoB2cFinanceEntity> implements SoB2cFinanceService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String add(SoB2cFinanceDTO.AddDTO addDTO) {
        SoB2cFinanceEntity soB2cFinanceEntity = new SoB2cFinanceEntity();
        BeanMapperUtils.copy(addDTO, soB2cFinanceEntity);

        // 数据处理
        handleData(soB2cFinanceEntity);

        log.info("开始新增B2C销售订单财务信息单");
        boolean save = super.save(soB2cFinanceEntity);
        if(!save) {
            throw new ServiceException("B2C销售订单财务信息单保存失败");
        }
        return soB2cFinanceEntity.getId();
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(SoB2cFinanceDTO.UpdateDTO updateDTO) {
        SoB2cFinanceEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "B2C销售订单财务信息单"));
        SoB2cFinanceEntity soB2cFinanceEntity =  BeanMapperUtils.map(SoB2cFinanceEntity.class, updateDTO);

        // 数据处理
        handleData(soB2cFinanceEntity);
        log.info("编辑 开始修改B2C销售订单财务信息单数据，id：【{}】", old.getId());
        boolean save = super.updateById(soB2cFinanceEntity);
        if(!save) {
            throw new ServiceException("B2C销售订单财务信息单保存失败");
        }

        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(SoB2cFinanceEntity soB2cFinanceEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
