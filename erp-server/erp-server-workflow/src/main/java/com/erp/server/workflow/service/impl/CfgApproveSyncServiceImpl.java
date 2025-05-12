package com.erp.server.workflow.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.workflow.entity.CfgApproveSyncEntity;
import com.erp.server.workflow.mapper.CfgApproveSyncMapper;
import com.erp.server.workflow.service.CfgApproveSyncService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.workflow.service.OperateLogService;
import com.erp.server.workflow.service.CommonService;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.workflow.dto.CfgApproveSyncDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * ERP审批同步配置 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-05-12
 */
@Slf4j
@Service
public class CfgApproveSyncServiceImpl extends SuperServiceImpl<CfgApproveSyncMapper, CfgApproveSyncEntity> implements CfgApproveSyncService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgApproveSyncDTO.AddDTO addDTO) {
        CfgApproveSyncEntity cfgApproveSyncEntity = new CfgApproveSyncEntity();
        BeanMapperUtils.copy(addDTO, cfgApproveSyncEntity);

        // 数据处理
        handleData(cfgApproveSyncEntity);

        log.info("开始新增ERP审批同步配置");
        // 生成单号
        // TODO 此处的null需填写生成单号类型，type查看BusinessNoTypeEnum枚举类 注意需要填写prefix 为单号前缀
        String code = docNoGenHelper.generateCode(null);
        cfgApproveSyncEntity.setCode(code);
        boolean save = super.save(cfgApproveSyncEntity);
        if(!save) {
            throw new ServiceException("ERP审批同步配置保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "ERP审批同步配置" , cfgApproveSyncEntity.getCode());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, cfgApproveSyncEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(cfgApproveSyncEntity.getId(), code);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgApproveSyncDTO.UpdateDTO addOrUpdateDTO) {
        CfgApproveSyncEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "ERP审批同步配置"));
        CfgApproveSyncEntity cfgApproveSyncEntity =  BeanMapperUtils.map(CfgApproveSyncEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(cfgApproveSyncEntity);
        log.info("编辑 开始修改ERP审批同步配置数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(cfgApproveSyncEntity);
        if(!save) {
            throw new ServiceException("ERP审批同步配置保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录ERP审批同步配置日志数据，单号：【{}】", cfgApproveSyncEntity.getCode());
            String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), cfgApproveSyncEntity.getCode(), "ERP审批同步配置");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, cfgApproveSyncEntity, null, cfgApproveSyncEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(CfgApproveSyncEntity cfgApproveSyncEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
