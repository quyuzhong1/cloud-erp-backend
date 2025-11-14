package com.erp.server.dmp.service.impl;


import cn.hutool.core.util.StrUtil;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.dmp.entity.CfgDiffStrategyConditionEntity;
import com.erp.server.dmp.mapper.CfgDiffStrategyConditionMapper;
import com.erp.server.dmp.service.CfgDiffStrategyConditionService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.dmp.service.OperateLogService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.dmp.dto.CfgDiffStrategyConditionDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 差异策略配置条件 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2025-11-11
 */
@Slf4j
@Service
public class CfgDiffStrategyConditionServiceImpl extends SuperServiceImpl<CfgDiffStrategyConditionMapper, CfgDiffStrategyConditionEntity> implements CfgDiffStrategyConditionService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgDiffStrategyConditionDTO.AddDTO addDTO) {
        CfgDiffStrategyConditionEntity cfgDiffStrategyConditionEntity = new CfgDiffStrategyConditionEntity();
        BeanMapperUtils.copy(addDTO, cfgDiffStrategyConditionEntity);

        // 数据处理
        handleData(cfgDiffStrategyConditionEntity);

        log.info("开始新增差异策略配置条件");
        boolean save = super.save(cfgDiffStrategyConditionEntity);
        if(!save) {
            throw new ServiceException("差异策略配置条件保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "差异策略配置条件" , cfgDiffStrategyConditionEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, cfgDiffStrategyConditionEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(cfgDiffStrategyConditionEntity.getId(), cfgDiffStrategyConditionEntity.getId());
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgDiffStrategyConditionDTO.UpdateDTO addOrUpdateDTO) {
        CfgDiffStrategyConditionEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "差异策略配置条件"));
        CfgDiffStrategyConditionEntity cfgDiffStrategyConditionEntity =  BeanMapperUtils.map(CfgDiffStrategyConditionEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(cfgDiffStrategyConditionEntity);
        log.info("编辑 开始修改差异策略配置条件数据，id：【{}】", old.getId());
        boolean save = super.updateById(cfgDiffStrategyConditionEntity);
        if(!save) {
            throw new ServiceException("差异策略配置条件保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录差异策略配置条件日志数据，id：【{}】", cfgDiffStrategyConditionEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), cfgDiffStrategyConditionEntity.getId(), "差异策略配置条件");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, cfgDiffStrategyConditionEntity, null, cfgDiffStrategyConditionEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(CfgDiffStrategyConditionEntity cfgDiffStrategyConditionEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
