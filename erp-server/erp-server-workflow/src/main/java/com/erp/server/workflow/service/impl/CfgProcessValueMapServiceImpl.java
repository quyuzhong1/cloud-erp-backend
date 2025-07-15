package com.erp.server.workflow.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.excel.util.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.RedisService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.workflow.dto.CfgProcessFieldMapDTO;
import com.erp.model.workflow.dto.CfgProcessValueMapDTO;
import com.erp.model.workflow.entity.CfgProcessValueMapEntity;
import com.erp.model.workflow.enums.CfgProcessRuleTypeEnum;
import com.erp.sdk.fs.service.FsService;
import com.erp.server.workflow.context.ProcessFormFactory;
import com.erp.server.workflow.handler.ProcessFormHandler;
import com.erp.server.workflow.mapper.CfgProcessValueMapMapper;
import com.erp.server.workflow.service.CfgProcessValueMapService;
import com.erp.server.workflow.service.OperateLogService;
import com.lark.oapi.service.approval.v4.model.GetApprovalResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>
 * 流程设置值映射 服务实现类
 * </p>
 *
 * @author hcg
 * @since 2025-05-12
 */
@Slf4j
@Service
public class CfgProcessValueMapServiceImpl extends SuperServiceImpl<CfgProcessValueMapMapper, CfgProcessValueMapEntity> implements CfgProcessValueMapService {
    @Autowired
    private OperateLogService operateLogService;

    @Resource
    private FsService fsService;

    @Resource
    private RedisService redisService;

    @Resource
    private ProcessFormFactory   processFormFactory;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(String cfgProcessId, String fieldMapId, List<CfgProcessValueMapDTO.AddOrUpdateDTO> addDTO, String processDefinitionId) {
        List<CfgProcessValueMapEntity> entities = addDTO.stream()
                .map(dto -> {
                    CfgProcessValueMapEntity entity = new CfgProcessValueMapEntity();
                    BeanUtil.copyProperties(dto, entity);
                    entity.setDefaultValue(dto.getDefaultValue());
                    entity.setFieldMapId(fieldMapId);
                    return entity;
                })
                .collect(Collectors.toList());
        if (entities.isEmpty()) {
            return new BaseResultDTO.AddDTO();
        }
        boolean b = this.saveBatch(entities);
        if (!b) {
            throw new ServiceException("保存值映射失败");
        }
        ;
        // 操作日志
        String msg = StrUtil.format("流程编码【{}】新增流程设置值映射",processDefinitionId);
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CFG_PROCESS.getCode(), cfgProcessId, "新增操作");

