package com.erp.server.workflow.service.impl;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.excel.util.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.vo.LoginUser;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.workflow.dto.CfgProcessValueMapDTO;
import com.erp.model.workflow.entity.CfgProcessFieldMapEntity;
import com.erp.model.workflow.entity.CfgQueryOptionEntity;
import com.erp.model.workflow.enums.CfgQueryOptionFieldBelongsTypeEnum;
import com.erp.model.workflow.enums.CfgQueryOptionFieldTypeEnum;
import com.erp.model.workflow.enums.FsRequestBodyAttributesEnum;
import com.erp.sdk.fs.service.FsService;
import com.erp.server.workflow.mapper.CfgProcessFieldMapMapper;
import com.erp.server.workflow.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.workflow.service.OperateLogService;
import com.common.core.exception.ServiceException;
import com.lark.oapi.service.approval.v4.model.GetApprovalResp;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.workflow.dto.CfgProcessFieldMapDTO;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;

import javax.annotation.Resource;

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

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(String bussinessKey, String cfgProcessId, String ruleId, List<CfgProcessFieldMapDTO.AddOrUpdateDTO> addDTO) {
        List<CfgProcessFieldMapEntity> entitiesToAddOrUpdate = handleData(bussinessKey, ruleId, addDTO);

        // 批量插入和更新
        boolean b = this.saveOrUpdateBatch(entitiesToAddOrUpdate);
        if (!b) {
            throw new ServiceException("流程设置字段配置新增失败");
        }
        //插入值映射
        for (CfgProcessFieldMapDTO.AddOrUpdateDTO dto : addDTO) {
            String id = dto.getId();
            List<CfgProcessValueMapDTO.AddOrUpdateDTO> processValueMapDTOList = dto.getProcessValueMapDTOList();
            if (ObjectUtil.isNotEmpty(processValueMapDTOList)) {
                cfgProcessValueMapService.add(cfgProcessId, id, processValueMapDTOList);
            }
        }
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】", UserContext.getDefaultLoginUser().getUserName(), "流程设置字段配置");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CFG_PROCESS.getCode(), cfgProcessId, "新增操作");
        return new BaseResultDTO.AddDTO();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO addOrUpdate(String bussinessKey, String cfgProcessId, String ruleId, List<CfgProcessFieldMapDTO.AddOrUpdateDTO> addDTO) {
        // 遍历addDTO，id为空的保存，id不为空的更新，使用ruleId查询ruleId的记录，如果查询的结果数小于addDTO数量，那么找出结果中未包含于addDTO的中的id，然后将此id对应的entiy删除
        // 查询数据库中与 ruleId 关联的记录
        List<CfgProcessFieldMapEntity> existingEntities = this.list(
                new LambdaQueryWrapper<CfgProcessFieldMapEntity>()
                        .eq(CfgProcessFieldMapEntity::getCfgId, ruleId)
                        .eq(CfgProcessFieldMapEntity::getIsDeleted, false)
        );

        //校验更新数据
        List<CfgProcessFieldMapEntity> entitiesToAddOrUpdate = handleData(bussinessKey, ruleId, addDTO);
        // 批量插入和更新
        boolean b = this.saveOrUpdateBatch(entitiesToAddOrUpdate);
        if (!b) {
            throw new ServiceException("流程设置字段配置更新失败");
        }
        //更新值映射
        for (CfgProcessFieldMapDTO.AddOrUpdateDTO dto : addDTO) {
            String id = dto.getId();
            List<CfgProcessValueMapDTO.AddOrUpdateDTO> processValueMapDTOList = dto.getProcessValueMapDTOList();
            if (ObjectUtil.isNotEmpty(processValueMapDTOList)) {
                cfgProcessValueMapService.addOrUpdate(cfgProcessId, id, processValueMapDTOList);
            }
        }
        //生成日志
        Map<String, CfgProcessFieldMapEntity> entityMap = entitiesToAddOrUpdate.stream()
                .collect(Collectors.toMap(CfgProcessFieldMapEntity::getId, entity -> entity));
        existingEntities.forEach(old -> {
            CfgProcessFieldMapEntity entity = entityMap.get(old.getId());
            if (ObjectUtil.isNotEmpty(old)) {
                operateLogService.addModuleOperateLogByObj(old, entity, ModuleTypeEnum.CFG_PROCESS.getCode(), cfgProcessId, "更新操作");
            }
        });

        return new BaseResultDTO.AddDTO();
    }

    @Override
    public List<CfgProcessFieldMapDTO.ViewDTO> view(String processDefinitionId) {
        try {
            GetApprovalResp approval = fsService.getApproval(processDefinitionId);
            String formStr = JSONUtil.toJsonStr(approval.getData());
            List<CfgProcessFieldMapDTO.ViewDTO> viewDTOList = parseForm(formStr);
            return viewDTOList;
        } catch (Exception e) {
            throw new ServiceException("获取指定飞书审批定义失败");
        }
    }

    @Override
    @Transactional
    public void delete(List<String> mainIds) {
        // 当前用户信息
        LoginUser loginUser = UserContext.getDefaultLoginUser();
        // 符合 ruleId 存在于 ids 的更新
        List<CfgProcessFieldMapEntity> cfgProcessFieldMapEntityList = this.list(new LambdaQueryWrapper<CfgProcessFieldMapEntity>().in(CfgProcessFieldMapEntity::getCfgId, mainIds));
        if (CollectionUtils.isEmpty(cfgProcessFieldMapEntityList)) {
            return;
        }
        List<String> ids = new ArrayList<>(cfgProcessFieldMapEntityList.size());
        cfgProcessFieldMapEntityList.forEach(item -> {
            item.setIsDeleted(true)
                    .setUpdateTime(LocalDateTime.now())
                    .setUpdateUserId(loginUser.getUid())
                    .setUpdateUserName(loginUser.getUserName());
            ids.add(item.getId());
        });
        // 批量更新
        this.updateBatchById(cfgProcessFieldMapEntityList);
        log.info("删除流程设置执行条件: {}", ids);
        //删除选项条件设置
        cfgProcessValueMapService.delete(cfgProcessFieldMapEntityList.stream().map(CfgProcessFieldMapEntity::getId).collect(Collectors.toList()));
    }


    /**
     * 新增修改处理数据
     */
    private List<CfgProcessFieldMapEntity> handleData(String bussinessKey, String ruleId, List<CfgProcessFieldMapDTO.AddOrUpdateDTO> addDTO) {
        List<CfgQueryOptionEntity> cfgQueryOptionEntities = cfgQueryOptionService.list(new LambdaQueryWrapper<CfgQueryOptionEntity>().eq(CfgQueryOptionEntity::getBussinessKey, bussinessKey).eq(CfgQueryOptionEntity::getIsDeleted, false));
        List<String> fieldList = cfgQueryOptionEntities.stream().map(CfgQueryOptionEntity::getConditionField).collect(Collectors.toList());
        Map<String, CfgQueryOptionEntity> fieldToEntityMap = cfgQueryOptionEntities.stream().collect(Collectors.toMap(CfgQueryOptionEntity::getConditionField, e -> e));
        // 遍历 addDTO，id 为空的保存，id 不为空的更新
        // 先校验所有 DTO，收集需要新增和更新的实体
        List<CfgProcessFieldMapEntity> entitiesToAddOrUpdate = new ArrayList<>();

        for (CfgProcessFieldMapDTO.AddOrUpdateDTO dto : addDTO) {
            // 校验 dto 的 third_field_type 和 sys_field_type
            CfgQueryOptionFieldTypeEnum thirdFieldType = CfgQueryOptionFieldTypeEnum.valueOf(dto.getThirdFieldType().toUpperCase());
            CfgQueryOptionFieldTypeEnum sysFieldType = CfgQueryOptionFieldTypeEnum.valueOf(dto.getSysFieldType().toUpperCase());

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
            if (thirdFieldType == CfgQueryOptionFieldTypeEnum.DATETIME && sysFieldType != CfgQueryOptionFieldTypeEnum.DATETIME) {
                throw new ServiceException("飞书日期仅支持转日期");
            }
            if (dto.getIsDetailField()!=null && !CfgQueryOptionFieldBelongsTypeEnum.DETAIL.getCode().equals(fieldToEntityMap.get(dto.getSysField()).getFieldBelongsType())){
                throw new ServiceException("明细只能对应明细");
            }
            // 判断必填是不是已配置
            if (fieldList.contains(dto.getSysField())) {
                fieldList.remove(dto.getSysField());
            }
            // 校验通过后，进行保存或更新操作
            if (StrUtil.isEmpty(dto.getId())) {
                dto.setId(IdWorker.getIdStr());
            }
            CfgProcessFieldMapEntity entity = new CfgProcessFieldMapEntity();
            BeanMapperUtils.copy(dto, entity);
            entity.setCfgId(ruleId); // 设置关联的 ruleId
            entitiesToAddOrUpdate.add(entity);
        }
        if (ObjectUtil.isNotEmpty(fieldList) && fieldList.size() > 0) {
            //将fieldList转为一个字符串
            String fieldListStr = String.join(",", fieldList);
            throw new ServiceException("存在{}尚未映射，无法提交保存", fieldListStr);
        }
        return entitiesToAddOrUpdate;
    }

    //解析form数据
    public static List<CfgProcessFieldMapDTO.ViewDTO> parseForm(String formString) {
        JSONObject root = JSONUtil.parseObj(formString);
        String form = root.getStr(FsRequestBodyAttributesEnum.FORM.getCode());
        JSONArray formArray = JSONUtil.parseArray(form);
        List<CfgProcessFieldMapDTO.ViewDTO> viewDTOList = new ArrayList<>();
        for (cn.hutool.json.JSONObject field : formArray.jsonIter()) {
            CfgProcessFieldMapDTO.ViewDTO viewDTO = new CfgProcessFieldMapDTO.ViewDTO();
            viewDTO.setThirdField(field.getStr(FsRequestBodyAttributesEnum.NAME.getCode()));
            viewDTO.setThirdFieldType(field.getStr(FsRequestBodyAttributesEnum.TYPE.getCode()));
            viewDTO.setThirdFieldRequired(field.getBool(FsRequestBodyAttributesEnum.REQUIRED.getCode(), false));
            viewDTO.setThirdFieldId(field.getStr(FsRequestBodyAttributesEnum.ID.getCode())); // 父级 fieldList 的 ID

            if ("fieldList".equals(field.getStr(FsRequestBodyAttributesEnum.TYPE.getCode()))) {
                viewDTO.setIsDetailField(true);
                JSONArray detailFields = field.getJSONArray(FsRequestBodyAttributesEnum.CHILDREN.getCode());

                for (JSONObject detail : detailFields.jsonIter()) {
                    CfgProcessFieldMapDTO.ViewDTO detailViewDTO = new CfgProcessFieldMapDTO.ViewDTO();
                    detailViewDTO.setThirdField(detail.getStr(FsRequestBodyAttributesEnum.NAME.getCode()));
                    detailViewDTO.setThirdFieldType(detail.getStr(FsRequestBodyAttributesEnum.TYPE.getCode()));
                    detailViewDTO.setThirdFieldRequired(detail.getBool(FsRequestBodyAttributesEnum.REQUIRED.getCode(), false));
                    detailViewDTO.setThirdFieldId(detail.getStr(FsRequestBodyAttributesEnum.ID.getCode())); // 父级 fieldList 的 ID
                    detailViewDTO.setIsDetailField(true);
                    detailViewDTO.setParentId(field.getStr(FsRequestBodyAttributesEnum.ID.getCode()));
                    viewDTOList.add(detailViewDTO); // 将子元素直接添加到 viewDTOList
                }
                continue; // 跳过当前 viewDTO 的添加
            }
            viewDTOList.add(viewDTO);
        }
        return viewDTOList;
    }
}
