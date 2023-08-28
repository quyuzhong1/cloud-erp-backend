package com.erp.server.oms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.erp.model.oms.entity.RuleConditionEntity;
import com.erp.server.oms.mapper.RuleConditionMapper;
import com.erp.server.oms.service.RuleConditionService;
import com.common.business.service.SuperServiceImpl;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.oms.dto.RuleConditionDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 规则条件表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-08-28
 */
@Slf4j
@Service
public class RuleConditionServiceImpl extends SuperServiceImpl<RuleConditionMapper, RuleConditionEntity> implements RuleConditionService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String add(RuleConditionDTO.AddDTO addDTO) {
        RuleConditionEntity ruleConditionEntity = new RuleConditionEntity();
        BeanMapperUtils.copy(addDTO, ruleConditionEntity);

        // 数据处理
        handleData(ruleConditionEntity);

        log.info("开始新增规则条件单");
        boolean save = super.save(ruleConditionEntity);
        if(!save) {
            throw new ServiceException("规则条件单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "规则条件单" , ruleConditionEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, ruleConditionEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）
        return ruleConditionEntity.getId();
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(RuleConditionDTO.UpdateDTO updateDTO) {
        RuleConditionEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "规则条件单"));
        RuleConditionEntity ruleConditionEntity =  BeanMapperUtils.map(RuleConditionEntity.class, updateDTO);

        // 数据处理
        handleData(ruleConditionEntity);
        log.info("编辑 开始修改规则条件单数据，id：【{}】", old.getId());
        boolean save = super.updateById(ruleConditionEntity);
        if(!save) {
            throw new ServiceException("规则条件单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录规则条件单日志数据，id：【{}】", ruleConditionEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), ruleConditionEntity.getId(), "规则条件单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, ruleConditionEntity, null, ruleConditionEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(RuleConditionEntity ruleConditionEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
