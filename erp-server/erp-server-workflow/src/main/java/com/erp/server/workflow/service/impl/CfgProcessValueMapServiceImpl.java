package com.erp.server.workflow.service.impl;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.excel.util.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.vo.LoginUser;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.workflow.dto.CfgProcessFieldMapDTO;
import com.erp.model.workflow.entity.CfgProcessValueMapEntity;
import com.erp.model.workflow.enums.FsRequestBodyAttributesEnum;
import com.erp.sdk.fs.service.FsService;
import com.erp.server.workflow.mapper.CfgProcessValueMapMapper;
import com.erp.server.workflow.service.CfgProcessValueMapService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.workflow.service.OperateLogService;
import com.common.core.exception.ServiceException;
import com.lark.oapi.service.approval.v4.model.GetApprovalResp;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.workflow.dto.CfgProcessValueMapDTO;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import com.common.core.utils.*;

import javax.annotation.Resource;

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

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(String cfgProcessId,String fieldMapId,List<CfgProcessValueMapDTO.AddOrUpdateDTO> addDTO) {
        List<CfgProcessValueMapEntity> entities = addDTO.stream()
                .map(dto -> {
                    CfgProcessValueMapEntity entity = new CfgProcessValueMapEntity();
                    BeanMapperUtils.copy(dto, entity);
                    entity.setFieldMapId(fieldMapId);
                    return entity;
                })
                .collect(Collectors.toList());
        if (entities.isEmpty()) {
            return new BaseResultDTO.AddDTO();
        }
        boolean b = this.saveOrUpdateBatch(entities);
        if(!b){
            throw new ServiceException("保存值映射失败");
        };
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】", UserContext.getDefaultLoginUser().getUserName(), "流程设置值映射");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CFG_PROCESS.getCode(), cfgProcessId, "新增操作");

        return new BaseResultDTO.AddDTO();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO addOrUpdate(String cfgProcessId,String fieldMapId,List<CfgProcessValueMapDTO.AddOrUpdateDTO> addDTO) {
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
        add(cfgProcessId, fieldMapId, addDTOs);
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
        boolean b = this.saveOrUpdateBatch(entities);
        if(!b){
            throw new ServiceException("保存值映射失败");
        };
        //生成日志
        existingEntities.forEach(entity -> {
            CfgProcessValueMapEntity ruleEntity = entityMap.get(entity.getId());
            if (ObjectUtil.isNotEmpty(ruleEntity)) {
                operateLogService.addModuleOperateLogByObj(entity, ruleEntity, ModuleTypeEnum.CFG_PROCESS.getCode(), cfgProcessId, "更新操作");
            }
        });

        return new BaseResultDTO.AddDTO();
    }

    @Override
    public List<CfgProcessValueMapDTO.DropDownDTO> view(String fieldId,String approvalCode) {
        try {
            GetApprovalResp approval = fsService.getApproval(approvalCode);
            String jsonStr = JSONUtil.toJsonStr(approval.getData());
            Map<String, Map<String, String>> stringMapMap = parseFormValue(jsonStr);
            Map<String, String> stringMap = stringMapMap.get(fieldId);
            //遍历map，key作为DropDownDTO的value，value作为DropDownDTO的name
            List<CfgProcessValueMapDTO.DropDownDTO> dropDownDTOS = new ArrayList<>();
            for (Map.Entry<String, String> entry : stringMap.entrySet()) {
                CfgProcessValueMapDTO.DropDownDTO dropDownDTO = new CfgProcessValueMapDTO.DropDownDTO();
                dropDownDTO.setName(entry.getValue());
                dropDownDTO.setValue(entry.getKey());
                dropDownDTOS.add(dropDownDTO);
            }
            CfgProcessValueMapDTO.DropDownDTO dropDownDTO = new CfgProcessValueMapDTO.DropDownDTO();
            dropDownDTO.setName("默认值");
            dropDownDTO.setValue(stringMap.get("default"));
            dropDownDTOS.add(dropDownDTO);
            return dropDownDTOS;
        } catch (Exception e) {
            throw new ServiceException("飞书选项值列表转换异常");
        }
    }

    @Override
    public void delete(List<String> mainIds) {
        // 当前用户信息
        LoginUser loginUser = UserContext.getDefaultLoginUser();
        // 符合 ruleId 存在于 ids 的更新
        List<CfgProcessValueMapEntity> cfgProcessValueMapEntities = this.list(new LambdaQueryWrapper<CfgProcessValueMapEntity>().in(CfgProcessValueMapEntity::getFieldMapId, mainIds));
        if (CollectionUtils.isEmpty(cfgProcessValueMapEntities)) {
            return;
        }
        //创建一个List<String>长度为cfgProcessValueMapEntities的长度
        List<String> ids = new ArrayList<>(cfgProcessValueMapEntities.size());
        cfgProcessValueMapEntities.forEach(item -> {item.setIsDeleted(true)
                .setUpdateTime(LocalDateTime.now())
                .setUpdateUserId(loginUser.getUid())
                .setUpdateUserName(loginUser.getUserName());
            ids.add(item.getId());
        });
        // 批量更新
        this.updateBatchById(cfgProcessValueMapEntities);
        log.info("删除流程设置执行条件: {}", ids);
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(CfgProcessValueMapEntity cfgProcessValueMapEntity) {
    // TODO 验证数据 & 数据赋值
    }

    public static Map<String, Map<String, String>> parseFormValue(String formString) {
        JSONObject root = JSONUtil.parseObj(formString);
        String formStringValue = root.getStr(FsRequestBodyAttributesEnum.FORM.getCode());
        JSONArray formArray = JSONUtil.parseArray(formStringValue);
        //处理下拉数据
        Map<String, Map<String, String>> parsedValues = new HashMap<>();
        for (JSONObject field : formArray.jsonIter()) {
            String fieldId = field.getStr(FsRequestBodyAttributesEnum.ID.getCode());
            String type = field.getStr(FsRequestBodyAttributesEnum.TYPE.getCode());
            if (!FsRequestBodyAttributesEnum.FIELDLIST.getCode().equals(type) && field.containsKey(FsRequestBodyAttributesEnum.OPTION.getCode())) {
                Object valueObj = field.get(FsRequestBodyAttributesEnum.OPTION.getCode());
                if (valueObj instanceof JSONArray) {
                    JSONArray valueList = (JSONArray) valueObj;
                    //校验valueList是否为null
                    if (valueList == null) {
                        continue;
                    }
                    Map<String, String> valueMap = new HashMap<>();
                    for (JSONObject value : valueList.jsonIter()) {
                        valueMap.put(value.getStr(FsRequestBodyAttributesEnum.VALUE.getCode()), value.getStr(FsRequestBodyAttributesEnum.TEXT.getCode()));
                    }
                    parsedValues.put(fieldId, valueMap);
                } else if (valueObj != null) {
                    // 如果不是 JSONArray，则直接处理为单个值
                    Map<String, String> valueMap = new HashMap<>();
                    valueMap.put(FsRequestBodyAttributesEnum.VALUE.getCode(), valueObj.toString());
                    parsedValues.put(fieldId, valueMap);
                }
            } else if (FsRequestBodyAttributesEnum.FIELDLIST.getCode().equals(type)) {
                JSONArray detailFields = field.getJSONArray(FsRequestBodyAttributesEnum.CHILDREN.getCode());
                if (detailFields != null) {
                    for (JSONObject detail : detailFields.jsonIter()) {
                        String detailId = detail.getStr("id");
                        if (detail.containsKey(FsRequestBodyAttributesEnum.OPTION.getCode())) {
                            Object valueObj = detail.get(FsRequestBodyAttributesEnum.OPTION.getCode());
                            if (valueObj instanceof JSONArray) {
                                JSONArray valueList = (JSONArray) valueObj;
                                //校验valueList是否为null
                                if (valueList == null) {
                                    continue;
                                }
                                Map<String, String> valueMap = new HashMap<>();
                                for (JSONObject value : valueList.jsonIter()) {
                                    valueMap.put(value.getStr(FsRequestBodyAttributesEnum.VALUE.getCode()), value.getStr(FsRequestBodyAttributesEnum.TEXT.getCode()));
                                }
                                parsedValues.put(detailId, valueMap);
                            } else if (valueObj != null) {
                                // 如果不是 JSONArray，则直接处理为单个值
                                JSONObject object = (JSONObject) valueObj;
                                Map<String, String> valueMap = new HashMap<>();
                                valueMap.put(object.getStr(FsRequestBodyAttributesEnum.VALUE.getCode()), object.getStr(FsRequestBodyAttributesEnum.TEXT.getCode()));
                                parsedValues.put(detailId, valueMap);
                            }
                        }
                    }
                }
            }
        }
        return parsedValues;
    }
}
