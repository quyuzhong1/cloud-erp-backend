package com.erp.server.dmp.service.impl;


import cn.hutool.core.util.StrUtil;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.dmp.entity.CfgDiffStrategyDetailEntity;
import com.erp.server.dmp.mapper.CfgDiffStrategyDetailMapper;
import com.erp.server.dmp.service.CfgDiffStrategyDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.dmp.service.OperateLogService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.dmp.dto.CfgDiffStrategyDetailDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 差异策略配置明细 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2025-11-11
 */
@Slf4j
@Service
public class CfgDiffStrategyDetailServiceImpl extends SuperServiceImpl<CfgDiffStrategyDetailMapper, CfgDiffStrategyDetailEntity> implements CfgDiffStrategyDetailService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgDiffStrategyDetailDTO.AddDTO addDTO) {
        CfgDiffStrategyDetailEntity cfgDiffStrategyDetailEntity = new CfgDiffStrategyDetailEntity();
        BeanMapperUtils.copy(addDTO, cfgDiffStrategyDetailEntity);

        // 数据处理
        handleData(cfgDiffStrategyDetailEntity);

        log.info("开始新增差异策略配置明细");
        boolean save = super.save(cfgDiffStrategyDetailEntity);
        if(!save) {
            throw new ServiceException("差异策略配置明细保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "差异策略配置明细" , cfgDiffStrategyDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, cfgDiffStrategyDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(cfgDiffStrategyDetailEntity.getId(), cfgDiffStrategyDetailEntity.getId());
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgDiffStrategyDetailDTO.UpdateDTO addOrUpdateDTO) {
        CfgDiffStrategyDetailEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "差异策略配置明细"));
        CfgDiffStrategyDetailEntity cfgDiffStrategyDetailEntity =  BeanMapperUtils.map(CfgDiffStrategyDetailEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(cfgDiffStrategyDetailEntity);
        log.info("编辑 开始修改差异策略配置明细数据，id：【{}】", old.getId());
        boolean save = super.updateById(cfgDiffStrategyDetailEntity);
        if(!save) {
            throw new ServiceException("差异策略配置明细保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录差异策略配置明细日志数据，id：【{}】", cfgDiffStrategyDetailEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), cfgDiffStrategyDetailEntity.getId(), "差异策略配置明细");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, cfgDiffStrategyDetailEntity, null, cfgDiffStrategyDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(CfgDiffStrategyDetailEntity cfgDiffStrategyDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
