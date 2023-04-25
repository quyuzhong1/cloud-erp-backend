package com.erp.server.workflow.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.interceptor.CommonInterceptor;
import com.common.business.service.RedisService;
import com.common.business.utils.ExportUtil;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.ExcelUtil;
import com.erp.model.scm.dto.PurchaseChangeDTO;
import com.erp.model.workflow.dto.ProcessDTO;
import com.erp.model.workflow.dto.ProcessDefinitionDTO;
import com.erp.model.workflow.entity.ProcessBusinessEntity;
import com.erp.model.workflow.entity.ProcessDefinitionEntity;
import com.erp.server.workflow.mapper.ProcessDefinitionMapper;
import com.erp.server.workflow.service.ProcessBusinessService;
import com.erp.server.workflow.service.ProcessDefinitionService;
import com.common.business.service.SuperServiceImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.impl.persistence.entity.DeploymentEntity;
import org.camunda.bpm.engine.repository.Deployment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Cloud
 * @since 2023-04-21
 */
@Service
public class ProcessDefinitionServiceImpl extends SuperServiceImpl<ProcessDefinitionMapper, ProcessDefinitionEntity> implements ProcessDefinitionService {

    @Resource
    private ProcessBusinessService processBusinessService;
    @Resource
    private RepositoryService repositoryService;
    @Resource
    private RedisService redisService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean saveOrUpdate(ProcessDefinitionDTO.AddOrUpdateDTO dto) {
        // 查询数据是否存在
        ProcessDefinitionEntity entity = getById(dto.getId());
        // dto转换为 processDefinitionEntity 和 processBusinessEntity 两个实体
        ProcessDefinitionEntity processDefinitionEntity = new ProcessDefinitionEntity(dto);
        ProcessBusinessEntity processBusinessEntity = new ProcessBusinessEntity(dto);
        // 不存在则新增
        if (null == entity) {
            // 保存 processDefinitionEntity
            if (!(save(processDefinitionEntity) && processBusinessService.save(processBusinessEntity))) {
                throw new ServiceException(ApiError.SAVE_PROCESS_ERROR);
            }
        }else {
            // 存在则更新
            if(ApproveStatusEnum.APPROVE.equals(entity.getApproveStatus()) || ApproveStatusEnum.APPROVE_ING.equals(entity.getApproveStatus())){
                throw new ServiceException(ApiError.PROCESS_DEFINITION_NOT_UPDATE);
            }
            processDefinitionEntity.setId(entity.getId());
            // 更新 processDefinitionEntity
            if (!updateById(processDefinitionEntity)) {
                throw new ServiceException(ApiError.UPDATE_PROCESS_ERROR);
            }
        }
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<ProcessDefinitionDTO.ListDTO> paging(PagingDTO<ProcessDefinitionDTO.QueryDTO> dto) {
        // 分页查询
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<ProcessDefinitionDTO.ListDTO> pageData = this.baseMapper.paging(query, dto.getParams());
        if(CollectionUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO<>(pageData);
        }
        pageData.getRecords().stream().peek(x -> x.setApproveStatusName(x.getApproveStatusCode().getName())).collect(Collectors.toList());
        return new PagingVO<>(pageData);
    }

    @Override
    public boolean unApprove(ProcessDefinitionDTO.UnApproveDTO dto) {
        // 查询数据是否存在
        ProcessDefinitionEntity entity = getById(dto.getId());
        if(null == entity){
            throw new ServiceException(ApiError.PROCESS_DEFINITION_NOT_EXIST);
        }
        // 判断是否已审核
        if(!ApproveStatusEnum.APPROVE.equals(entity.getApproveStatus())){
            throw new ServiceException(ApiError.PROCESS_DEFINITION_NOT_APPROVE);
        }
        // 更新审核状态
        ProcessDefinitionEntity updateEntity = new ProcessDefinitionEntity(entity.getId(), ApproveStatusEnum.WAIT_SUBMIT);
        if(!updateById(updateEntity)){
            throw new ServiceException(ApiError.UPDATE_PROCESS_ERROR);
        }
        return Boolean.TRUE;
    }

    @Override
    public ProcessDTO.DeployResultDTO deploy(ProcessDTO.DeployDTO dto) {
        // 获取流程定义信息
        com.erp.model.workflow.entity.ProcessDefinitionEntity definitionEntity = getById(dto.getProcessDefinitionId());
        if(null == definitionEntity){
            throw new ServiceException(ApiError.PROCESS_DEFINITION_NOT_EXIST);
        }
        // 判断流程状态为审核中
        if(!ApproveStatusEnum.APPROVE_ING.equals(definitionEntity.getApproveStatus())){
            throw new ServiceException(ApiError.PROCESS_DEFINITION_NOT_APPROVE);
        }
        Deployment deploy = repositoryService.createDeployment()
                .name(definitionEntity.getProcessName())
                .addString(definitionEntity.getId() + ".bpmn", definitionEntity.getBpmnXml())
                .deploy();
        DeploymentEntity deploymentEntity = (DeploymentEntity) deploy;
        org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity entity = (org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity) deploymentEntity.getDeployedArtifacts().get(org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity.class).get(0);
        // 保存部署时间和部署id 部署状态
        LoginUser loginUser = CommonInterceptor.threadLocal.get();
        String uid = null == loginUser ? "" : loginUser.getUid();
        String userName = null == loginUser ? "" : loginUser.getUserName();
        com.erp.model.workflow.entity.ProcessDefinitionEntity update = new com.erp.model.workflow.entity.ProcessDefinitionEntity(dto,
                deploy.getId(), deploy.getDeploymentTime(), uid,  userName,entity.getVersion());
        updateById(update);
        return new ProcessDTO.DeployResultDTO(definitionEntity, entity.getVersion());
    }

    @Override
    public Boolean submitApprove(ProcessDefinitionDTO.SubmitDTO dto) {
        // 查询数据是否存在
        ProcessDefinitionEntity entity = getById(dto.getId());
        if(null == entity){
            throw new ServiceException(ApiError.PROCESS_DEFINITION_NOT_EXIST);
        }
        // 判断是否为待提交状态
        if(!(ApproveStatusEnum.WAIT_SUBMIT.equals(entity.getApproveStatus()) || ApproveStatusEnum.REJECT.equals(entity.getApproveStatus()))){
            throw new ServiceException(ApiError.PROCESS_DEFINITION_NOT_WAIT_SUBMIT);
        }
        // 更新审核状态
        ProcessDefinitionEntity updateEntity = new ProcessDefinitionEntity(entity.getId(), ApproveStatusEnum.APPROVE_ING);
        if(!updateById(updateEntity)){
            throw new ServiceException(ApiError.UPDATE_PROCESS_ERROR);
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean cancelApprove(ProcessDefinitionDTO.CancelDTO dto) {
        // 查询数据是否存在
        ProcessDefinitionEntity entity = getById(dto.getId());
        if(null == entity){
            throw new ServiceException(ApiError.PROCESS_DEFINITION_NOT_EXIST);
        }
        // 判断是否为审核中状态
        if(!ApproveStatusEnum.APPROVE_ING.equals(entity.getApproveStatus())){
            throw new ServiceException(ApiError.PROCESS_DEFINITION_NOT_APPROVE_ING);
        }
        // 更新审核状态
        ProcessDefinitionEntity updateEntity = new ProcessDefinitionEntity(entity.getId(), ApproveStatusEnum.WAIT_SUBMIT);
        if(!updateById(updateEntity)){
            throw new ServiceException(ApiError.CANCEL_PROCESS_ERROR);
        }
        return Boolean.TRUE;
    }

    @Override
    public ProcessDefinitionDTO.CopyResultDTO copy(ProcessDefinitionDTO.CopyDTO dto) {
        // 查询数据是否存在
        ProcessDefinitionEntity entity = getById(dto.getId());
        if(null == entity){
            throw new ServiceException(ApiError.PROCESS_DEFINITION_NOT_EXIST);
        }
        // 查询关联业务数据
        List<ProcessBusinessEntity> businessEntityList = processBusinessService.getByDefinitionId(entity.getId());
        String businessKey = null;
        if(CollectionUtil.isNotEmpty(businessEntityList)){
            businessKey = businessEntityList.get(0).getBusinessKey();
        }
        if(StrUtil.isNotBlank(entity.getBpmnXml())){
            // 替换流程定义id
            String bpmnXml = entity.getBpmnXml().replaceAll(entity.getId(), StrUtil.format("act_{}", System.currentTimeMillis()));
            entity.setBpmnXml(bpmnXml);
        }
        ProcessDefinitionDTO.CopyResultDTO resultDTO = new ProcessDefinitionDTO.CopyResultDTO(entity, businessKey);
        return  resultDTO;
    }

    @Override
    public Boolean exportExcel(ProcessDefinitionDTO.QueryExportDTO dto, HttpServletResponse response) {
        // 查询数据
        List<ProcessDefinitionDTO.ExportDTO> list = this.baseMapper.query(dto);
        list.stream().peek(x -> x.setApproveStatusName(x.getApproveStatusCode().getName())).collect(Collectors.toList());
        // 导出数据
        if (CollectionUtils.isEmpty(list)) {
            return Boolean.TRUE;
        }
        String fileName = ExportUtil.getFileName(redisService, "流程设计记录");
        ExcelUtil.export(fileName, "流程设计记录", list, ProcessDefinitionDTO.ExportDTO.class, response);
        return Boolean.TRUE;
    }
}
