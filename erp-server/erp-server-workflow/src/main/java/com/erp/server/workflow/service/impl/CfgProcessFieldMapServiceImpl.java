package com.erp.server.workflow.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.excel.util.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.workflow.dto.CfgProcessFieldMapDTO;
import com.erp.model.workflow.dto.CfgProcessValueMapDTO;
import com.erp.model.workflow.entity.CfgProcessFieldMapEntity;
import com.erp.model.workflow.enums.*;
import com.erp.sdk.fs.service.FsService;
import com.erp.server.workflow.context.ProcessFormFactory;
import com.erp.server.workflow.handler.ProcessFormHandler;
import com.erp.server.workflow.mapper.CfgProcessFieldMapMapper;
import com.erp.server.workflow.service.CfgProcessFieldMapService;
import com.erp.server.workflow.service.CfgProcessValueMapService;
import com.erp.server.workflow.service.CfgQueryOptionService;
import com.erp.server.workflow.service.OperateLogService;
import com.lark.oapi.service.approval.v4.model.GetApprovalResp;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 流程设置字段配置 服务实现类
 * </p>
 *
 * @author hcg
 * @since 2025-05-12
 */
@Slf4j
@Service
public class CfgProcessFieldMapServiceImpl extends SuperServiceImpl<CfgProcessFieldMapMapper, CfgProcessFieldMapEntity> implements CfgProcessFieldMapService {
    @Autowired
    private OperateLogService operateLogService;

    @Resource
    private CfgProcessValueMapService cfgProcessValueMapService;

    @Resource
    private CfgQueryOptionService cfgQueryOptionService;

    @Resource
    private FsService fsService;

    @Resource
    private ProcessFormFactory processFormFactory;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(String bussinessKey, String cfgProcessId, String ruleId, List<CfgProcessFieldMapDTO.AddOrUpdateDTO> addDTO, String processDefinitionId, String type) {
        //校验飞书必填字段
        validateRequiredFsFields(processDefinitionId, type, addDTO);

        try {
            List<CfgProcessFieldMapEntity> entitiesToAddOrUpdate = handleData(ruleId, addDTO);
            this.saveOrUpdateBatch(entitiesToAddOrUpdate);
        } catch (Exception e) {
            throw new ServiceException("字段配置新增失败:{}", e.getMessage());
        }
        //插入值映射
        for (CfgProcessFieldMapDTO.AddOrUpdateDTO dto : addDTO) {
            if (!CharSequenceUtil.equals(dto.getThirdFieldType(), CfgQueryOptionFieldTypeEnum.CHECKBOXV2.getCode())
                    && !CharSequenceUtil.equals(dto.getThirdFieldType(), CfgQueryOptionFieldTypeEnum.RADIOV2.getCode())) {
                // 如果不是单选或多选，则跳过值映射的添加,并且清空已存在的映射
                cfgProcessValueMapService.delete(Collections.singletonList(dto.getId()));
                continue;
            }
            String id = dto.getId();
            List<CfgProcessValueMapDTO.AddOrUpdateDTO> processValueMapDTOList = dto.getProcessValueMapDTOList();
            if (ObjectUtil.isNotEmpty(processValueMapDTOList)) {
                cfgProcessValueMapService.add(cfgProcessId, id, processValueMapDTOList,processDefinitionId);
            }
        }
        // 操作日志
        String msg = StrUtil.format("流程编码【{}】新增流程设置字段映射",processDefinitionId);
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CFG_PROCESS.getCode(), cfgProcessId, "新增操作");
        return new BaseResultDTO.AddDTO();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO addOrUpdate(String bussinessKey, String cfgProcessId, String ruleId, List<CfgProcessFieldMapDTO.AddOrUpdateDTO> addDTO, String processDefinitionId, String type) {
        //校验飞书必填字段
        validateRequiredFsFields(processDefinitionId, type, addDTO);
        // 查询数据库中与 ruleId 关联的记录
        List<CfgProcessFieldMapEntity> oldList = listByCfgId(ruleId);
        try {
            //校验更新数据
            List<CfgProcessFieldMapEntity> entitiesToAddOrUpdate = handleData(ruleId, addDTO);
            //需要删除的ID列表
            List<String> deleteIds = getDeleteIds(entitiesToAddOrUpdate, oldList);
            if (CollUtil.isNotEmpty(deleteIds)) {
                this.removeByIds(deleteIds);
            }
            // 批量插入和更新
            this.saveOrUpdateBatch(entitiesToAddOrUpdate);
            //更新值映射
            for (CfgProcessFieldMapDTO.AddOrUpdateDTO dto : addDTO) {
                if (!CharSequenceUtil.equals(dto.getThirdFieldType(), CfgQueryOptionFieldTypeEnum.CHECKBOXV2.getCode())
                        && !CharSequenceUtil.equals(dto.getThirdFieldType(), CfgQueryOptionFieldTypeEnum.RADIOV2.getCode())) {
                    // 如果不是单选或多选，则跳过值映射的添加,并且清空已存在的映射
                    cfgProcessValueMapService.delete(Collections.singletonList(dto.getId()));
                    continue;
                }
                List<CfgProcessValueMapDTO.AddOrUpdateDTO> processValueMapDTOList = dto.getProcessValueMapDTOList();
                if (ObjectUtil.isNotEmpty(processValueMapDTOList)) {
                    cfgProcessValueMapService.addOrUpdate(cfgProcessId,processDefinitionId, dto, processValueMapDTOList);
                }
            }
            //生成日志
            Map<String, CfgProcessFieldMapEntity> entityMap = entitiesToAddOrUpdate.stream()
                    .collect(Collectors.toMap(CfgProcessFieldMapEntity::getId, entity -> entity));
            oldList.forEach(item -> {
                CfgProcessFieldMapEntity entity = entityMap.get(item.getId());
                if (ObjectUtil.isNotEmpty(item)) {
                    operateLogService.addModuleOperateLogByObj(item, entity, ModuleTypeEnum.CFG_PROCESS.getCode(), cfgProcessId, CharSequenceUtil.format("流程编码【{}】字段配置",processDefinitionId));
                }
            });

            return new BaseResultDTO.AddDTO();
        } catch (Exception e) {
            log.info("字段配置更新失败："+e);
            throw new ServiceException("字段配置更新失败:{}", e.getMessage());
        }
    }