        return new BaseResultDTO.AddDTO();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO addOrUpdate(String cfgProcessId, String processDefinitionId, CfgProcessFieldMapDTO.AddOrUpdateDTO fieldMapDTO , List<CfgProcessValueMapDTO.AddOrUpdateDTO> addDTO) {
        String fieldMapId = fieldMapDTO.getId();
        // 查询数据库中与 field_map_id 关联的记录
        List<CfgProcessValueMapEntity> existingEntities = this.list(
                new LambdaQueryWrapper<CfgProcessValueMapEntity>()
                        .eq(CfgProcessValueMapEntity::getFieldMapId, fieldMapId)
                        .eq(CfgProcessValueMapEntity::getIsDeleted, false)
        );
        // 提取 addDTO 中的 id
        List<String> addDTOIds = addDTO.stream()
                .map(CfgProcessValueMapDTO.AddOrUpdateDTO::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        // 找出 existingEntities 中未包含于 addDTOIds 的记录
        List<String> idsToDelete = existingEntities.stream()
                .map(CfgProcessValueMapEntity::getId)
                .filter(id -> !addDTOIds.contains(id))
                .collect(Collectors.toList());
        //分离add
        List<CfgProcessValueMapDTO.AddOrUpdateDTO> addDTOs = addDTO.stream()
                .filter(dto -> StrUtil.isEmpty(dto.getId()))
                .collect(Collectors.toList());
        add(cfgProcessId, fieldMapId, addDTOs,processDefinitionId);
        addDTO.removeAll(addDTOs);
        // 删除未包含的记录
        if (!idsToDelete.isEmpty()) {
            this.removeByIds(idsToDelete);
            log.info("删除流程设置值映射: {}", idsToDelete);
        }
        // 遍历 addDTO，id 为空的保存，id 不为空的更新
        List<CfgProcessValueMapEntity> entities = addDTO.stream()
                .map(dto -> {
                    CfgProcessValueMapEntity entity = new CfgProcessValueMapEntity();
                    BeanMapperUtils.copy(dto, entity);
                    entity.setFieldMapId(fieldMapId);
                    entity.setDefaultValue(dto.getDefaultValue());
                    return entity;
                })
                .collect(Collectors.toList());
        log.info("批量保存或更新流程设置值映射: {}", entities);
        Map<String, CfgProcessValueMapEntity> entityMap = entities.stream()
                .collect(Collectors.toMap(CfgProcessValueMapEntity::getId, entity -> entity));
        if (entities.isEmpty()) {
            return new BaseResultDTO.AddDTO();
        }
        //保存
        boolean b = this.updateBatchById(entities);
        if (!b) {
            throw new ServiceException("保存值映射失败");
        }
        ;
        //生成日志
        existingEntities.forEach(entity -> {
            CfgProcessValueMapEntity ruleEntity = entityMap.get(entity.getId());
            if (ObjectUtil.isNotEmpty(ruleEntity)) {
                operateLogService.addModuleOperateLogByObj(entity, ruleEntity, ModuleTypeEnum.CFG_PROCESS.getCode(), cfgProcessId, CharSequenceUtil.format("流程编码【{}】字段【{}】值映射",processDefinitionId,fieldMapDTO.getThirdField()));
            }
        });

        return new BaseResultDTO.AddDTO();
    }

    @Override
    public List<CfgProcessValueMapDTO.DropDownDTO> view(String fieldId, String approvalCode,String type) {
        try {
            GetApprovalResp approval = fsService.getApproval(approvalCode);
            String jsonStr = JSONUtil.toJsonStr(approval.getData());
            if (ObjectUtil.isNotEmpty(redisService.getCacheObject(fieldId))){
                return redisService.getCacheObject(fieldId);
            }
            ProcessFormHandler handler = processFormFactory.getAssembleFormHandler(CfgProcessRuleTypeEnum.getByCode(type).name());
            Map<String, Map<String, String>> stringMapMap = handler.parseFormValue(jsonStr);
            Map<String, String> stringMap = stringMapMap.get(fieldId);
            //遍历map，key作为DropDownDTO的value，value作为DropDownDTO的name
            List<CfgProcessValueMapDTO.DropDownDTO> dropDownDTOS = new ArrayList<>();
            for (Map.Entry<String, String> entry : stringMap.entrySet()) {
                CfgProcessValueMapDTO.DropDownDTO dropDownDTO = new CfgProcessValueMapDTO.DropDownDTO();
                dropDownDTO.setName(entry.getValue());
                dropDownDTO.setValue(entry.getKey());
                dropDownDTOS.add(dropDownDTO);
                redisService.setNx(entry.getKey(), entry.getValue(),10 * 60L,  TimeUnit.SECONDS);
            }

            return dropDownDTOS;
        } catch (Exception e) {
            throw new ServiceException("飞书选项值列表转换异常：{}", e.getMessage());
        }
    }

    @Override
    public void delete(List<String> mainIds) {
        // 符合 ruleId 存在于 ids 的更新
        List<CfgProcessValueMapEntity> cfgProcessValueMapEntities = this.list(new LambdaQueryWrapper<CfgProcessValueMapEntity>().in(CfgProcessValueMapEntity::getFieldMapId, mainIds));
        if (CollectionUtils.isEmpty(cfgProcessValueMapEntities)) {
            return;
        }
        List<String> ids = cfgProcessValueMapEntities.stream().map(CfgProcessValueMapEntity::getId).collect(Collectors.toList());
        removeByIds(ids);
    }

}
