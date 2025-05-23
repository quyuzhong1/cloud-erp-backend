package com.erp.server.workflow.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.excel.util.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.RedisService;
import com.common.business.vo.LoginUser;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.workflow.dto.CfgProcessFieldMapDTO;
import com.erp.model.workflow.entity.CfgProcessValueMapEntity;
import com.erp.model.workflow.enums.CfgQueryOptionFieldTypeEnum;
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
import java.util.concurrent.TimeUnit;
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

    @Resource
    private RedisService redisService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(String cfgProcessId, String fieldMapId, List<CfgProcessValueMapDTO.AddOrUpdateDTO> addDTO) {
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
        String msg = StrUtil.format("用户【{}】新增【{}】", UserContext.getDefaultLoginUser().getUserName(), "流程设置值映射");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CFG_PROCESS.getCode(), cfgProcessId, "新增操作");

        return new BaseResultDTO.AddDTO();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO addOrUpdate(String cfgProcessId, String fieldMapId, List<CfgProcessValueMapDTO.AddOrUpdateDTO> addDTO) {
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
                operateLogService.addModuleOperateLogByObj(entity, ruleEntity, ModuleTypeEnum.CFG_PROCESS.getCode(), cfgProcessId, "更新操作");
            }
        });

        return new BaseResultDTO.AddDTO();
    }

    @Override
    public List<CfgProcessValueMapDTO.DropDownDTO> view(String fieldId, String approvalCode) {
        try {
            GetApprovalResp approval = fsService.getApproval(approvalCode);
            String jsonStr = JSONUtil.toJsonStr(approval.getData());
            if (ObjectUtil.isNotEmpty(redisService.getCacheObject(fieldId))){
                return redisService.getCacheObject(fieldId);
            }
            Map<String, Map<String, String>> stringMapMap = parseFormValue(jsonStr);
            Map<String, String> stringMap = stringMapMap.get(fieldId);
            //遍历map，key作为DropDownDTO的value，value作为DropDownDTO的name
            List<CfgProcessValueMapDTO.DropDownDTO> dropDownDTOS = new ArrayList<>();
            for (Map.Entry<String, String> entry : stringMap.entrySet()) {
                CfgProcessValueMapDTO.DropDownDTO dropDownDTO = new CfgProcessValueMapDTO.DropDownDTO();
                dropDownDTO.setName(entry.getValue());
                dropDownDTO.setValue(entry.getKey());
                dropDownDTOS.add(dropDownDTO);
                redisService.setNx(entry.getKey(), entry.getValue(),10*60,  TimeUnit.SECONDS);
            }

            return dropDownDTOS;
        } catch (Exception e) {
            throw new ServiceException("飞书选项值列表转换异常：{}", e.getMessage());
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
        cfgProcessValueMapEntities.forEach(item -> {
            item.setIsDeleted(true)
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

    /**
     * 解析表单值，提取下拉选项和其他特定字段类型的值映射
     *
     * @param formString 表单JSON字符串
     * @return 字段ID到值映射的Map
     */
    public static Map<String, Map<String, String>> parseFormValue(String formString) {
        JSONObject root = JSONUtil.parseObj(formString);
        String formStringValue = root.getStr(FsRequestBodyAttributesEnum.FORM.getCode());
        JSONArray formArray = JSONUtil.parseArray(formStringValue);

        Map<String, Map<String, String>> parsedValues = new HashMap<>();

        // 遍历表单字段
        for (JSONObject field : formArray.jsonIter()) {
            String fieldId = field.getStr(FsRequestBodyAttributesEnum.ID.getCode());
            String type = field.getStr(FsRequestBodyAttributesEnum.TYPE.getCode());

            if (FsRequestBodyAttributesEnum.FIELDLIST.getCode().equals(type)) {
                // 处理明细表字段
                processDetailFields(field, parsedValues);
            } else {
                // 处理普通字段
                processField(field, fieldId, type, parsedValues);
            }
        }

        return parsedValues;
    }

    /**
     * 处理普通字段
     */
    private static void processField(JSONObject field, String fieldId, String type, Map<String, Map<String, String>> parsedValues) {
        // 处理金额类型
        if (CfgQueryOptionFieldTypeEnum.AMOUNT.getCode().equals(type)) {
            processAmountField(field, fieldId, parsedValues);
            return;
        }

        // 处理包含选项的字段
        if (field.containsKey(FsRequestBodyAttributesEnum.OPTION.getCode())) {
            Object valueObj = field.get(FsRequestBodyAttributesEnum.OPTION.getCode());
            if (valueObj instanceof JSONArray) {
                processOptionArray((JSONArray) valueObj, fieldId, parsedValues);
            } else if (valueObj != null && !field.get(FsRequestBodyAttributesEnum.TYPE.getCode()).equals(CfgQueryOptionFieldTypeEnum.DEPARTMENT.getCode())) {
                // 处理单个值选项
                Map<String, String> valueMap = new HashMap<>();
                valueMap.put(FsRequestBodyAttributesEnum.VALUE.getCode(), valueObj.toString());
                parsedValues.put(fieldId, valueMap);
            }
        }
    }

    /**
     * 处理明细表字段
     */
    private static void processDetailFields(JSONObject field, Map<String, Map<String, String>> parsedValues) {
        JSONArray detailFields = field.getJSONArray(FsRequestBodyAttributesEnum.CHILDREN.getCode());
        if (detailFields == null) {
            return;
        }

        for (JSONObject detail : detailFields.jsonIter()) {
            String detailId = detail.getStr("id");
            String detailType = detail.getStr(FsRequestBodyAttributesEnum.TYPE.getCode());

            if (!detail.containsKey(FsRequestBodyAttributesEnum.OPTION.getCode())) {
                continue;
            }

            // 处理明细表中的字段
            processField(detail, detailId, detailType, parsedValues);
        }
    }

    /**
     * 处理金额类型字段
     */
    private static void processAmountField(JSONObject field, String fieldId, Map<String, Map<String, String>> parsedValues) {
        Object obj = field.get(FsRequestBodyAttributesEnum.OPTION.getCode());
        if (obj == null) {
            return;
        }

        Map<String, String> valueMap = new HashMap<>();
        JSONObject option = (JSONObject) obj;
        JSONArray currencyRange = option.getJSONArray("currencyRange");

        if (currencyRange != null) {
            for (Object currency : currencyRange) {
                valueMap.put(currency.toString(), currency.toString());
            }
            parsedValues.put(fieldId, valueMap);
        }
    }

    /**
     * 处理选项数组
     */
    private static void processOptionArray(JSONArray valueList, String fieldId, Map<String, Map<String, String>> parsedValues) {
        if (valueList == null) {
            return;
        }

        Map<String, String> valueMap = new HashMap<>();
        for (JSONObject value : valueList.jsonIter()) {
            String valueCode = value.getStr(FsRequestBodyAttributesEnum.VALUE.getCode());
            String valueText = value.getStr(FsRequestBodyAttributesEnum.TEXT.getCode());
            if (valueCode != null && valueText != null) {
                valueMap.put(valueCode, valueText);
            }
        }

        if (!valueMap.isEmpty()) {
            parsedValues.put(fieldId, valueMap);
        }
    }

    /**
     * 处理单个选项对象
     */
    private static void processSingleOption(JSONObject option, String fieldId, Map<String, Map<String, String>> parsedValues) {
        if (option == null) {
            return;
        }

        String valueCode = option.getStr(FsRequestBodyAttributesEnum.VALUE.getCode());
        String valueText = option.getStr(FsRequestBodyAttributesEnum.TEXT.getCode());

        if (valueCode != null && valueText != null) {
            Map<String, String> valueMap = new HashMap<>();
            valueMap.put(valueCode, valueText);
            parsedValues.put(fieldId, valueMap);
        }
    }
}
