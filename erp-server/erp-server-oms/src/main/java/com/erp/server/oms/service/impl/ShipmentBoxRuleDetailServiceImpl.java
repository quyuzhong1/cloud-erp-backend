package com.erp.server.oms.service.impl;


import cn.hutool.core.util.StrUtil;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.oms.entity.ShipmentBoxRuleDetailEntity;
import com.erp.server.oms.mapper.ShipmentBoxRuleDetailMapper;
import com.erp.server.oms.service.ShipmentBoxRuleDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.oms.dto.ShipmentBoxRuleDetailDTO;
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
public class ShipmentBoxRuleDetailServiceImpl extends SuperServiceImpl<ShipmentBoxRuleDetailMapper, ShipmentBoxRuleDetailEntity> implements ShipmentBoxRuleDetailService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(ShipmentBoxRuleDetailDTO.AddDTO addDTO) {
        ShipmentBoxRuleDetailEntity shipmentBoxRuleDetailEntity = new ShipmentBoxRuleDetailEntity();
        BeanMapperUtils.copy(addDTO, shipmentBoxRuleDetailEntity);

        // 数据处理
        handleData(shipmentBoxRuleDetailEntity);

        log.info("开始新增");
        boolean save = super.save(shipmentBoxRuleDetailEntity);
        if(!save) {
            throw new ServiceException("保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "" , shipmentBoxRuleDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, shipmentBoxRuleDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(shipmentBoxRuleDetailEntity.getId(), shipmentBoxRuleDetailEntity.getId());
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(ShipmentBoxRuleDetailDTO.UpdateDTO addOrUpdateDTO) {
        ShipmentBoxRuleDetailEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, ""));
        ShipmentBoxRuleDetailEntity shipmentBoxRuleDetailEntity =  BeanMapperUtils.map(ShipmentBoxRuleDetailEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(shipmentBoxRuleDetailEntity);
        log.info("编辑 开始修改数据，id：【{}】", old.getId());
        boolean save = super.updateById(shipmentBoxRuleDetailEntity);
        if(!save) {
            throw new ServiceException("保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录日志数据，id：【{}】", shipmentBoxRuleDetailEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), shipmentBoxRuleDetailEntity.getId(), "");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, shipmentBoxRuleDetailEntity, null, shipmentBoxRuleDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(ShipmentBoxRuleDetailEntity shipmentBoxRuleDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
