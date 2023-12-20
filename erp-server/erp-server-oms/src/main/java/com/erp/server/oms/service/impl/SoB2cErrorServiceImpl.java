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
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.oms.dto.SoB2cErrorDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
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
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
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
        log.info("编辑 开始修改B2C销售订单异常单数据，id：【{}】", old.getId());
        boolean save = super.updateById(soB2cErrorEntity);
        if(!save) {
            throw new ServiceException("B2C销售订单异常单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录B2C销售订单异常单日志数据，id：【{}】", soB2cErrorEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), soB2cErrorEntity.getId(), "B2C销售订单异常单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, soB2cErrorEntity, null, soB2cErrorEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(SoB2cErrorEntity soB2cErrorEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
