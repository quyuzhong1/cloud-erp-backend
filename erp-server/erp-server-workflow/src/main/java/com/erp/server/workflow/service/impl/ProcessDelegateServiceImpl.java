package com.erp.server.workflow.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.workflow.dto.ProcessDelegateDTO;
import com.erp.model.workflow.entity.ProcessDelegateEntity;
import com.erp.server.workflow.mapper.ProcessDelegateMapper;
import com.erp.server.workflow.service.OperateLogService;
import com.erp.server.workflow.service.ProcessDelegateService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
/**
 * <p>
 * 委托审批 服务实现类
 * </p>
 *
 * @author will
 * @since 2025-05-12
 */
@Slf4j
@Service
public class ProcessDelegateServiceImpl extends SuperServiceImpl<ProcessDelegateMapper, ProcessDelegateEntity> implements ProcessDelegateService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(ProcessDelegateDTO.AddDTO addDTO) {
        ProcessDelegateEntity processDelegateEntity = new ProcessDelegateEntity();
        BeanMapperUtils.copy(addDTO, processDelegateEntity);

        // 数据处理
        handleData(processDelegateEntity);

        log.info("开始新增委托审批");
        // 生成单号
        // TODO 此处的null需填写生成单号类型，type查看BusinessNoTypeEnum枚举类 注意需要填写prefix 为单号前缀
        String code = docNoGenHelper.generateCode(null);
        processDelegateEntity.setCode(code);
        boolean save = super.save(processDelegateEntity);
        if(!save) {
            throw new ServiceException("委托审批保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "委托审批" , processDelegateEntity.getCode());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, processDelegateEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(processDelegateEntity.getId(), code);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(ProcessDelegateDTO.UpdateDTO addOrUpdateDTO) {
        ProcessDelegateEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "委托审批"));
        ProcessDelegateEntity processDelegateEntity =  BeanMapperUtils.map(ProcessDelegateEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(processDelegateEntity);
        log.info("编辑 开始修改委托审批数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(processDelegateEntity);
        if(!save) {
            throw new ServiceException("委托审批保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录委托审批日志数据，单号：【{}】", processDelegateEntity.getCode());
            String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), processDelegateEntity.getCode(), "委托审批");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, processDelegateEntity, null, processDelegateEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<ProcessDelegateDTO.TabListDTO> tabList(PermissionsDTO dto) {
        return Collections.emptyList();
    }

    @Override
    public PagingVO<ProcessDelegateDTO.ListDTO> paging(PagingDTO<ProcessDelegateDTO.PagingParamDTO> dto) {
        return null;
    }

    @Override
    public ProcessDelegateDTO.ViewDTO view(String id) {
        return null;
    }

    @Override
    public BatchResultDTO closeDelegate(String id) {
        return null;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(ProcessDelegateEntity processDelegateEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
