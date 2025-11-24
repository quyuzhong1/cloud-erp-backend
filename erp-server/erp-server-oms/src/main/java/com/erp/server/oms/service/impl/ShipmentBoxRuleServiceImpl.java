package com.erp.server.oms.service.impl;


import cn.hutool.core.util.StrUtil;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.oms.entity.ShipmentBoxRuleEntity;
import com.erp.server.oms.mapper.ShipmentBoxRuleMapper;
import com.erp.server.oms.service.ShipmentBoxRuleService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.oms.dto.ShipmentBoxRuleDTO;
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
public class ShipmentBoxRuleServiceImpl extends SuperServiceImpl<ShipmentBoxRuleMapper, ShipmentBoxRuleEntity> implements ShipmentBoxRuleService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(ShipmentBoxRuleDTO.AddDTO addDTO) {
        ShipmentBoxRuleEntity shipmentBoxRuleEntity = new ShipmentBoxRuleEntity();
        BeanMapperUtils.copy(addDTO, shipmentBoxRuleEntity);

        // 数据处理
        handleData(shipmentBoxRuleEntity);

        log.info("开始新增");
        boolean save = super.save(shipmentBoxRuleEntity);
        if(!save) {
            throw new ServiceException("保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "" , shipmentBoxRuleEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, shipmentBoxRuleEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(shipmentBoxRuleEntity.getId(), shipmentBoxRuleEntity.getId());
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(ShipmentBoxRuleDTO.UpdateDTO addOrUpdateDTO) {
        ShipmentBoxRuleEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, ""));
        ShipmentBoxRuleEntity shipmentBoxRuleEntity =  BeanMapperUtils.map(ShipmentBoxRuleEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(shipmentBoxRuleEntity);
        log.info("编辑 开始修改数据，id：【{}】", old.getId());
        boolean save = super.updateById(shipmentBoxRuleEntity);
        if(!save) {
            throw new ServiceException("保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录日志数据，id：【{}】", shipmentBoxRuleEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), shipmentBoxRuleEntity.getId(), "");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, shipmentBoxRuleEntity, null, shipmentBoxRuleEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(ShipmentBoxRuleEntity shipmentBoxRuleEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
