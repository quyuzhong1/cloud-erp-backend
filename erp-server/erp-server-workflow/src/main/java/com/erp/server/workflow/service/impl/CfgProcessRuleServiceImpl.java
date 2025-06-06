package com.erp.server.workflow.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.util.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.workflow.dto.CfgProcessExpDTO;
import com.erp.model.workflow.dto.CfgProcessFieldMapDTO;
import com.erp.model.workflow.dto.CfgProcessRuleDTO;
import com.erp.model.workflow.entity.CfgProcessRuleEntity;
import com.erp.model.workflow.entity.ProcessDefinitionEntity;
import com.erp.model.workflow.entity.ProcessManagementEntity;
import com.erp.model.workflow.entity.ThirdProcessInstanceEntity;
import com.erp.model.workflow.enums.CfgProcessRuleTypeEnum;
import com.erp.server.workflow.mapper.CfgProcessRuleMapper;
import com.erp.server.workflow.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


/**
 * <p>
 * 流程设置执行条件 服务实现类
 * </p>
 *
 * @author hcg
 * @since 2025-05-13
 */
@Slf4j
@Service
public class CfgProcessRuleServiceImpl extends SuperServiceImpl<CfgProcessRuleMapper, CfgProcessRuleEntity> implements CfgProcessRuleService {
    @Autowired
    private OperateLogService operateLogService;
    @Resource
    CfgProcessExpService cfgProcessExpService;
    @Resource
    CfgProcessFieldMapService cfgProcessFieldMapService;
    @Resource
    ProcessManagementService processManagementService;
    @Resource
    ThirdProcessInstanceService thirdProcessInstanceService;
    @Resource
    ProcessDefinitionService processDefinitionService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(String bussinessKey, String cfgProcessId, List<CfgProcessRuleDTO.AddOrUpdateDTO> addDTO) {
        //校验数据
        List<CfgProcessRuleEntity> entities = handleData(cfgProcessId, addDTO);
        // 批量保存或更新
        if (!entities.isEmpty()) {
            log.info("批量保存流程设置执行条件");
            boolean result = super.saveBatch(entities);
            if (!result) {
                throw new ServiceException("流程设置执行条件批量保存失败");
            }
        }

        // 组装审核条件和字段配置的Map
        for (CfgProcessRuleDTO.AddOrUpdateDTO dto : addDTO) {
            String ruleId = dto.getId();
            List<CfgProcessExpDTO.AddOrUpdateDTO> processExpDTOList = dto.getProcessExpDTOList() != null
                    ? dto.getProcessExpDTOList() : Collections.emptyList();
            if (!processExpDTOList.isEmpty()) {
                cfgProcessExpService.add(cfgProcessId, ruleId, processExpDTOList);
            }
            List<CfgProcessFieldMapDTO.AddOrUpdateDTO> processFieldMapDTOList = dto.getProcessFieldMapDTOList() != null
                    ? dto.getProcessFieldMapDTOList() : Collections.emptyList();
            if (!processFieldMapDTOList.isEmpty()) {
                cfgProcessFieldMapService.add(bussinessKey, cfgProcessId, ruleId, processFieldMapDTOList);
            }
        }

        // 操作日志
        String msg = StrUtil.format("新增【{}】流程设置执行条件", UserContext.getDefaultLoginUser().getUserName(), "流程设置执行条件", "");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CFG_PROCESS.getCode(), cfgProcessId, "新增操作");
        // TODO 新增明细（如果有明细的话）
        return new BaseResultDTO.AddDTO();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.UpdateDTO update(String bussinessKey, String cfgProcessId, List<CfgProcessRuleDTO.AddOrUpdateDTO> addDTO) {
        // 查询数据库中与 mainId 关联的记录
        List<CfgProcessRuleEntity> old = this.list(
                new LambdaQueryWrapper<CfgProcessRuleEntity>()
                        .eq(CfgProcessRuleEntity::getCfgProcessId, cfgProcessId)
                        .eq(CfgProcessRuleEntity::getIsDeleted, false)
        );

        // 提取 addDTO 中的 id
        Set<String> addDTOIds = addDTO.stream()
                .map(CfgProcessRuleDTO.AddOrUpdateDTO::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 找出 existingEntities 中未包含于 addDTOIds 的记录
        List<String> idsToDelete = old.stream()
                .map(CfgProcessRuleEntity::getId)
                .filter(id -> !addDTOIds.contains(id))
                .collect(Collectors.toList());
        //add
        List<CfgProcessRuleDTO.AddOrUpdateDTO> addDTOWithoutId = addDTO.stream()
                .filter(dto -> StrUtil.isEmpty(dto.getId()))
                .collect(Collectors.toList());
        add(bussinessKey, cfgProcessId, addDTOWithoutId);

        //去除新增
        addDTO.removeAll(addDTOWithoutId);

        // 删除未包含的记录
        if (!idsToDelete.isEmpty()) {
            this.removeByIds(idsToDelete);
            log.info("删除流程设置执行条件: {}", idsToDelete);
            delete(idsToDelete);
        }

        // 校验
        List<CfgProcessRuleEntity> entities = handleData(cfgProcessId, addDTO);
        // 批量保存或更新
        if (!entities.isEmpty()) {
            log.info("批量更新流程设置执行条件");
            boolean result = super.updateBatchById(entities);
            if (!result) {
                throw new ServiceException("流程设置执行条件批量更新失败");
            }
        }
        //遍历entities，组成map，key为id，value为entity
        Map<String, CfgProcessRuleEntity> entityMap = entities.stream()
                .collect(Collectors.toMap(CfgProcessRuleEntity::getId, entity -> entity));
        // 组装审核条件和字段配置的Map
        for (CfgProcessRuleDTO.AddOrUpdateDTO dto : addDTO) {
            CfgProcessRuleEntity ruleEntity = entityMap.get(dto.getId());
            if (ObjectUtil.isEmpty(ruleEntity)) {
                continue;
            }
            String ruleId = dto.getId();
            List<CfgProcessExpDTO.AddOrUpdateDTO> processExpDTOList = dto.getProcessExpDTOList() != null
                    ? dto.getProcessExpDTOList() : Collections.emptyList();
            if (!processExpDTOList.isEmpty()) {
                cfgProcessExpService.addOrUpdate(cfgProcessId, ruleId, processExpDTOList);
            }
            List<CfgProcessFieldMapDTO.AddOrUpdateDTO> processFieldMapDTOList = dto.getProcessFieldMapDTOList() != null
                    ? dto.getProcessFieldMapDTOList() : Collections.emptyList();
            if (!processFieldMapDTOList.isEmpty()) {
                cfgProcessFieldMapService.addOrUpdate(bussinessKey, cfgProcessId, ruleId, processFieldMapDTOList);
            }
        }
        // 操作日志，遍历entities，找出old中和entity id相同的
        old.forEach(entity -> {
            CfgProcessRuleEntity ruleEntity = entityMap.get(entity.getId());
            if (ObjectUtil.isNotEmpty(ruleEntity)) {
                operateLogService.addModuleOperateLogByObj(entity, ruleEntity, ModuleTypeEnum.CFG_PROCESS.getCode(), cfgProcessId, "更新操作");
            }
        });
        return new BaseResultDTO.UpdateDTO();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<String> ids) {
        //删除执行条件
        List<CfgProcessRuleEntity> processRuleEntityList = this.list(new LambdaQueryWrapper<CfgProcessRuleEntity>().in(CfgProcessRuleEntity::getId, ids).eq(CfgProcessRuleEntity::getIsDeleted, false));
        if (CollUtil.isEmpty(processRuleEntityList)){
            throw new ServiceException("请选择要删除的流程执行条件");
        }
        processRuleEntityList.forEach(item -> {
            if (item.getType().equals(CfgProcessRuleTypeEnum.ERPPROCESS.getCode())) {
                List<ProcessManagementEntity> processManagementEntities = processManagementService.list(new LambdaQueryWrapper<ProcessManagementEntity>().eq(ProcessManagementEntity::getActProcessDefinitionId, item.getProcessDefinitionId()).eq(ProcessManagementEntity::getIsDeleted, false));
                if (processManagementEntities.size() > 0) {
                    throw new ServiceException("流程已被单据使用，不可删除");
                }
            }
            List<ThirdProcessInstanceEntity> thirdProcessInstanceEntities = thirdProcessInstanceService.list(new LambdaQueryWrapper<ThirdProcessInstanceEntity>().eq(ThirdProcessInstanceEntity::getApprovalCode, item.getProcessDefinitionId()).eq(ThirdProcessInstanceEntity::getIsDeleted, false));
            if (thirdProcessInstanceEntities.size() > 0) {
                throw new ServiceException("流程已被单据使用，不可删除");
            }
        });
        removeByIds(ids);
        cfgProcessExpService.delete(ids);
        cfgProcessFieldMapService.delete(ids);
        processRuleEntityList.forEach(processRuleEntity -> {
            // 操作日志
            String msg = StrUtil.format("删除【{}】流程设置执行条件", UserContext.getDefaultLoginUser().getUserName(), "流程设置执行条件", "");
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CFG_PROCESS.getCode(), processRuleEntity.getCfgProcessId(), "删除操作");
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResultDTO.UpdateDTO updateDefault(CfgProcessRuleDTO.UpdateStateDTO dto) {
        List<CfgProcessRuleEntity> processRuleEntityList = this.list(new LambdaQueryWrapper<CfgProcessRuleEntity>().eq(CfgProcessRuleEntity::getCfgProcessId, dto.getCfgProcessId()).eq(CfgProcessRuleEntity::getIsDefault, true).eq(CfgProcessRuleEntity::getIsDeleted, false));
        if (processRuleEntityList.size() > 0) {
            for (CfgProcessRuleEntity cfgProcessRuleEntity : processRuleEntityList) {
                cfgProcessRuleEntity.setIsDefault(false);
            }
            this.updateBatchById(processRuleEntityList);
        }
        //根据dto中的id，和state更新
        boolean success = new LambdaUpdateChainWrapper<>(this.baseMapper)
                .eq(CfgProcessRuleEntity::getId, dto.getId())
                .set(CfgProcessRuleEntity::getIsDefault, dto.getState())
                .update();
        return new BaseResultDTO.UpdateDTO(dto.getId(), dto.getState().toString());
    }

    @Override
    public CfgProcessRuleEntity getByDefinitionId(String id) {
        return lambdaQuery().eq(CfgProcessRuleEntity::getProcessDefinitionId,id).last("limit 1").one();
    }

    @Override
    public List<CfgProcessRuleEntity> listByProcessId(String id,String type) {
        return this.lambdaQuery().eq(CfgProcessRuleEntity::getCfgProcessId,id)
                .eq(CfgProcessRuleEntity::getType,type)
                .eq(CfgProcessRuleEntity::getDisabled, Boolean.FALSE)
                .list();
    }

    @Override
    public String getVersion(String processDefinitionId) {
        ProcessDefinitionEntity entity = processDefinitionService.getIsDeployEntityById(processDefinitionId);
        return entity.getProcessVersion().toString();
    }

    /**
     * 新增修改处理数据
     */
    private List<CfgProcessRuleEntity> handleData(String cfgProcessId, List<CfgProcessRuleDTO.AddOrUpdateDTO> addDTO) {
        // TODO 验证数据 & 数据赋值
        // 校验 type=sysProcess 的数量是否大于1
        long sysProcessCount = addDTO.stream()
                .filter(dto -> CfgProcessRuleTypeEnum.ERPPROCESS.getCode().equals(dto.getType()))
                .count();
        if (sysProcessCount > 1) {
            //TODO 单据name
            throw new ServiceException("{}已配置流程，不可重复配置");
        }

        // 校验 type=fsProcess 的 processFieldMapDTOList 是否为空
        addDTO.stream()
                .filter(dto -> CfgProcessRuleTypeEnum.FSPROCESS.getCode().equals(dto.getType()))
                .forEach(dto -> {
                    if (CollectionUtils.isEmpty(dto.getProcessFieldMapDTOList())) {
                        throw new ServiceException("字段配置必须填写");
                    }
                });
        //统计默认条件数
        AtomicInteger count = new AtomicInteger();
        // 转换DTO为Entity
        List<CfgProcessRuleEntity> entities = addDTO.stream()
                .map(dto -> {
                    CfgProcessRuleEntity entity = new CfgProcessRuleEntity();
                    if (StrUtil.isEmpty(dto.getId())) {
                        dto.setId(IdWorker.getIdStr());
                    }
                    if (dto.getIsDefault()) {
                        count.getAndIncrement();
                    }
                    BeanMapperUtils.copy(dto, entity);
                    entity.setCfgProcessId(cfgProcessId);
                    return entity;
                }).collect(Collectors.toList());
        if (count.get() > 1) {
            throw new ServiceException("默认条件不能超过1个");
        }
        return entities;
    }
}