    /**
     * 根据配置id查询
     * @author will
     * @date 2025/7/8 17:28
     * @param cfgId
     * @return List<CfgProcessFieldMapEntity>
     */
    private List<CfgProcessFieldMapEntity> listByCfgId(String cfgId) {
        return lambdaQuery().eq(CfgProcessFieldMapEntity::getCfgId,cfgId).list();
    }

    /**
     * 查询需要删除的数据
     */
    private List<String> getDeleteIds(List<CfgProcessFieldMapEntity> newList, List<CfgProcessFieldMapEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(CfgProcessFieldMapEntity::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(CfgProcessFieldMapEntity::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }

    @Override
    public List<CfgProcessFieldMapDTO.ViewDTO> view(String processDefinitionId, String type) {
        try {
            GetApprovalResp approval = fsService.getApproval(processDefinitionId);
            String formStr = JSONUtil.toJsonStr(approval.getData());
            CfgProcessRuleTypeEnum ruleTypeEnum = CfgProcessRuleTypeEnum.getByCode(type);
            ProcessFormHandler handler = null;
            if (ruleTypeEnum != null) {
                handler = processFormFactory.getAssembleFormHandler(ruleTypeEnum.name());
            }
            if (handler == null) {
                ProcessSourcePlatformEnum sourcePlatformEnum = ProcessSourcePlatformEnum.getByCode(type);
                if (sourcePlatformEnum != null) {
                    handler = processFormFactory.getConstructBillHandler(sourcePlatformEnum.getCode());
                }
            }
            if (handler == null) {
                throw new ServiceException("无法获取对应的表单处理器，type参数无效: " + type);
            }
            List<CfgProcessFieldMapDTO.ViewDTO> viewDTOList = handler.parseForm(formStr);
            List<CfgProcessFieldMapDTO.ViewDTO> resultList = new ArrayList<>();
            for (CfgProcessFieldMapDTO.ViewDTO e :viewDTOList) {
                if (ObjectUtil.isNotEmpty(ruleTypeEnum)){
                    e.setCfgType(DictBasicEnum.THIRDCFG.getCode());
                }
                e.setCfgType(DictBasicEnum.SYSCFG.getCode());
                CfgQueryOptionFieldTypeEnum enumByCode = CfgQueryOptionFieldTypeEnum.getByCode(e.getThirdFieldType());
                if (ObjectUtil.isEmpty(enumByCode)) {
                    //类型为空的时候跳过本次循环
                    log.error("飞书审批定义中存在未知字段类型，字段名称：{}，字段类型：{}", e.getThirdField(), e.getThirdFieldType());
                    continue;
                }
                e.setThirdFieldTypeName(enumByCode.getName());
                resultList.add(e);
            }
            return resultList;
        } catch (Exception e) {
            throw new ServiceException("获取指定飞书审批定义失败:{}", e);
        }
    }

    public static void main(String[] args) {
        String s = "CHECKBOXV2";
        CfgQueryOptionFieldTypeEnum enumByCode = CfgQueryOptionFieldTypeEnum.getByCode(s.toUpperCase());
        System.out.println(enumByCode.getName());
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<String> mainIds) {
        // 符合 ruleId 存在于 ids 的更新
        List<CfgProcessFieldMapEntity> cfgProcessFieldMapEntityList = this.list(new LambdaQueryWrapper<CfgProcessFieldMapEntity>().in(CfgProcessFieldMapEntity::getCfgId, mainIds));
        if (CollectionUtils.isEmpty(cfgProcessFieldMapEntityList)) {
            return;
        }
        List<String> ids = cfgProcessFieldMapEntityList.stream().map(CfgProcessFieldMapEntity::getId).collect(Collectors.toList());
        removeByIds(ids);
        //删除选项条件设置
        //TODO 日志
        cfgProcessValueMapService.delete(ids);
    }


    /**
     * 新增修改处理数据
     * useType用于区分流程配置还是三方审批生成
     */
    private List<CfgProcessFieldMapEntity> handleData(String ruleId, List<CfgProcessFieldMapDTO.AddOrUpdateDTO> addDTO) {

        //过滤addDto，thirdField为空的数据
        List<CfgProcessFieldMapDTO.AddOrUpdateDTO> thirdFieldNotEmptyDTO = addDTO.stream().filter(dto -> StrUtil.isNotBlank(dto.getThirdFieldId())).collect(Collectors.toList());
        // 先校验所有 DTO，收集需要新增和更新的实体
        List<CfgProcessFieldMapEntity> entitiesToAddOrUpdate = new ArrayList<>();
        for (CfgProcessFieldMapDTO.AddOrUpdateDTO dto : thirdFieldNotEmptyDTO) {
            if (dto.getSysField().equals(CfgQueryOptionFieldTypeEnum.DEFAULT.getCode()) || dto.getSysField().equals(CfgQueryOptionFieldTypeEnum.NULLVALUE.getCode())) {
                // 校验通过后，进行保存或更新操作
                if (StrUtil.isEmpty(dto.getId())) {
                    dto.setId(IdWorker.getIdStr());
                }
                CfgProcessFieldMapEntity entity = new CfgProcessFieldMapEntity();
                BeanMapperUtils.copy(dto, entity);
                entity.setCfgId(ruleId); // 设置关联的 ruleId
                entitiesToAddOrUpdate.add(entity);
                continue;
            }
            // 校验 dto 的 third_field_type 和 sys_field_type
            CfgQueryOptionFieldTypeEnum thirdFieldType = CfgQueryOptionFieldTypeEnum.valueOf(dto.getThirdFieldType().toUpperCase());
            CfgQueryOptionFieldTypeEnum sysFieldType = CfgQueryOptionFieldTypeEnum.valueOf(dto.getSysFieldType().toUpperCase());
            if (ObjectUtil.isEmpty(thirdFieldType) || ObjectUtil.isEmpty(sysFieldType)) {
                    throw new ServiceException("请选择正确的第三方字段类型");
            }

            if ((thirdFieldType == CfgQueryOptionFieldTypeEnum.INPUT || thirdFieldType == CfgQueryOptionFieldTypeEnum.TEXTAREA) &&
                    (sysFieldType == CfgQueryOptionFieldTypeEnum.NUMBER || sysFieldType == CfgQueryOptionFieldTypeEnum.ATTACHMENTV2)) {
                throw new ServiceException("飞书文本不可生成数值，附件类型");
            }
            if (thirdFieldType == CfgQueryOptionFieldTypeEnum.NUMBER && sysFieldType == CfgQueryOptionFieldTypeEnum.ATTACHMENTV2) {
                throw new ServiceException("飞书数值不可生成附件");
            }
            if (thirdFieldType == CfgQueryOptionFieldTypeEnum.ATTACHMENTV2 && sysFieldType != CfgQueryOptionFieldTypeEnum.ATTACHMENTV2) {
                throw new ServiceException("飞书附件仅支持生成附件");
            }
            if (thirdFieldType == CfgQueryOptionFieldTypeEnum.RADIOV2 &&
                    (sysFieldType == CfgQueryOptionFieldTypeEnum.NUMBER || sysFieldType == CfgQueryOptionFieldTypeEnum.ATTACHMENTV2)) {
                throw new ServiceException("飞书单选项不可生成数值，附件");
            }
            if (thirdFieldType == CfgQueryOptionFieldTypeEnum.CHECKBOXV2 && sysFieldType != CfgQueryOptionFieldTypeEnum.CHECKBOXV2) {
                throw new ServiceException("飞书多选项仅可支持生成多选项");
            }
            if (thirdFieldType == CfgQueryOptionFieldTypeEnum.DATE && sysFieldType != CfgQueryOptionFieldTypeEnum.DATE) {
                throw new ServiceException("飞书日期仅支持转日期");
            }
            /*if ((dto.getIsDetailField() && CfgQueryOptionFieldBelongsTypeEnum.isFieldMain(dto.getSysParentId())) ||
                    (!dto.getIsDetailField() && !CfgQueryOptionFieldBelongsTypeEnum.isFieldMain(dto.getSysParentId()))) {
                throw new ServiceException("字段【{}】明细只能对应明细", dto.getThirdField());
            }*/
            // 校验通过后，进行保存或更新操作
            if (CharSequenceUtil.isEmpty(dto.getId())) {
                dto.setId(IdWorker.getIdStr());
            }
            CfgProcessFieldMapEntity entity = new CfgProcessFieldMapEntity();
            BeanMapperUtils.copy(dto, entity);
            entity.setCfgId(ruleId); // 设置关联的 ruleId
            entitiesToAddOrUpdate.add(entity);
        }
        return entitiesToAddOrUpdate;
    }

    // CfgProcessFieldMapServiceImpl.java

    /**
     * 校验飞书必填字段是否都已提供
     * @param processDefinitionId 飞书的审批定义ID
     * @param type 规则类型，用于调用view方法
     * @param fieldMapDTOList 用户提交的字段配置列表
     */
    private void validateRequiredFsFields(String processDefinitionId, String type, List<CfgProcessFieldMapDTO.AddOrUpdateDTO> fieldMapDTOList) {
        // 1. 如果关键参数为空，则跳过校验 (某些场景可能不需要此校验)
        if (StrUtil.hasBlank(processDefinitionId, type)) {
            return;
        }

        // 2. 调用 view 方法获取飞书审批定义的完整字段信息
        List<CfgProcessFieldMapDTO.ViewDTO> allFeishuFields = this.view(processDefinitionId, type);

        // 3. 按ID分组所有必填字段。这能正确处理像“金额”字段（一个ID，多个组件）的情况
        Map<String, List<CfgProcessFieldMapDTO.ViewDTO>> requiredFieldsGroupedById = allFeishuFields.stream()
                .filter(CfgProcessFieldMapDTO.ViewDTO::getThirdFieldRequired)
                .collect(Collectors.groupingBy(CfgProcessFieldMapDTO.ViewDTO::getThirdFieldId));

        // 如果定义中没有任何必填项，直接返回
        if (requiredFieldsGroupedById.isEmpty()) {
            return;
        }

        // 4. 同样，按ID对用户提交的字段进行分组
        Map<String, List<CfgProcessFieldMapDTO.AddOrUpdateDTO>> providedFieldsGroupedById = fieldMapDTOList.stream()
                .filter(dto -> StrUtil.isNotEmpty(dto.getThirdFieldId()))
                .collect(Collectors.groupingBy(CfgProcessFieldMapDTO.AddOrUpdateDTO::getThirdFieldId));

        // 5. 遍历每个必填字段组，检查其所有组件是否都已满足
        List<String> missingFieldNames = new ArrayList<>();
        requiredFieldsGroupedById.forEach((requiredId, requiredComponents) -> {
            // 获取此ID下所有必填组件的名称集合，例如 ["请款金额", "请款金额(币种)"]
            Set<String> requiredComponentNames = requiredComponents.stream()
                    .map(CfgProcessFieldMapDTO.ViewDTO::getThirdField)
                    .collect(Collectors.toSet());

            List<CfgProcessFieldMapDTO.AddOrUpdateDTO> providedComponents = providedFieldsGroupedById.get(requiredId);

            if (CollUtil.isEmpty(providedComponents)) {
                // 如果用户提交的数据中完全不包含此ID，则其所有组件都视为缺失
                missingFieldNames.addAll(requiredComponentNames);
            } else {
                // 如果ID存在，则需进一步比对组件名称，检查是否所有组件都已提供
                Set<String> providedComponentNames = providedComponents.stream()
                        .map(CfgProcessFieldMapDTO.AddOrUpdateDTO::getThirdField)
                        .collect(Collectors.toSet());

                // 从“必填”集合中，移除“已提供”的组件
                requiredComponentNames.removeAll(providedComponentNames);

                // 如果“必填”集合中仍有剩余，说明这些组件是缺失的
                missingFieldNames.addAll(requiredComponentNames);
            }
        });

        // 6. 如果“缺失字段”列表不为空，则抛出一个清晰、详细的异常
        if (CollUtil.isNotEmpty(missingFieldNames)) {
            throw new ServiceException("操作失败，缺少必填字段: " + String.join(", ", missingFieldNames));
        }
    }
}
