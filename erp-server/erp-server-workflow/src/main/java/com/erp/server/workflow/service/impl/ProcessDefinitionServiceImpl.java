package com.erp.server.workflow.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.DisabledEnum;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.service.impl.RedisService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.utils.ApplicationContextUtils;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.erp.model.workflow.dto.ProcessDTO;
import com.erp.model.workflow.dto.ProcessDefinitionDTO;
import com.erp.model.workflow.dto.ThirdProcessDefinitionDTO;
import com.erp.model.workflow.entity.CfgProcessRuleEntity;
import com.erp.model.workflow.entity.ProcessBusinessEntity;
import com.erp.model.workflow.entity.ProcessDefinitionEntity;
import com.erp.model.workflow.enums.CfgProcessRuleTypeEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.workflow.mapper.ProcessDefinitionMapper;
import com.erp.server.workflow.service.CfgProcessRuleService;
import com.erp.server.workflow.service.ProcessBusinessService;
import com.erp.server.workflow.service.ProcessDefinitionService;
import com.erp.server.workflow.service.ThirdProcessDefinitionService;
import org.apache.commons.collections4.CollectionUtils;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.impl.persistence.entity.DeploymentEntity;
import org.camunda.bpm.engine.repository.Deployment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
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

    @Resource
    private ThirdProcessDefinitionService  thirdProcessDefinitionService;

    @Resource
    private CfgProcessRuleService cfgProcessRuleService;


    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean addOrUpdate(ProcessDefinitionDTO.AddOrUpdateDTO dto) {
        // 查询数据是否存在
        ProcessDefinitionEntity entity = getProcessVersionEntity(dto.getId(),dto.getProcessVersion());
        if (ObjectUtil.isNotEmpty(entity) && Boolean.TRUE.equals(entity.getIsDeploy())) {
            throw new ServiceException(ApiError.PROCESS_DEFINITION_DEPLOY_UPDATE_ERROR);
        }
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
            if (!this.update(processDefinitionEntity,getUpdateWrapper(processDefinitionEntity.getId(),entity.getProcessVersion()))) {
                throw new ServiceException(ApiError.UPDATE_PROCESS_ERROR);
            }
        }
        // 保存 processBusinessEntity
        processBusinessService.addOrUpdate(dto, isSave);
        return Boolean.TRUE;
    }


    /**
     * 查询未发布数据
     * @author will
     * @date 2025/5/23 18:17
     * @param id
     * @return ProcessDefinitionEntity
     */
    @Override
    public  ProcessDefinitionEntity getProcessVersionEntity(String id,Integer processVersion) {
        return lambdaQuery().eq(ProcessDefinitionEntity::getId,id)
                .eq(ObjectUtil.isNotNull(processVersion),ProcessDefinitionEntity::getProcessVersion,processVersion)
                .last("limit 1")
                .one();
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
        ProcessDefinitionEntity definitionEntity = getProcessVersionEntity(dto.getProcessDefinitionId(),dto.getProcessVersion());
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
        //删除已发布的定义
        ApplicationContextUtils.getBean(ProcessDefinitionServiceImpl.class).deleteIsDeployById(dto.getProcessDefinitionId());
        // 保存部署时间和部署id 部署状态
        ProcessDefinitionEntity update = new ProcessDefinitionEntity(dto, deploy.getId(), deploy.getDeploymentTime(),entity.getVersion());
        ApplicationContextUtils.getBean(ProcessDefinitionServiceImpl.class).update(update,getUpdateWrapper(update.getId(),dto.getProcessVersion()
        ));
        return new ProcessDTO.DeployResultDTO(definitionEntity, entity.getVersion());
    }

    /**
     * 删除已发布的数据
     * @author will
     * @date 2025/5/26 09:05
     * @param processDefinitionId
     * @return Boolean
     */
    private Boolean deleteIsDeployById(String processDefinitionId) {
        return lambdaUpdate().eq(ProcessDefinitionEntity::getId,processDefinitionId)
                .eq(ProcessDefinitionEntity::getIsDeploy,Boolean.TRUE)
                .remove();
    }


    @Override
    public ProcessDefinitionDTO.CopyResultDTO copy(ProcessDefinitionDTO.CopyDTO dto) {
        // 查询数据是否存在
        ProcessDefinitionEntity entity = getProcessVersionEntity(dto.getId(),dto.getProcessVersion());
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
    public BatchResultDTO deleteByIds(String id,Integer processVersion,Boolean isValidate) {
        // 查询数据是否存在
        ProcessDefinitionEntity entity = getProcessVersionEntity(id,processVersion);
        if(ObjUtil.isEmpty(entity)){
            throw new ServiceException(ApiError.PROCESS_DEFINITION_NOT_EXIST);
        }
        // 已发布的流程不能删除
        if(entity.getIsDeploy() && isValidate){
            throw new ServiceException(ApiError.PROCESS_DEFINITION_DEPLOY_DELETE);
        }
        //不存在其他定义数据则删除业务信息
        List<ProcessDefinitionEntity> processDefinitionList = listByIds(Collections.singletonList(id));
        if (processDefinitionList.size() == 1) {
            // 删除关联业务数据
            List<ProcessBusinessEntity> processBusinessList = processBusinessService.getByDefinitionIds(Collections.singletonList(id));
            if(!CollectionUtils.isEmpty(processBusinessList)){
                List<String> businessIds = processBusinessList.stream().map(ProcessBusinessEntity::getId).collect(Collectors.toList());
                processBusinessService.removeByIds(businessIds);
            }
        }
        // 删除流程定义
        boolean remove = lambdaUpdate().eq(ProcessDefinitionEntity::getId,id).eq(ProcessDefinitionEntity::getProcessVersion,processVersion).remove();
        if (!remove) {
            throw new ServiceException(ApiError.ERROR_DATA_DELETE_ERROR);
        }
        return BatchResultDTO.success(entity.getId(), entity.getProcessName(), OperationTypeEnum.DELETE);
    }

    @Override
    public PagingVO<ProcessDefinitionDTO.ExportDTO> exportProcessDefinition(PagingDTO<ProcessDefinitionDTO.QueryExportDTO> dto) {
        // 查询数据
        Page<ProcessDefinitionDTO.ExportDTO> page = this.baseMapper.query(new Page<>(dto.getCurrPage(), dto.getPageSize()), dto.getParams());
        if (CollUtil.isNotEmpty(page.getRecords())) {
            handleExport(page.getRecords());
        }
        return new PagingVO<>(page);
    }

    /**
     * 导出数据处理
     * @author will
     * @date 2025/5/27 09:12
     * @param records
     * @return void
     */
    private void handleExport(List<ProcessDefinitionDTO.ExportDTO> records) {
        // 处理导出数据
        records.forEach(item -> {
            item.setDisabledName(item.getDisabled() ? "是" : "否");
        });
    }

    @Override
    public List<ProcessDefinitionDTO.DropDTO> getProcessDefinition(String businessKey) {
        //TODO 启用状态判断，暂未添加
        List<ProcessDefinitionDTO.DropDTO> reslut = baseMapper.getProcessDefinition(businessKey);
        return reslut;
    }

    @Override
    public BatchResultDTO updateDisabled(ProcessDefinitionDTO.DisableDTO disableDTO) {
        // 查询数据是否存在
        ProcessDefinitionEntity entity = getProcessVersionEntity(disableDTO.getId(),disableDTO.getProcessVersion());
        if(ObjectUtil.isEmpty(entity)){
            throw new ServiceException(ApiError.PROCESS_DEFINITION_NOT_EXIST);
        }

        entity.setDisabled(disableDTO.getDisabled());
        this.update(entity,getUpdateWrapper(entity.getId(),entity.getProcessVersion()));
        return BatchResultDTO.success(entity.getId(), entity.getProcessName(), OperationTypeEnum.DISABLED);
    }

    @Override
    public List<ProcessDefinitionDTO.TabListDTO> tabList(PermissionsDTO dto) {
        List<ProcessDefinitionDTO.TabListDTO> tabList = this.baseMapper.tabList(dto);
        Map<Boolean, Integer> map = CollUtil.isEmpty(tabList) ? new HashMap<>() : tabList.stream().collect(Collectors.toMap(ProcessDefinitionDTO.TabListDTO::getTabFlag, ProcessDefinitionDTO.TabListDTO::getCount));
        DisabledEnum[] values = DisabledEnum.values();
        List<ProcessDefinitionDTO.TabListDTO> list = new ArrayList<>();
        for (DisabledEnum item : values) {
            ProcessDefinitionDTO.TabListDTO resultDTO = new ProcessDefinitionDTO.TabListDTO();
            Integer count = map.get(item.getCode());
            resultDTO.setCount(ObjUtil.isEmpty(count) ? MathUtil.ZERO : count);
            resultDTO.setTabFlag(item.getCode());
            resultDTO.setTabFlagName(item.getName());
            list.add(resultDTO);
        }
        return list;
    }

    @Override
    public List<ProcessDefinitionDTO.DropDownDTO> dropDown(String type) {
        //1、定时拉取获取定义状态
        List<ProcessDefinitionDTO.DropDownDTO> downDTOList = this.list(new LambdaQueryWrapper<ProcessDefinitionEntity>().eq(ProcessDefinitionEntity::getIsDeploy, true).eq(ProcessDefinitionEntity::getIsDeleted, false)).stream().map(processDefinitionEntity -> {
            ProcessDefinitionDTO.DropDownDTO dropDownDTO = new ProcessDefinitionDTO.DropDownDTO();
            dropDownDTO.setCode(processDefinitionEntity.getId());
            dropDownDTO.setName(processDefinitionEntity.getProcessName());
            return dropDownDTO;
        }).collect(Collectors.toList());
        List<ThirdProcessDefinitionDTO.DropDownDTO> dropDownDTOS = thirdProcessDefinitionService.dropDown(type);
        //组合
        downDTOList.addAll(BeanUtil.copyToList(dropDownDTOS, ProcessDefinitionDTO.DropDownDTO.class));
        return downDTOList;
    }

    @Override
    public List<ProcessDefinitionDTO.DropDownDTO> proDropDown() {
        List<String> ids = cfgProcessRuleService.lambdaQuery().eq(CfgProcessRuleEntity::getType, CfgProcessRuleTypeEnum.ERPPROCESS.getCode()).eq(CfgProcessRuleEntity::getIsDeleted, false).list().stream().map(CfgProcessRuleEntity::getProcessDefinitionId).collect(Collectors.toList());
        List<ProcessDefinitionDTO.DropDownDTO> downDTOList = this.list(new LambdaQueryWrapper<ProcessDefinitionEntity>().eq(ProcessDefinitionEntity::getIsDeploy, true).eq(ProcessDefinitionEntity::getIsDeleted, false)).stream().map(processDefinitionEntity -> {
            ProcessDefinitionDTO.DropDownDTO dropDownDTO = new ProcessDefinitionDTO.DropDownDTO();
            dropDownDTO.setCode(processDefinitionEntity.getId());
            dropDownDTO.setName(processDefinitionEntity.getProcessName());
            dropDownDTO.setDisabled(ids.contains(processDefinitionEntity.getId())?true:false);
            return dropDownDTO;
        }).collect(Collectors.toList());
        return downDTOList;
    }

    @Override
    public Boolean changeProcess(ProcessDefinitionDTO.ProcessChangeDTO dto) {
        // 查询数据是否存在
        List<ProcessDefinitionEntity> list = listByIds(Collections.singletonList(dto.getId()));
        if(CollUtil.isEmpty(list)){
            throw new ServiceException(ApiError.PROCESS_DEFINITION_NOT_EXIST);
        }
        ProcessDefinitionEntity oldEntity = list.stream().filter(obj -> obj.getProcessVersion().equals(dto.getProcessVersion())).findFirst().orElse(null);
        if (!oldEntity.getIsDeploy()) {
            throw new ServiceException(ApiError.PROCESS_DEFINITION_CHANGE_ERROR);
        }
        long count = list.stream().filter(obj -> !obj.getIsDeploy()).count();
        if (count > MathUtil.ZERO) {
            throw new ServiceException(ApiError.PROCESS_DEFINITION_CHANGE_EXIST_NOT_DEPLOY);
        }
        // 实体转换
        ProcessDefinitionEntity processDefinitionEntity = new ProcessDefinitionEntity(dto);
        boolean save = save(processDefinitionEntity);
        // 保存 processDefinitionEntity
        if (!save) {
            throw new ServiceException(ApiError.SAVE_PROCESS_ERROR);
        }
        return save;
    }

    @Override
    public ProcessDefinitionEntity getIsDeployEntityById(String id) {
        return lambdaQuery().eq(ProcessDefinitionEntity::getId,id).eq(ProcessDefinitionEntity::getIsDeploy,Boolean.TRUE).last("limit 1").one();
    }


    /**
     * 获取更新唯一字段
     * @author will
     * @date 2025/5/29 11:26
     * @param id
     * @param processVersion
     * @return UpdateWrapper<ProcessDefinitionEntity>
     */
    private UpdateWrapper<ProcessDefinitionEntity> getUpdateWrapper (String id,Integer processVersion) {
        UpdateWrapper<ProcessDefinitionEntity> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", id)
                .eq("process_version", processVersion);
        return  wrapper;
    }
}
