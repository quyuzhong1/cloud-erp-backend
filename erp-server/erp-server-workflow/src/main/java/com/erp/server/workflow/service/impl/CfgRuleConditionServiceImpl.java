package com.erp.server.workflow.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.workflow.entity.CfgRuleConditionEntity;
import com.erp.server.workflow.mapper.CfgRuleConditionMapper;
import com.erp.server.workflow.service.CfgRuleConditionService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.workflow.service.OperateLogService;
import com.erp.server.workflow.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.workflow.dto.CfgRuleConditionDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 规则条件表 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-05-12
 */
@Slf4j
@Service
public class CfgRuleConditionServiceImpl extends SuperServiceImpl<CfgRuleConditionMapper, CfgRuleConditionEntity> implements CfgRuleConditionService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgRuleConditionDTO.AddDTO addDTO) {
        CfgRuleConditionEntity cfgRuleConditionEntity = new CfgRuleConditionEntity();
        BeanMapperUtils.copy(addDTO, cfgRuleConditionEntity);

        // 数据处理
        handleData(cfgRuleConditionEntity);

        log.info("开始新增规则条件单");
        boolean save = super.save(cfgRuleConditionEntity);
        if(!save) {
            throw new ServiceException("规则条件单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "规则条件单" , cfgRuleConditionEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, cfgRuleConditionEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(cfgRuleConditionEntity.getId(), cfgRuleConditionEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgRuleConditionDTO.UpdateDTO addOrUpdateDTO) {
        CfgRuleConditionEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "规则条件单"));
        CfgRuleConditionEntity cfgRuleConditionEntity =  BeanMapperUtils.map(CfgRuleConditionEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(cfgRuleConditionEntity);
        log.info("编辑 开始修改规则条件单数据，id：【{}】", old.getId());
        boolean save = super.updateById(cfgRuleConditionEntity);
        if(!save) {
            throw new ServiceException("规则条件单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录规则条件单日志数据，id：【{}】", cfgRuleConditionEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), cfgRuleConditionEntity.getId(), "规则条件单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, cfgRuleConditionEntity, null, cfgRuleConditionEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(CfgRuleConditionEntity cfgRuleConditionEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
