package com.erp.server.dmp.service.impl;


import cn.hutool.core.util.StrUtil;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.dmp.entity.CfgDiffStrategyEntity;
import com.erp.server.dmp.mapper.CfgDiffStrategyMapper;
import com.erp.server.dmp.service.CfgDiffStrategyService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.dmp.service.OperateLogService;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.dmp.dto.CfgDiffStrategyDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 差异策略配置基础信息 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2025-11-11
 */
@Slf4j
@Service
public class CfgDiffStrategyServiceImpl extends SuperServiceImpl<CfgDiffStrategyMapper, CfgDiffStrategyEntity> implements CfgDiffStrategyService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgDiffStrategyDTO.AddDTO addDTO) {
        CfgDiffStrategyEntity cfgDiffStrategyEntity = new CfgDiffStrategyEntity();
        BeanMapperUtils.copy(addDTO, cfgDiffStrategyEntity);

        // 数据处理
        handleData(cfgDiffStrategyEntity);

        log.info("开始新增差异策略配置基础信息");
        // 生成单号
        // TODO 此处的null需填写生成单号类型，type查看BusinessNoTypeEnum枚举类 注意需要填写prefix 为单号前缀
        String code = docNoGenHelper.generateCode(null);
        cfgDiffStrategyEntity.setCode(code);
        boolean save = super.save(cfgDiffStrategyEntity);
        if(!save) {
            throw new ServiceException("差异策略配置基础信息保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "差异策略配置基础信息" , cfgDiffStrategyEntity.getCode());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, cfgDiffStrategyEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(cfgDiffStrategyEntity.getId(), code);
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgDiffStrategyDTO.UpdateDTO addOrUpdateDTO) {
        CfgDiffStrategyEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "差异策略配置基础信息"));
        CfgDiffStrategyEntity cfgDiffStrategyEntity =  BeanMapperUtils.map(CfgDiffStrategyEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(cfgDiffStrategyEntity);
        log.info("编辑 开始修改差异策略配置基础信息数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(cfgDiffStrategyEntity);
        if(!save) {
            throw new ServiceException("差异策略配置基础信息保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录差异策略配置基础信息日志数据，单号：【{}】", cfgDiffStrategyEntity.getCode());
            String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), cfgDiffStrategyEntity.getCode(), "差异策略配置基础信息");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, cfgDiffStrategyEntity, null, cfgDiffStrategyEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(CfgDiffStrategyEntity cfgDiffStrategyEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
