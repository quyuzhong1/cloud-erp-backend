package com.erp.server.workflow.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.vo.ThirdUnionDTO;
import com.erp.model.workflow.dto.*;
import com.erp.model.workflow.entity.*;
import com.erp.model.workflow.enums.*;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.sdk.fs.service.FsService;
import com.erp.server.workflow.context.ProcessFormFactory;
import com.erp.server.workflow.handler.ProcessFormHandler;
import com.erp.server.workflow.mapper.CfgProcessMapper;
import com.erp.server.workflow.service.*;
import com.lark.oapi.service.approval.v4.model.CreateInstanceReq;
import com.lark.oapi.service.approval.v4.model.InstanceCreate;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_PROCESS_CFG_PROCESS;

/**
 * <p>
 * 流程配置 服务实现类
 * </p>
 *
 * @author hcg
 * @since 2025-05-12
 */
@Slf4j
@Service
public class CfgProcessServiceImpl extends SuperServiceImpl<CfgProcessMapper, CfgProcessEntity> implements CfgProcessService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;

    @Resource
    private CfgProcessRuleService cfgProcessRuleService;

    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Resource
    private ThirdProcessDefinitionService thirdProcessDefinitionService;

    @Resource
    private CfgProcessFieldMapService cfgProcessFieldMapService;

    @Resource
    private CfgProcessValueMapService cfgProcessValueMapService;

    @Resource
    private ProcessFormFactory processFormFactory;

    @Resource
    private FsService fsService;

    @Resource
    private ApproveTaskInfoService approveTaskInfoService;

    @Resource
    private SysUserFeign sysUserFeign;
    @Resource
    private CfgQueryOptionService cfgQueryOptionService;
    @Resource
    private WorkMenuService workMenuService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(@RequestBody @Validated CfgProcessDTO.AddOrUpdateDTO dto) {
        //
        List<CfgProcessEntity> list = this.list(new LambdaQueryWrapper<CfgProcessEntity>().eq(CfgProcessEntity::getBussinessKey, dto.getBussinessKey()).eq(CfgProcessEntity::getIsDeleted, false));
        if (CollUtil.isNotEmpty(list) && list.size() > 0) {
            throw new ServiceException("{}已配置流程，不可重复配置", CfgQueryOptionBussinessKeyEnum.getByCode(dto.getBussinessKey()) != null ? CfgQueryOptionBussinessKeyEnum.getByCode(dto.getBussinessKey()).getName() : dto.getBussinessKey());
        }
        CfgProcessEntity cfgProcessEntity = new CfgProcessEntity();
        BeanMapperUtils.copy(dto, cfgProcessEntity);
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_LCPZ);
        cfgProcessEntity.setCode(code);
        log.info("开始新增流程配置");
        boolean save = super.save(cfgProcessEntity);
        if (!save) {
            throw new ServiceException("流程配置保存失败");
        }
        //保存执行条件
        cfgProcessRuleService.add(cfgProcessEntity.getBussinessKey(), cfgProcessEntity.getId(), dto.getProcessRuleDTOList());
        // 操作日志
        String msg = StrUtil.format("新增-{}-{}", cfgProcessEntity.getName(), cfgProcessEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CFG_PROCESS.getCode(), cfgProcessEntity.getId(), "新增操作");
        return new BaseResultDTO.AddDTO(cfgProcessEntity.getId(), code);
    }

    @Override
    public BaseResultDTO.AddDTO update(@RequestBody @Validated CfgProcessDTO.AddOrUpdateDTO dto) {
        if (dto.getId() == null) {
            throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "流程配置id不能为空");
        }
        CfgProcessEntity old = this.getById(dto.getId());
        CfgProcessEntity cfgProcessEntity = new CfgProcessEntity();
        BeanMapperUtils.copy(dto, cfgProcessEntity);
        //code不能修改
        cfgProcessEntity.setCode(old.getCode());
        log.info("开始编辑流程配置");
        boolean save = super.updateById(cfgProcessEntity);
        if (!save) {
            throw new ServiceException("流程配置保存失败");
        }
        //保存执行条件
        cfgProcessRuleService.update(cfgProcessEntity.getBussinessKey(), cfgProcessEntity.getId(), dto.getProcessRuleDTOList());
        // 操作日志
        operateLogService.addModuleOperateLogByObj(old, cfgProcessEntity, ModuleTypeEnum.CFG_PROCESS.getCode(), cfgProcessEntity.getId(), "流程配置");
        return new BaseResultDTO.AddDTO(cfgProcessEntity.getId(), dto.getCode());
    }

    @Override
    public PagingVO<CfgProcessDTO.ProcessViewDTO> paging(@RequestBody @Validated PagingDTO<CfgProcessDTO.SearchParamDTO> dto) {
        CfgProcessDTO.SearchParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<CfgProcessDTO.ProcessViewDTO> pageData = baseMapper.getProcessWithRuleAndAggregatedExps(query, params);
        handlePaging(pageData.getRecords());
        return new PagingVO(pageData);
    }

    /**
     * 处理分页数据
     * @author will
     * @date 2025/6/27 14:58
     * @param list
     * @return void
     */
    private void handlePaging (List<CfgProcessDTO.ProcessViewDTO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        for (CfgProcessDTO.ProcessViewDTO viewDTO : list) {
            viewDTO.setDisabledName(viewDTO.getDisabled() ? "禁用" : "启用");
        }
    }

    @Override
    public CfgProcessDTO.ViewDTO view(String settingId) {
        CfgProcessDTO.ViewDTO viewDTO = baseMapper.getViewDTOById(settingId);
        if (viewDTO == null){
            throw new ServiceException(ApiError.BILL_NOT_EXIST);
        }
        for (CfgProcessRuleDTO.ViewDTO ruleDto : viewDTO.getProcessRuleDTOList()) {
            if (ObjectUtil.isEmpty(ruleDto.getProcessFieldMapDTOList())) {
                continue;
            }
            for (CfgProcessFieldMapDTO.ViewDTO fieldMapDto : ruleDto.getProcessFieldMapDTOList()) {
                if (StrUtil.isBlank(fieldMapDto.getThirdFieldType())) {
                    continue;
                }
                //唯一值
                if (CharSequenceUtil.isBlank(fieldMapDto.getSysParentId()) || CharSequenceUtil.equals(fieldMapDto.getSysField(),"default") || CharSequenceUtil.equals(fieldMapDto.getSysField(),"nullValue")) {
                    fieldMapDto.setUniqueCode(fieldMapDto.getSysField());
                } else {
                    String uniqueCode = CharSequenceUtil.format("{}-{}",  fieldMapDto.getSysParentId(), fieldMapDto.getSysField());
                    fieldMapDto.setUniqueCode(uniqueCode);
                }
                try {
                    // 转换为大写以匹配枚举名称的约定 (通常枚举常量是大写的)
                    fieldMapDto.setThirdFieldTypeName(CfgQueryOptionFieldTypeEnum.valueOf(fieldMapDto.getThirdFieldType().toUpperCase()).getName());
                } catch (IllegalArgumentException e) {
                    // 处理枚举值不存在的情况，例如记录日志或设置一个默认名称
                    // log.warn("未知的 thirdFieldType: {}", fieldMapDto.getThirdFieldType());
                    throw new ServiceException("流程配置详情接口飞，书字段类型Name转换异常");
                }
                if (StrUtil.isBlank(fieldMapDto.getSysFieldType())) {
                    continue;
                }
                try {
                    fieldMapDto.setSysFieldTypeName(CfgQueryOptionFieldTypeEnum.valueOf(fieldMapDto.getSysFieldType().toUpperCase()).getName());
                } catch (IllegalArgumentException e) {
                    // log.warn("未知的 sysFieldType: {}", fieldMapDto.getSysFieldType());
                    throw new ServiceException("流程配置详情接口,数大臣字段类型Name转换异常");
                }

            }
        }
        return viewDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO delete(CfgProcessRuleEntity entity) {

        CfgProcessEntity cfgProcessEntity = this.getById(entity.getCfgProcessId());
        if (ObjectUtil.isEmpty(cfgProcessEntity)) {
            throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE,"流程配置");
        }

        boolean delete = cfgProcessRuleService.delete(Collections.singletonList(entity.getId()));
        // 删除规则
        if (!delete) {
            throw new ServiceException("删除规则失败");
        }
        //添加操作日志
        operateLogService.addModuleOperateLog(
                String.format("删除{}-{}", cfgProcessEntity.getName(),cfgProcessEntity.getCode()),
                ModuleTypeEnum.CFG_PROCESS.getCode(),
                cfgProcessEntity.getId(),
                "删除操作"
        );
        //流程下没了规则，则删除流程
        List<CfgProcessRuleEntity> remainingRules = cfgProcessRuleService.listAllByProcessId(entity.getCfgProcessId());
        if (CollUtil.isNotEmpty(remainingRules)) {
            return BatchResultDTO.success(entity.getId(), cfgProcessEntity.getCode(), OperationTypeEnum.DELETE);
        }
        super.removeById(cfgProcessEntity.getId());
        return BatchResultDTO.success(entity.getId(), cfgProcessEntity.getCode(), OperationTypeEnum.DELETE);
    }

    @Override
    public void exportList(CfgProcessDTO.SearchParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("流程配置导出", EXPORT_PROCESS_CFG_PROCESS.getCode(), dto);
    }

    @Override
    public List<CfgProcessDTO.TabListDTO> tabList(PermissionsDTO dto) {
        CfgProcessDTO.SearchParamDTO params = new CfgProcessDTO.SearchParamDTO();
        params.setPermissionSql(dto.getPermissionSql());

        List<CfgProcessDTO.TabListDTO> list = baseMapper.tabList(params);

        // 定义需要展示的状态列表
        List<String> statusList = Arrays.asList("t", "f");

        // 获取已存在的状态
        List<String> existStatusList = list.stream()
                .map(CfgProcessDTO.TabListDTO::getTabFlag)
                .collect(Collectors.toList());

        // 补充不存在的状态并设置默认值
        statusList.forEach(status -> {
            if (!existStatusList.contains(status)) {
                list.add(new CfgProcessDTO.TabListDTO(status, "", 0));
            }
        });

        // 设置状态中文名称
        list.forEach(item -> item.setTabFlagName(Objects.equals(item.getTabFlag(), "t") ? "停用" : "启用"));

        // 计算总数并添加“全部”条目
        int totalCount = list.stream()
                .map(CfgProcessDTO.TabListDTO::getCount)
                .filter(Objects::nonNull)
                .reduce(0, Integer::sum);
        list.add(new CfgProcessDTO.TabListDTO("all", "全部", totalCount));

        // 倒序排列
        Collections.reverse(list);

        return list;
    }

    @Override
    public CfgProcessEntity getByBusinessKey(String businessKey) {
        return lambdaQuery().eq(CfgProcessEntity::getBussinessKey, businessKey).last("limit 1").one();
    }


    /**
     * 创建飞书审批实例
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public void startThirdProcess(CfgProcessDTO.StartDTO dto) {
        log.info("开始创建飞书审批实例,启动参数为:{}", dto);
        //查询approvalCode
        CfgProcessRuleEntity cfgProcessRuleEntity = cfgProcessRuleService.getById(dto.getRuleId());
        String code = cfgProcessRuleEntity.getProcessDefinitionId();

        //查询userid
        List<ThirdUnionDTO> dtoList = sysUserFeign.getThirdByUserIds("FS", Collections.singletonList(dto.getUserId()));
        if (CollUtil.isEmpty(dtoList) || StrUtil.isEmpty(dtoList.get(0).getThirdUserId())) {
            throw new ServiceException(ApiError.WF_FS_QUERY_USER_NOT_FOUND);
        }
        //
        String userId = dtoList.get(0).getThirdUserId();
        //查询字段映射表
        List<CfgProcessFieldMapEntity> fieldMapList = cfgProcessFieldMapService.list(new LambdaQueryWrapper<CfgProcessFieldMapEntity>().eq(CfgProcessFieldMapEntity::getCfgId, dto.getRuleId()).eq(CfgProcessFieldMapEntity::getIsDeleted, false));
        if (CollUtil.isEmpty(fieldMapList)) {
            throw new ServiceException(ApiError.WF_FIELD_MAP_NOT_FOUND);
        }
        List<String> fieldIds = fieldMapList.stream().map(CfgProcessFieldMapEntity::getId).collect(Collectors.toList());
        //查询值映射表
        List<CfgProcessValueMapEntity> valueMapList = cfgProcessValueMapService.list(new LambdaQueryWrapper<CfgProcessValueMapEntity>().in(CfgProcessValueMapEntity::getFieldMapId, fieldIds).eq(CfgProcessValueMapEntity::getIsDeleted, false));

        //组装form，1、实时获取 2、查询流程定义表
        ThirdProcessDefinitionEntity processDefinition = thirdProcessDefinitionService.getOne(new LambdaQueryWrapper<ThirdProcessDefinitionEntity>().eq(ThirdProcessDefinitionEntity::getStatus, ThirdProcessDefinitionStatusEnum.ACTIVE.getCode()).eq(ThirdProcessDefinitionEntity::getApprovalCode, code).eq(ThirdProcessDefinitionEntity::getIsDeleted, false));
        if (ObjectUtil.isEmpty(processDefinition)) {
            throw new ServiceException(ApiError.WF_FS_PROCESS_NOT_EXIST);
        }
        JSONArray formArray = JSONUtil.parseArray(processDefinition.getFormJson());
        //组装Json
        try {
            ProcessFormHandler handler = processFormFactory.getAssembleFormHandler(CfgProcessRuleTypeEnum.getByCode(dto.getRuleType()).name());
            JSONArray objects = handler.assembleForm(formArray, dto.getVariablesMap(), fieldMapList, valueMapList);
            String form = JSONUtil.toJsonStr(objects);
            // 提交飞书前打印组装后的 form，便于飞书报错(如 validate form error)时回查具体字段值
            log.info("提交飞书审批表单: approvalCode={}, userId={}, form={}", code, userId, form);
            CreateInstanceReq req = CreateInstanceReq.newBuilder()
                    .instanceCreate(InstanceCreate.newBuilder()
                            .approvalCode(code)
                            .userId(userId)
                            .form(form)
                            .build())
                    .build();
            String instanceCode = fsService.createInstance(req);
            //飞书明细控件 id:name
            Map<String, String> resultMap = new HashMap<>();
            for (int i = 0; i < formArray.size(); i++) {
                JSONObject obj = formArray.getJSONObject(i);
                if (CfgQueryOptionFieldTypeEnum.FIELDLIST.equals(obj.getStr(FsRequestBodyAttributesEnum.TYPE.getCode()))) {
                    String id = obj.getStr(FsRequestBodyAttributesEnum.ID.getCode());
                    String name = obj.getStr(FsRequestBodyAttributesEnum.NAME.getCode());
                    resultMap.put(id, name);
                }
            }
            // 生成三方查询记录
            List<ApproveTaskDetailDTO.AddDTO> addDTOS = handler.generatePushDetailDTO(objects, fieldMapList, dto.getVariablesMap(),resultMap);
            List<CfgQueryOptionEntity> options = cfgQueryOptionService.list(
                    new LambdaQueryWrapper<CfgQueryOptionEntity>().eq(CfgQueryOptionEntity::getBussinessKey, dto.getBusinessKey())
            );
            //fieldName
            Map<String, Object> optionMap = new HashMap<>();
            for (CfgQueryOptionEntity option : options) {
                if ("main".equals(option.getFieldBelongsType())) {
                    optionMap.put(option.getConditionField(), option.getConditionFieldName());
                } else {
                    Map<String, String> map;
                    if (optionMap.containsKey(option.getFieldBelongsType())) {
                        map = (Map<String, String>) optionMap.get(option.getFieldBelongsType());
                    } else {
                        map = new HashMap<>();
                        optionMap.put(option.getFieldBelongsType(), map);
                    }
                    map.put(option.getConditionField(), option.getConditionFieldName());
                }
            }
            for (ApproveTaskDetailDTO.AddDTO addDTO : addDTOS) {
                if (addDTO.getEntityCode()!=null){
                    Map<String, String> codeMap = (Map<String, String>) optionMap.get(addDTO.getEntityCode());
                    if (ObjectUtil.isNotEmpty(codeMap)) {
                        addDTO.setSysFieldName(codeMap.get(addDTO.getSysField()));
                    }
                    continue;
                }
                Object object = optionMap.get(addDTO.getSysField());
                addDTO.setSysFieldName(ObjectUtil.isEmpty(object) ? "" : object.toString());
            }
            //验证addDTOS
            log.info("三方查询生成明细：", JSONUtil.toJsonStr(addDTOS));
            ApproveTaskInfoDTO.AddDTO addDTO = buildApproveTaskInfo(dto, processDefinition);
            addDTO.setDetailList(addDTOS);
            addDTO.setThirdInstanceId(instanceCode);
            addDTO.setThirdApprovalCode(code);
            approveTaskInfoService.add(addDTO);
        } catch (Exception e) {
            log.error("飞书创建审批实例失败：", e);
            // 飞书原始错误只带 widget id（例：validate form error 控件值不合法或者为空。控件= widget17785774022440001。）
            // 这里按 form 定义把 widget id 翻译成"控件名 + ERP 字段"，方便运营/开发快速定位。
            // 不带 args 是为了避免 enrichedMsg 中可能出现的 `{}` 被 CharSequenceUtil.format 误当占位符。
            String enrichedMsg = enrichWidgetIdInMessage(e.getMessage(), formArray, fieldMapList);
            throw new ServiceException("飞书创建审批实例失败：" + enrichedMsg);
        }
    }

    /**
     * 把飞书报错 message 中的 widget id 替换为"widgetId(控件名,ERP 字段=xxx)"形式，
     * 便于在飞书表单只暴露 widget id 时也能快速定位是哪个 ERP 字段。
     * 当 message 中找不到 widget id 时，原样返回。
     */
    private String enrichWidgetIdInMessage(String message,
                                           JSONArray formArray,
                                           List<CfgProcessFieldMapEntity> fieldMapList) {
        if (StrUtil.isBlank(message)) {
            return "";
        }
        Pattern widgetPattern = Pattern.compile("widget[0-9]+");
        Matcher matcher = widgetPattern.matcher(message);
        if (!matcher.find()) {
            return message;
        }

        Map<String, String> widgetNameMap = collectWidgetNameMap(formArray);
        Map<String, String> widgetSysFieldMap = fieldMapList == null ? Collections.emptyMap() :
                fieldMapList.stream()
                        .filter(fm -> StrUtil.isNotBlank(fm.getThirdFieldId()))
                        .collect(Collectors.toMap(
                                CfgProcessFieldMapEntity::getThirdFieldId,
                                fm -> StrUtil.nullToEmpty(fm.getSysField()),
                                (a, b) -> a
                        ));

        matcher.reset();
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String widgetId = matcher.group();
            String widgetName = widgetNameMap.getOrDefault(widgetId, "");
            String sysField = widgetSysFieldMap.getOrDefault(widgetId, "");
            StringBuilder repl = new StringBuilder(widgetId);
            if (StrUtil.isNotBlank(widgetName) || StrUtil.isNotBlank(sysField)) {
                repl.append("(");
                if (StrUtil.isNotBlank(widgetName)) {
                    repl.append("控件=").append(widgetName);
                }
                if (StrUtil.isNotBlank(sysField)) {
                    if (StrUtil.isNotBlank(widgetName)) {
                        repl.append(",");
                    }
                    repl.append("ERP字段=").append(sysField);
                }
                repl.append(")");
            }
            matcher.appendReplacement(sb, Matcher.quoteReplacement(repl.toString()));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * 递归收集 form 中所有控件的 id->name 映射（包含 fieldList 子控件）。
     */
    private Map<String, String> collectWidgetNameMap(JSONArray formArray) {
        Map<String, String> result = new HashMap<>();
        if (formArray == null) {
            return result;
        }
        for (int i = 0; i < formArray.size(); i++) {
            collectWidgetNameRecursively(formArray.getJSONObject(i), result);
        }
        return result;
    }

    private void collectWidgetNameRecursively(JSONObject obj, Map<String, String> result) {
        if (obj == null) {
            return;
        }
        String id = obj.getStr("id");
        String name = obj.getStr("name");
        if (StrUtil.isNotBlank(id)) {
            result.put(id, StrUtil.nullToEmpty(name));
        }
        Object children = obj.get("children");
        if (children instanceof JSONArray) {
            JSONArray arr = (JSONArray) children;
            for (int j = 0; j < arr.size(); j++) {
                collectWidgetNameRecursively(arr.getJSONObject(j), result);
            }
        }
    }

    @Override
    public List<CfgProcessDTO.ProcessSelectDTO> listProcessSelect() {
        List<WorkMenuEntity> list = workMenuService.list();
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        List<CfgProcessEntity> processList = this.list();
        Map<String, String> processMap = CollUtil.isEmpty(processList) ? new HashMap<>() : processList.stream()
                .collect(Collectors.toMap(CfgProcessEntity::getBussinessKey, CfgProcessEntity::getId));

        List<CfgProcessDTO.ProcessSelectDTO> result = new ArrayList<>();
        for (WorkMenuEntity workMenuEntity : list) {
            CfgProcessDTO.ProcessSelectDTO processSelectDTO = new CfgProcessDTO.ProcessSelectDTO();
            processSelectDTO.setCode(workMenuEntity.getModuleCode());
            processSelectDTO.setName(workMenuEntity.getModuleClassify());
            String id = processMap.get(workMenuEntity.getModuleCode());
            if (StrUtil.isBlank(id)) {
                processSelectDTO.setDisabled(Boolean.FALSE);
            } else {
                processSelectDTO.setDisabled(Boolean.TRUE);
            }
            result.add(processSelectDTO);
        }
        return result;

    }

    private ApproveTaskInfoDTO.AddDTO buildApproveTaskInfo(CfgProcessDTO.StartDTO dto, ThirdProcessDefinitionEntity processDefinition) {
        ApproveTaskInfoDTO.AddDTO addDTO = new ApproveTaskInfoDTO.AddDTO();
        addDTO.setSourcePlatform(processDefinition.getSourcePlatform());
        addDTO.setType(processDefinition.getType());
        addDTO.setThirdDefinniationName(processDefinition.getName());
        addDTO.setBussinessKey(dto.getBusinessKey());
        addDTO.setBussinessCode(dto.getBusinessCode());
        addDTO.setBussinessId(dto.getBusinessId());
        addDTO.setHappenTime(LocalDateTime.now());
        addDTO.setBussinessApproveStatus(ApproveStatusEnum.APPROVE_ING.getCode());
        addDTO.setStatus(ApproveTaskStatusEnum.SUCCESS.getCode());
        return addDTO;
    }
}
