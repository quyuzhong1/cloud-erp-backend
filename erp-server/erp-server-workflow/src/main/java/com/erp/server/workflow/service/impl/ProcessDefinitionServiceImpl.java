package com.erp.server.workflow.service.impl;

import org.apache.commons.collections4.CollectionUtils;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.RedisService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.workflow.dto.ProcessDTO;
import com.erp.model.workflow.dto.ProcessDefinitionDTO;
import com.erp.model.workflow.entity.ProcessBusinessEntity;
import com.erp.model.workflow.entity.ProcessDefinitionEntity;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.workflow.mapper.ProcessDefinitionMapper;
import com.erp.server.workflow.service.ProcessBusinessService;
import com.erp.server.workflow.service.ProcessDefinitionService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.impl.persistence.entity.DeploymentEntity;
import org.camunda.bpm.engine.repository.Deployment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_PROCESS_DEFINITION;

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
    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean addOrUpdate(ProcessDefinitionDTO.AddOrUpdateDTO dto) {
        // 查询数据是否存在
        ProcessDefinitionEntity entity = getById(dto.getId());
        // dto转换为 processDefinitionEntity 和 processBusinessEntity 两个实体
        ProcessDefinitionEntity processDefinitionEntity = new ProcessDefinitionEntity(dto);
        // 不存在则新增
        Boolean isSave = Boolean.FALSE;
        if (null == entity) {
            // 保存 processDefinitionEntity
            if (!(save(processDefinitionEntity))) {
                throw new ServiceException(ApiError.SAVE_PROCESS_ERROR);
            }
            isSave = Boolean.TRUE;
        }else {
            processDefinitionEntity.setId(entity.getId());
            processDefinitionEntity.setIsDeploy(Boolean.FALSE);
            // 更新 processDefinitionEntity
            if (!updateById(processDefinitionEntity)) {
                throw new ServiceException(ApiError.UPDATE_PROCESS_ERROR);
            }
        }
        // 保存 processBusinessEntity
        processBusinessService.addOrUpdate(dto, isSave);
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<ProcessDefinitionDTO.ListDTO> paging(PagingDTO<ProcessDefinitionDTO.QueryDTO> dto) {
        // 分页查询
        Page<?> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<ProcessDefinitionDTO.ListDTO> pageData = this.baseMapper.paging(query, dto.getParams());
        if(CollectionUtils.isEmpty(pageData.getRecords())) {
            return new PagingVO<>(pageData);
        }
        return new PagingVO<>(pageData);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProcessDTO.DeployResultDTO deploy(ProcessDTO.DeployDTO dto) {
        // 获取流程定义信息
        ProcessDefinitionEntity definitionEntity = getById(dto.getProcessDefinitionId());
        if(null == definitionEntity){
            throw new ServiceException(ApiError.PROCESS_DEFINITION_NOT_EXIST);
        }
        // 已发布的流程不能再次发布
        if(Boolean.TRUE.equals(definitionEntity.getIsDeploy())){
            throw new ServiceException(ApiError.PROCESS_DEFINITION_ALREADY_DEPLOY);
        }
        Deployment deploy = repositoryService.createDeployment()
                .name(definitionEntity.getProcessName())
                .addString(definitionEntity.getId() + ".bpmn", definitionEntity.getBpmnXml())
                .deploy();
        DeploymentEntity deploymentEntity = (DeploymentEntity) deploy;
        org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity entity = (org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity) deploymentEntity.getDeployedArtifacts().get(org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity.class).get(0);
        // 保存部署时间和部署id 部署状态
        ProcessDefinitionEntity update = new ProcessDefinitionEntity(dto, deploy.getId(), deploy.getDeploymentTime(),entity.getVersion());
        updateById(update);
        return new ProcessDTO.DeployResultDTO(definitionEntity, entity.getVersion());
    }


    @Override
    public ProcessDefinitionDTO.CopyResultDTO copy(ProcessDefinitionDTO.CopyDTO dto) {
        // 查询数据是否存在
        ProcessDefinitionEntity entity = getById(dto.getId());
        if(null == entity){
            throw new ServiceException(ApiError.PROCESS_DEFINITION_NOT_EXIST);
        }
        // 查询关联业务数据
        ProcessBusinessEntity businessEntity = processBusinessService.getByDefinitionId(entity.getId());
        String businessKey = null;
        if(null != businessEntity){
            businessKey = businessEntity.getBusinessKey();
        }
        if(CharSequenceUtil.isNotBlank(entity.getBpmnXml())){
            // 替换流程定义id
            String bpmnXml = entity.getBpmnXml().replaceAll(entity.getId(), CharSequenceUtil.format("act_{}", System.currentTimeMillis()));
            entity.setBpmnXml(bpmnXml);
        }
        return new ProcessDefinitionDTO.CopyResultDTO(entity, businessKey);
    }

    @Override
    public Boolean exportExcel(ProcessDefinitionDTO.QueryExportDTO dto) {
        downloadTaskFeign.saveDownloadTask("流程设计记录", EXPORT_PROCESS_DEFINITION.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteByIds(List<String> ids) {
        // 查询数据是否存在
        List<ProcessDefinitionEntity> entityList = listByIds(ids);
        if(CollectionUtils.isEmpty(entityList)){
            throw new ServiceException(ApiError.PROCESS_DEFINITION_NOT_EXIST);
        }
        // 已发布的流程不能删除
        List<ProcessDefinitionEntity> deployList = entityList.stream().filter(x -> x.getIsDeploy()).collect(Collectors.toList());
        if(!CollectionUtils.isEmpty(deployList)){
            throw new ServiceException(ApiError.PROCESS_DEFINITION_DEPLOY_DELETE);
        }
        // 删除关联业务数据
        List<ProcessBusinessEntity> processBusinessList = processBusinessService.getByDefinitionIds(ids);
        if(!CollectionUtils.isEmpty(processBusinessList)){
            List<String> businessIds = processBusinessList.stream().map(ProcessBusinessEntity::getId).collect(Collectors.toList());
            processBusinessService.removeByIds(businessIds);
        }
        // 删除流程定义
        return removeByIds(ids);
    }

    @Override
    public PagingVO<ProcessDefinitionDTO.ExportDTO> exportProcessDefinition(PagingDTO<ProcessDefinitionDTO.QueryExportDTO> dto) {
        // 查询数据
        Page<ProcessDefinitionDTO.ExportDTO> page = this.baseMapper.query(new Page<>(dto.getCurrPage(), dto.getPageSize()), dto.getParams());
        return new PagingVO<>(page);
    }
}
