package com.erp.server.oms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.erp.model.oms.entity.RuleRefConditionEntity;
import com.erp.server.oms.mapper.RuleRefConditionMapper;
import com.erp.server.oms.service.RuleRefConditionService;
import com.common.business.service.SuperServiceImpl;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.oms.dto.RuleRefConditionDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 规则关联条件表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-08-28
 */
@Slf4j
@Service
public class RuleRefConditionServiceImpl extends SuperServiceImpl<RuleRefConditionMapper, RuleRefConditionEntity> implements RuleRefConditionService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String add(RuleRefConditionDTO.AddDTO addDTO) {
        RuleRefConditionEntity ruleRefConditionEntity = new RuleRefConditionEntity();
        BeanMapperUtils.copy(addDTO, ruleRefConditionEntity);

        // 数据处理
        handleData(ruleRefConditionEntity);

        log.info("开始新增规则关联条件单");
        boolean save = super.save(ruleRefConditionEntity);
        if(!save) {
            throw new ServiceException("规则关联条件单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "规则关联条件单" , ruleRefConditionEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, ruleRefConditionEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）
        return ruleRefConditionEntity.getId();
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(RuleRefConditionDTO.UpdateDTO updateDTO) {
        RuleRefConditionEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "规则关联条件单"));
        RuleRefConditionEntity ruleRefConditionEntity =  BeanMapperUtils.map(RuleRefConditionEntity.class, updateDTO);

        // 数据处理
        handleData(ruleRefConditionEntity);
        log.info("编辑 开始修改规则关联条件单数据，id：【{}】", old.getId());
        boolean save = super.updateById(ruleRefConditionEntity);
        if(!save) {
            throw new ServiceException("规则关联条件单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录规则关联条件单日志数据，id：【{}】", ruleRefConditionEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), ruleRefConditionEntity.getId(), "规则关联条件单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, ruleRefConditionEntity, null, ruleRefConditionEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(RuleRefConditionEntity ruleRefConditionEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
