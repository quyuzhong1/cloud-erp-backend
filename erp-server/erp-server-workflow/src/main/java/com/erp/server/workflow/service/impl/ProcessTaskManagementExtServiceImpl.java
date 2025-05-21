package com.erp.server.workflow.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.workflow.entity.ProcessTaskManagementExtEntity;
import com.erp.server.workflow.mapper.ProcessTaskManagementExtMapper;
import com.erp.server.workflow.service.ProcessTaskManagementExtService;
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
import com.erp.model.workflow.dto.ProcessTaskManagementExtDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * process_task_management拓展表 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-05-12
 */
@Slf4j
@Service
public class ProcessTaskManagementExtServiceImpl extends SuperServiceImpl<ProcessTaskManagementExtMapper, ProcessTaskManagementExtEntity> implements ProcessTaskManagementExtService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(ProcessTaskManagementExtDTO.AddDTO addDTO) {
        ProcessTaskManagementExtEntity processTaskManagementExtEntity = new ProcessTaskManagementExtEntity();
        BeanMapperUtils.copy(addDTO, processTaskManagementExtEntity);

        // 数据处理
        handleData(processTaskManagementExtEntity);

        log.info("开始新增process_task_management拓展单");
        boolean save = super.save(processTaskManagementExtEntity);
        if(!save) {
            throw new ServiceException("process_task_management拓展单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "process_task_management拓展单" , processTaskManagementExtEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, processTaskManagementExtEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(processTaskManagementExtEntity.getId(), processTaskManagementExtEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(ProcessTaskManagementExtDTO.UpdateDTO addOrUpdateDTO) {
        ProcessTaskManagementExtEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "process_task_management拓展单"));
        ProcessTaskManagementExtEntity processTaskManagementExtEntity =  BeanMapperUtils.map(ProcessTaskManagementExtEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(processTaskManagementExtEntity);
        log.info("编辑 开始修改process_task_management拓展单数据，id：【{}】", old.getId());
        boolean save = super.updateById(processTaskManagementExtEntity);
        if(!save) {
            throw new ServiceException("process_task_management拓展单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录process_task_management拓展单日志数据，id：【{}】", processTaskManagementExtEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), processTaskManagementExtEntity.getId(), "process_task_management拓展单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, processTaskManagementExtEntity, null, processTaskManagementExtEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(ProcessTaskManagementExtEntity processTaskManagementExtEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
