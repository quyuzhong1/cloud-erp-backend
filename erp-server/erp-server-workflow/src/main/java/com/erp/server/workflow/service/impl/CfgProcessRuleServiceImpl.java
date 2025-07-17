package com.erp.server.workflow.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.DisabledEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.workflow.dto.CfgProcessExpDTO;
import com.erp.model.workflow.dto.CfgProcessFieldMapDTO;
import com.erp.model.workflow.dto.CfgProcessRuleDTO;
import com.erp.model.workflow.entity.*;
import com.erp.model.workflow.enums.CfgProcessRuleTypeEnum;
import com.erp.server.workflow.mapper.CfgProcessRuleMapper;
import com.erp.server.workflow.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
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
    @Resource
    private ThirdProcessManagementService thirdProcessManagementService;
    @Resource
    private ThirdProcessDefinitionService thirdProcessDefinitionService;
    @Autowired
    private CfgProcessService cfgProcessService;

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

                cfgProcessFieldMapService.add(bussinessKey, cfgProcessId, ruleId, processFieldMapDTOList,dto.getProcessDefinitionId(),dto.getType());
            }
        }
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
                cfgProcessFieldMapService.addOrUpdate(bussinessKey, cfgProcessId, ruleId, processFieldMapDTOList,dto.getProcessDefinitionId(),dto.getType());
            }
        }
        // 操作日志，遍历entities，找出old中和entity id相同的
        old.forEach(entity -> {
            CfgProcessRuleEntity ruleEntity = entityMap.get(entity.getId());
            if (ObjectUtil.isNotEmpty(ruleEntity)) {
                operateLogService.addModuleOperateLogByObj(entity, ruleEntity, ModuleTypeEnum.CFG_PROCESS.getCode(), cfgProcessId, CharSequenceUtil.format("流程编码【{}】",ruleEntity.getProcessDefinitionId()));
            }
        });
        return new BaseResultDTO.UpdateDTO();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(List<String> ids) {
        //删除执行条件
        List<CfgProcessRuleEntity> processRuleEntityList = this.list(new LambdaQueryWrapper<CfgProcessRuleEntity>().in(CfgProcessRuleEntity::getId, ids).eq(CfgProcessRuleEntity::getIsDeleted, false));
        if (CollUtil.isEmpty(processRuleEntityList)) {
            throw new ServiceException("请选择要删除的流程执行条件");
        }
        //校验是否存在关联单据是否在走流程
        checkBillStatus(processRuleEntityList);

        //校验是否存在运行中的流程,map,key是getType，value是List<id>
        Map<String, List<CfgProcessRuleEntity>> map = processRuleEntityList.stream().collect(Collectors.groupingBy(CfgProcessRuleEntity::getType));
        StringBuilder errmsg = new StringBuilder();
        map.forEach((key, value) -> {

            List<String> list = value.stream()
                    .map(CfgProcessRuleEntity::getProcessDefinitionId)
                    .collect(Collectors.toList());

            if (CfgProcessRuleTypeEnum.getByCode(key).equals(CfgProcessRuleTypeEnum.ERPPROCESS)) {
                List<ProcessDefinitionEntity> definitionEntityList = processDefinitionService.listByIds(list);
                Map<String, String> dIdToNameMap = definitionEntityList.stream()
                        .collect(Collectors.toMap(obj -> CharSequenceUtil.format("{}-{}",obj.getId(),obj.getProcessVersion()), ProcessDefinitionEntity::getProcessName));

                List<ProcessManagementEntity> processManagementEntities = processManagementService.list(
                        new LambdaQueryWrapper<ProcessManagementEntity>()
                                .in(ProcessManagementEntity::getProcessDefinitionId, list)
                                .eq(ProcessManagementEntity::getIsDeleted, Boolean.FALSE));

                // 分组后计算分组的数量
                Map<String, Long> groupCountMap = processManagementEntities.stream()
                        .collect(Collectors.groupingBy(obj -> CharSequenceUtil.format("{}-{}",obj.getProcessDefinitionId(),obj.getProcessVersion()), Collectors.counting()));

                for (CfgProcessRuleEntity item : value) {
                    Long a = groupCountMap.get( CharSequenceUtil.format("{}-{}",item.getProcessDefinitionId(),item.getProcessDefinitionVersion()));
                    if (a != null && a.compareTo(0L) > 0) {
                        errmsg.append(dIdToNameMap.get(CharSequenceUtil.format("{}-{}",item.getProcessDefinitionId(),item.getProcessDefinitionVersion())));
                    }
                }
            } else {
                List<ThirdProcessDefinitionEntity> processDefinitionEntityList = thirdProcessDefinitionService.list(new LambdaQueryWrapper<ThirdProcessDefinitionEntity>().in(ThirdProcessDefinitionEntity::getApprovalCode,  list));
                Map<String, String> collect = processDefinitionEntityList.stream().collect(Collectors.toMap(ThirdProcessDefinitionEntity::getApprovalCode, ThirdProcessDefinitionEntity::getName));

                List<ThirdProcessManagementEntity> processManagementEntityList = thirdProcessManagementService.list(new LambdaQueryWrapper<ThirdProcessManagementEntity>()
                        .in(ThirdProcessManagementEntity::getProcessDefinitionId, list).eq(ThirdProcessManagementEntity::getIsDeleted, Boolean.FALSE));

                Map<String, Long> thirdIdtoCountMap = processManagementEntityList.stream().collect(Collectors.groupingBy(ThirdProcessManagementEntity::getProcessDefinitionId, Collectors.counting()));
                for (CfgProcessRuleEntity item : value) {
                    Long a = thirdIdtoCountMap.get(item.getProcessDefinitionId());
                    if (a != null && a.compareTo(0L) > 0) {
                        errmsg.append(collect.get(item.getProcessDefinitionId()));
                    }
                }
            }
        });

        if (StrUtil.isNotBlank(errmsg)) {
            throw new ServiceException(errmsg + "流程已被单据使用，不可删除");
        }

        if (CollUtil.isNotEmpty(ids)) {
            try {
                super.removeByIds(ids);
                cfgProcessExpService.delete(ids);
                cfgProcessFieldMapService.delete(ids);
            } catch (Exception e) {
                log.info("删除流程设置执行条件相关数据失败, ids={}", ids, e);
                throw new ServiceException("删除流程设置执行条件相关数据失败：" + e.getMessage());
            }
        } else {
            log.info("删除流程设置执行条件相关数据时，传入的ids为空");
        }
        processRuleEntityList.forEach(processRuleEntity -> {
            // 操作日志
            String msg = StrUtil.format("流程编码【{}】删除流程设置规则", processRuleEntity.getProcessDefinitionId());
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CFG_PROCESS.getCode(), processRuleEntity.getCfgProcessId(), msg);
        });
        return Boolean.TRUE;
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
        return lambdaQuery().eq(CfgProcessRuleEntity::getProcessDefinitionId, id).last("limit 1").one();
    }

    @Override
    public List<CfgProcessRuleEntity> listByProcessId(String id, String type) {
        return this.lambdaQuery().eq(CfgProcessRuleEntity::getCfgProcessId, id)
                .eq(CharSequenceUtil.isNotBlank(type),CfgProcessRuleEntity::getType, type)
                .eq(CfgProcessRuleEntity::getDisabled, Boolean.FALSE)
                .list();
    }

    @Override
    public List<CfgProcessRuleEntity> listAllByProcessId(String id) {
        return this.lambdaQuery().eq(CfgProcessRuleEntity::getCfgProcessId, id)
                .list();
    }

    @Override
    public String getVersion(String processDefinitionId) {
        ProcessDefinitionEntity entity = processDefinitionService.getIsDeployEntityById(processDefinitionId);
        return entity.getProcessVersion().toString();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO updateState(CfgProcessRuleEntity entity, Boolean disabled) {
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL,"流程规则");
        }
        CfgProcessEntity cfgProcessEntity = cfgProcessService.getById(entity.getCfgProcessId());
        if (ObjectUtil.isEmpty(cfgProcessEntity)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL,"流程配置");
        }
        //判断审核条件状态是否发生变化
        if (disabled.equals(entity.getDisabled())) {
           return  BatchResultDTO.fail(entity.getId(), cfgProcessEntity.getCode() + SourceTypeEnum.getName(cfgProcessEntity.getBussinessKey()), "状态未发生变化");
        }
        // 更新状态
        entity.setDisabled(disabled);
        entity.setUpdateTime(LocalDateTime.now());
        super.updateById(entity);

        //日志
        String msg = StrUtil.format("流程编码【{}】设置执行条件状态由【{}】更新为【{}】",entity.getProcessDefinitionId(), DisabledEnum.getName(entity.getDisabled()) , DisabledEnum.getName(disabled));
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CFG_PROCESS.getCode(), entity.getCfgProcessId(), msg);
        return BatchResultDTO.success(entity.getId(), cfgProcessEntity.getCode() + SourceTypeEnum.getName(cfgProcessEntity.getBussinessKey()), "状态更新成功");
    }

    /**
     * 新增修改处理数据
     */
    private List<CfgProcessRuleEntity> handleData(String cfgProcessId, List<CfgProcessRuleDTO.AddOrUpdateDTO> addDTO) {
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


    /**
     * 验证是否存在单据进行中
     * @author will
     * @date 2025/6/30 14:40
     * @param list
     * @return void
     */
    private void checkBillStatus (List<CfgProcessRuleEntity> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        //erp流程定义id
        List<String> processDefinitionIdList = list.stream().filter(obj -> CharSequenceUtil.equals(obj.getType(),CfgProcessRuleTypeEnum.ERPPROCESS.getCode())).map(CfgProcessRuleEntity::getProcessDefinitionId).distinct().collect(Collectors.toList());
        //erp流程定义版本
        List<Integer> processDefinitionVersionList = list.stream().filter(obj -> CharSequenceUtil.equals(obj.getType(),CfgProcessRuleTypeEnum.ERPPROCESS.getCode())).map(CfgProcessRuleEntity::getProcessDefinitionVersion).distinct().collect(Collectors.toList());
        //fs流程定义id
        List<String> fsProcessDefinitionIdList = list.stream().filter(obj -> CharSequenceUtil.equals(obj.getType(),CfgProcessRuleTypeEnum.FSPROCESS.getCode())).map(CfgProcessRuleEntity::getProcessDefinitionId).distinct().collect(Collectors.toList());

        //查询进行中ERP流程
        List<ProcessManagementEntity> managementList = processManagementService.listDoing(processDefinitionIdList, processDefinitionVersionList);
        Map<String, List<ProcessManagementEntity>> managementMap = CollUtil.isEmpty(managementList) ? new HashMap<>() : managementList.stream().collect(Collectors.groupingBy(obj -> CharSequenceUtil.format("{}-{}", obj.getProcessDefinitionId(), obj.getProcessVersion())));

        //查询进行中第三方流程
        List<ThirdProcessManagementEntity> thirdManagementList = thirdProcessManagementService.listDoing(fsProcessDefinitionIdList);
        Map<String, List<ThirdProcessManagementEntity>> thirdManagementMap = CollUtil.isEmpty(thirdManagementList) ? new HashMap<>() : thirdManagementList.stream().collect(Collectors.groupingBy(ThirdProcessManagementEntity::getProcessDefinitionId));

        for (CfgProcessRuleEntity entity : list) {

            if (CharSequenceUtil.equals(entity.getType(),CfgProcessRuleTypeEnum.ERPPROCESS.getCode())) {
                //ERP流程
                List<ProcessManagementEntity> erpList = managementMap.get(CharSequenceUtil.format("{}-{}", entity.getProcessDefinitionId(), entity.getProcessDefinitionVersion()));
                if (CollUtil.isEmpty(erpList)){
                    continue;
                }
            } else if (CharSequenceUtil.equals(entity.getType(), CfgProcessRuleTypeEnum.FSPROCESS.getCode())) {
                //第三方流程
                List<ThirdProcessManagementEntity> thirdList = thirdManagementMap.get(entity.getProcessDefinitionId());
                if (CollUtil.isEmpty(thirdList)){
                    continue;
                }
            } else {
                throw new ServiceException(ApiError.CFG_PROCESS_RULE_TYPE_NOT_EXIST);
            }
            //未跳过则根据类型报错
            throw new ServiceException(ApiError.CFG_PROCESS_RULE_DELETE,CfgProcessRuleTypeEnum.getName(entity.getType()));
        }
    }
}