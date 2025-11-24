package com.erp.server.oms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.erp.model.oms.dto.DeliveryBoxRuleDetailDTO;
import com.erp.model.oms.entity.DeliveryBoxRuleDetailEntity;
import com.erp.server.oms.mapper.DeliveryBoxRuleDetailMapper;
import com.erp.server.oms.service.DeliveryBoxRuleDetailService;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.oms.service.OperateLogService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author wtr
 * @since 2025-11-24
 */
@Slf4j
@Service
public class DeliveryBoxRuleDetailServiceImpl extends SuperServiceImpl<DeliveryBoxRuleDetailMapper, DeliveryBoxRuleDetailEntity> implements DeliveryBoxRuleDetailService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DeliveryBoxRuleDetailDTO.AddDTO addDTO) {
        DeliveryBoxRuleDetailEntity deliveryBoxRuleDetailEntity = new DeliveryBoxRuleDetailEntity();
        BeanMapperUtils.copy(addDTO, deliveryBoxRuleDetailEntity);

        // 数据处理
        handleData(deliveryBoxRuleDetailEntity);

        log.info("开始新增");
        boolean save = super.save(deliveryBoxRuleDetailEntity);
        if(!save) {
            throw new ServiceException("保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "" , deliveryBoxRuleDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, deliveryBoxRuleDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(deliveryBoxRuleDetailEntity.getId(), deliveryBoxRuleDetailEntity.getId());
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DeliveryBoxRuleDetailDTO.UpdateDTO addOrUpdateDTO) {
        DeliveryBoxRuleDetailEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, ""));
        DeliveryBoxRuleDetailEntity deliveryBoxRuleDetailEntity =  BeanMapperUtils.map(DeliveryBoxRuleDetailEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(deliveryBoxRuleDetailEntity);
        log.info("编辑 开始修改数据，id：【{}】", old.getId());
        boolean save = super.updateById(deliveryBoxRuleDetailEntity);
        if(!save) {
            throw new ServiceException("保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录日志数据，id：【{}】", deliveryBoxRuleDetailEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), deliveryBoxRuleDetailEntity.getId(), "");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, deliveryBoxRuleDetailEntity, null, deliveryBoxRuleDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DeliveryBoxRuleDetailEntity deliveryBoxRuleDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
