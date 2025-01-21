package com.erp.server.dmp.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.dto.RulePromptWordDTO;
import com.erp.model.dmp.entity.RulePromptWordEntity;
import com.erp.server.dmp.mapper.RulePromptWordMapper;
import com.erp.server.dmp.service.OperateLogService;
import com.erp.server.dmp.service.RulePromptWordService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
/**
 * <p>
 * 汉化管理规则表 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2025-01-17
 */
@Slf4j
@Service
public class RulePromptWordServiceImpl extends SuperServiceImpl<RulePromptWordMapper, RulePromptWordEntity> implements RulePromptWordService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(RulePromptWordDTO.AddDTO addDTO) {
        RulePromptWordEntity rulePromptWordEntity = new RulePromptWordEntity();
        BeanMapperUtils.copy(addDTO, rulePromptWordEntity);

        // 数据处理
        handleData(rulePromptWordEntity);

        log.info("开始新增汉化管理规则单");
        boolean save = super.save(rulePromptWordEntity);
        if(!save) {
            throw new ServiceException("汉化管理规则单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "汉化管理规则单" , rulePromptWordEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, rulePromptWordEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(rulePromptWordEntity.getId(), rulePromptWordEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(RulePromptWordDTO.UpdateDTO addOrUpdateDTO) {
        RulePromptWordEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "汉化管理规则单"));
        RulePromptWordEntity rulePromptWordEntity =  BeanMapperUtils.map(RulePromptWordEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(rulePromptWordEntity);
        log.info("编辑 开始修改汉化管理规则单数据，id：【{}】", old.getId());
        boolean save = super.updateById(rulePromptWordEntity);
        if(!save) {
            throw new ServiceException("汉化管理规则单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录汉化管理规则单日志数据，id：【{}】", rulePromptWordEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), rulePromptWordEntity.getId(), "汉化管理规则单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, rulePromptWordEntity, null, rulePromptWordEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(RulePromptWordEntity rulePromptWordEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
