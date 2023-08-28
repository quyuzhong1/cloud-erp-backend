package com.erp.server.oms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.erp.model.oms.entity.RuleDeliveryWarehouseEntity;
import com.erp.server.oms.mapper.RuleDeliveryWarehouseMapper;
import com.erp.server.oms.service.RuleDeliveryWarehouseService;
import com.common.business.service.SuperServiceImpl;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.oms.dto.RuleDeliveryWarehouseDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 发货仓库规则表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-08-28
 */
@Slf4j
@Service
public class RuleDeliveryWarehouseServiceImpl extends SuperServiceImpl<RuleDeliveryWarehouseMapper, RuleDeliveryWarehouseEntity> implements RuleDeliveryWarehouseService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String add(RuleDeliveryWarehouseDTO.AddDTO addDTO) {
        RuleDeliveryWarehouseEntity ruleDeliveryWarehouseEntity = new RuleDeliveryWarehouseEntity();
        BeanMapperUtils.copy(addDTO, ruleDeliveryWarehouseEntity);

        // 数据处理
        handleData(ruleDeliveryWarehouseEntity);

        log.info("开始新增发货仓库规则单");
        boolean save = super.save(ruleDeliveryWarehouseEntity);
        if(!save) {
            throw new ServiceException("发货仓库规则单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "发货仓库规则单" , ruleDeliveryWarehouseEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, ruleDeliveryWarehouseEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）
        return ruleDeliveryWarehouseEntity.getId();
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(RuleDeliveryWarehouseDTO.UpdateDTO updateDTO) {
        RuleDeliveryWarehouseEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "发货仓库规则单"));
        RuleDeliveryWarehouseEntity ruleDeliveryWarehouseEntity =  BeanMapperUtils.map(RuleDeliveryWarehouseEntity.class, updateDTO);

        // 数据处理
        handleData(ruleDeliveryWarehouseEntity);
        log.info("编辑 开始修改发货仓库规则单数据，id：【{}】", old.getId());
        boolean save = super.updateById(ruleDeliveryWarehouseEntity);
        if(!save) {
            throw new ServiceException("发货仓库规则单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录发货仓库规则单日志数据，id：【{}】", ruleDeliveryWarehouseEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), ruleDeliveryWarehouseEntity.getId(), "发货仓库规则单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, ruleDeliveryWarehouseEntity, null, ruleDeliveryWarehouseEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(RuleDeliveryWarehouseEntity ruleDeliveryWarehouseEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
