package com.erp.server.oms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.oms.entity.SoB2cErrorEntity;
import com.erp.server.oms.mapper.SoB2cErrorMapper;
import com.erp.server.oms.service.SoB2cErrorService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.CommonService;
import com.common.core.exception.ServiceException;
import com.erp.server.oms.service.SoB2cService;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.oms.dto.SoB2cErrorDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;

/**
 * <p>
 * B2C销售订单异常表 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-12-20
 */
@Slf4j
@Service
public class SoB2cErrorServiceImpl extends SuperServiceImpl<SoB2cErrorMapper, SoB2cErrorEntity> implements SoB2cErrorService {

    @Resource
    private SoB2cService soB2cService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean add(SoB2cErrorDTO.AddDTO addDTO) {
        SoB2cErrorEntity soB2cErrorEntity = new SoB2cErrorEntity();
        BeanMapperUtils.copy(addDTO, soB2cErrorEntity);
        // 数据处理
        handleData(soB2cErrorEntity);
        boolean save = super.save(soB2cErrorEntity);
        if(!save) {
            throw new ServiceException("B2C销售订单异常单保存失败");
        }
        soB2cService.addSignError(soB2cErrorEntity.getMainId(),soB2cErrorEntity.getType());
        return true;
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(SoB2cErrorDTO.UpdateDTO updateDTO) {
        SoB2cErrorEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "B2C销售订单异常单"));
        SoB2cErrorEntity soB2cErrorEntity =  BeanMapperUtils.map(SoB2cErrorEntity.class, updateDTO);
        // 数据处理
        handleData(soB2cErrorEntity);
        boolean save = super.updateById(soB2cErrorEntity);
        if(!save) {
            throw new ServiceException("B2C销售订单异常单保存失败");
        }
        return Boolean.TRUE;
    }


    /** 
     * @description
     * @param dto
     * @author Lambda
     * @return 
     * @create 2023-12-20 11:24
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(SoB2cErrorDTO.DeleteDTO dto) {
        Boolean result = baseMapper.deleteB2cError(dto);
        if(result){
            soB2cService.removeSignError(dto.getMainId(),dto.getType());
        }
        return result;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(SoB2cErrorEntity soB2cErrorEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
