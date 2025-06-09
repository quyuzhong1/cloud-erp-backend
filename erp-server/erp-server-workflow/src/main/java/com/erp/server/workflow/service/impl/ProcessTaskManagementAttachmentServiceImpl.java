package com.erp.server.workflow.service.impl;


import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.core.config.JacksonConfig;
import com.erp.model.workflow.entity.ProcessTaskManagementAttachmentEntity;
import com.erp.server.workflow.mapper.ProcessTaskManagementAttachmentMapper;
import com.erp.server.workflow.service.ProcessTaskManagementAttachmentService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.workflow.dto.ProcessTaskManagementAttachmentDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 审核附件表 服务实现类
 * </p>
 *
 * @author tmj
 * @since 2024-09-05
 */
@Slf4j
@Service
public class ProcessTaskManagementAttachmentServiceImpl extends SuperServiceImpl<ProcessTaskManagementAttachmentMapper, ProcessTaskManagementAttachmentEntity> implements ProcessTaskManagementAttachmentService {

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(ProcessTaskManagementAttachmentDTO.AddDTO addDTO) {
        ProcessTaskManagementAttachmentEntity processTaskManagementAttachmentEntity = new ProcessTaskManagementAttachmentEntity();
        BeanMapperUtils.copy(addDTO, processTaskManagementAttachmentEntity);

        // 数据处理
        handleData(processTaskManagementAttachmentEntity);

        log.info("开始新增审核附件表（保存用户审核时提交的附件）");
        boolean save = super.save(processTaskManagementAttachmentEntity);
        if(!save) {
            throw new ServiceException("审核附件表（保存用户审核时提交的附件）保存失败");
        }

        // 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        return new BaseResultDTO.AddDTO(processTaskManagementAttachmentEntity.getId(), processTaskManagementAttachmentEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(ProcessTaskManagementAttachmentDTO.UpdateDTO updateDTO) {
        ProcessTaskManagementAttachmentEntity old = super.getById(updateDTO.getId());
        old= Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "审核附件表（保存用户审核时提交的附件）"));
        ProcessTaskManagementAttachmentEntity processTaskManagementAttachmentEntity =  BeanMapperUtils.map(ProcessTaskManagementAttachmentEntity.class, updateDTO);

        // 数据处理
        handleData(processTaskManagementAttachmentEntity);
        log.info("编辑 开始修改审核附件表（保存用户审核时提交的附件）数据，id：【{}】", old.getId());
        boolean save = super.updateById(processTaskManagementAttachmentEntity);
        if(!save) {
            throw new ServiceException("审核附件表（保存用户审核时提交的附件）保存失败");
        }
        //  改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录审核附件表（保存用户审核时提交的附件）日志数据，id：【{}】", processTaskManagementAttachmentEntity.getId());
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(ProcessTaskManagementAttachmentEntity processTaskManagementAttachmentEntity) {
    //  验证数据 & 数据赋值
        log.debug("handleData processTaskManagementAttachmentEntity：{}", JSONUtil.toJsonStr(processTaskManagementAttachmentEntity));
    }
}
