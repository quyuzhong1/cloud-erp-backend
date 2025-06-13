package com.erp.server.workflow.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.vo.ThirdUnionDTO;
import com.erp.model.workflow.dto.*;
import com.erp.model.workflow.entity.*;
import com.erp.model.workflow.enums.CfgProcessRuleTypeEnum;
import com.erp.model.workflow.enums.CfgQueryOptionBussinessKeyEnum;
import com.erp.model.workflow.enums.CfgQueryOptionFieldTypeEnum;
import com.erp.model.workflow.enums.ThirdProcessDefinitionStatusEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.sdk.fs.service.FsService;
import com.erp.server.workflow.context.ProcessFormFactory;
import com.erp.server.workflow.handler.FsProcessFormHandler;
import com.erp.server.workflow.handler.ProcessFormHandler;
import com.erp.server.workflow.mapper.CfgProcessMapper;
import com.erp.server.workflow.service.*;
import com.lark.oapi.service.approval.v4.model.CreateInstanceReq;
import com.lark.oapi.service.approval.v4.model.InstanceCreate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import java.util.*;
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
    private FsService  fsService;

    @Resource
    private ApproveTaskInfoService  approveTaskInfoService;

    @Resource
    private SysUserFeign sysUserFeign;



    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(@RequestBody @Validated CfgProcessDTO.AddOrUpdateDTO dto) {
        //
        List<CfgProcessEntity> list = this.list(new LambdaQueryWrapper<CfgProcessEntity>().eq(CfgProcessEntity::getBussinessKey, dto.getBussinessKey()).eq(CfgProcessEntity::getIsDeleted, false));
        if (CollUtil.isNotEmpty(list) && list.size()>0){
            throw new ServiceException("{}已配置流程，不可重复配置", CfgQueryOptionBussinessKeyEnum.getByCode(dto.getBussinessKey())!=null ?  CfgQueryOptionBussinessKeyEnum.getByCode(dto.getBussinessKey()).getName():dto.getBussinessKey());
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
        String msg = StrUtil.format("新增【{}】配置编码为【{}】", "流程配置", cfgProcessEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CFG_PROCESS.getCode(), cfgProcessEntity.getId(), "新增操作");
        return new BaseResultDTO.AddDTO(cfgProcessEntity.getId(), code);
    }

    @Override
    public BaseResultDTO.AddDTO update(@RequestBody @Validated CfgProcessDTO.AddOrUpdateDTO dto) {
        if (dto.getId() == null){
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "流程配置id不能为空");
        }
        CfgProcessEntity old = this.getById(dto.getId());
        CfgProcessEntity cfgProcessEntity = new CfgProcessEntity();
        BeanMapperUtils.copy(dto, cfgProcessEntity);
        //code不能修改
        cfgProcessEntity.setCode(old.getCode());
        log.info("开始新增流程配置");
        boolean save = super.updateById(cfgProcessEntity);
        if (!save) {
            throw new ServiceException("流程配置保存失败");
        }
        //保存执行条件
        cfgProcessRuleService.update(cfgProcessEntity.getBussinessKey(), cfgProcessEntity.getId(), dto.getProcessRuleDTOList());
        // 操作日志
        operateLogService.addModuleOperateLogByObj(old, cfgProcessEntity, ModuleTypeEnum.CFG_PROCESS.getCode(), cfgProcessEntity.getId(), "编辑信息");
        return new BaseResultDTO.AddDTO(cfgProcessEntity.getId(), dto.getCode());
    }

    @Override
    public PagingVO<CfgProcessDTO.ProcessViewDTO> paging(@RequestBody @Validated PagingDTO<CfgProcessDTO.SearchParamDTO> dto) {
        CfgProcessDTO.SearchParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<CfgProcessDTO.ProcessViewDTO> pageData = baseMapper.getProcessWithRuleAndAggregatedExps(query, params);
        return new PagingVO(pageData);
    }

    @Override
    public CfgProcessDTO.ViewDTO view(String settingId) {
        CfgProcessDTO.ViewDTO viewDTO = baseMapper.getViewDTOById(settingId);

        for (CfgProcessRuleDTO.ViewDTO ruleDto : viewDTO.getProcessRuleDTOList()) {
            if (ObjectUtil.isEmpty(ruleDto.getProcessFieldMapDTOList())) {
                continue;
            }
            for (CfgProcessFieldMapDTO.ViewDTO fieldMapDto : ruleDto.getProcessFieldMapDTOList()) {
                if (StrUtil.isBlank(fieldMapDto.getThirdFieldType())) {
                    continue;
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
    public void delete(List<String> ids) {
        if (ObjectUtil.isEmpty(ids)) {
            return;
        }

        // 获取规则列表
        List<CfgProcessRuleEntity> processRuleEntityList = cfgProcessRuleService.list(
                new LambdaQueryWrapper<CfgProcessRuleEntity>()
                        .in(CfgProcessRuleEntity::getId, ids)
                        .eq(CfgProcessRuleEntity::getIsDeleted, false)
        );

        if (ObjectUtil.isEmpty(processRuleEntityList)) {
            return;
        }

        // 删除规则
        if (!cfgProcessRuleService.delete(ids)) {
            throw new ServiceException("删除规则失败");
        }

        // 获取需要删除的流程配置ID
        List<String> processIds = processRuleEntityList.stream()
                .map(CfgProcessRuleEntity::getCfgProcessId)
                .distinct()
                .collect(Collectors.toList());

        // 检查哪些流程配置没有规则了
        List<CfgProcessRuleEntity> remainingRules = cfgProcessRuleService.list(
                new LambdaQueryWrapper<CfgProcessRuleEntity>()
                        .in(CfgProcessRuleEntity::getCfgProcessId, processIds)
                        .eq(CfgProcessRuleEntity::getIsDeleted, false)
        );

        Set<String> remainingProcessIds = remainingRules.stream()
                .map(CfgProcessRuleEntity::getCfgProcessId)
                .collect(Collectors.toSet());

        List<String> processesToDelete = processIds.stream()
                .filter(id -> !remainingProcessIds.contains(id))
                .collect(Collectors.toList());

        if (!processesToDelete.isEmpty()) {
            super.removeByIds(processesToDelete);
            // 添加操作日志
            operateLogService.addModuleOperateLog(
                    String.format("删除流程配置，ID：%s", String.join(",", processesToDelete)),
                    ModuleTypeEnum.CFG_PROCESS.getCode(),
                    null,
                    "删除操作"
            );
        }
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
        return lambdaQuery().eq(CfgProcessEntity::getBussinessKey,businessKey).last("limit 1").one();
    }


    /**
     * 创建飞书审批实例
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startThirdProcess(CfgProcessDTO.StartDTO dto) {
        log.info("开始创建飞书审批实例,启动参数为:{}", dto);
        //查询approvalCode
        CfgProcessRuleEntity cfgProcessRuleEntity = cfgProcessRuleService.getById(dto.getRuleId());
        String code = cfgProcessRuleEntity.getProcessDefinitionId();
        //
        //查询userid
        List<ThirdUnionDTO> dtoList = sysUserFeign.getThirdByUserIds("FS", Collections.singletonList(dto.getUserId()));
        if (CollUtil.isEmpty(dtoList) || StrUtil.isEmpty(dtoList.get(0).getThirdUserId())){
            throw new ServiceException(ApiError.FS_FOUNDER_NOT_EXIST.msg,  "FS用户不存在");
        }
        String userId = dtoList.get(0).getThirdUserId();
        //查询字段映射表
        List<CfgProcessFieldMapEntity> fieldMapList = cfgProcessFieldMapService.list(new LambdaQueryWrapper<CfgProcessFieldMapEntity>().eq(CfgProcessFieldMapEntity::getCfgId, dto.getRuleId()).eq(CfgProcessFieldMapEntity::getIsDeleted, false));
        if (CollUtil.isEmpty(fieldMapList)) {
            throw new ServiceException(ApiError.CFG_PROCESS_FIELD_MAP_NOT_EXIST);
        }
        List<String> fieldIds = fieldMapList.stream().map(CfgProcessFieldMapEntity::getId).collect(Collectors.toList());
        //查询值映射表
        List<CfgProcessValueMapEntity> valueMapList = cfgProcessValueMapService.list(new LambdaQueryWrapper<CfgProcessValueMapEntity>().in(CfgProcessValueMapEntity::getFieldMapId, fieldIds).eq(CfgProcessValueMapEntity::getIsDeleted, false));
        if (CollUtil.isEmpty(valueMapList)){
            throw new ServiceException(ApiError.CFG_PROCESS_FIELD_MAP_NOT_EXIST);
        }
        //组装form，1、实时获取 2、查询流程定义表
        ThirdProcessDefinitionEntity body = thirdProcessDefinitionService.getOne(new LambdaQueryWrapper<ThirdProcessDefinitionEntity>().eq(ThirdProcessDefinitionEntity::getStatus, ThirdProcessDefinitionStatusEnum.ACTIVE.getCode()).eq(ThirdProcessDefinitionEntity::getApprovalCode, code).eq(ThirdProcessDefinitionEntity::getIsDeleted, false));
        if (ObjectUtil.isEmpty(body)) {
            throw new ServiceException(ApiError.FS_PROCESS_DEFINITION_NOT_EXIST);
        }
        JSONArray formArray = JSONUtil.parseArray(body.getFormJson());
        //组装Json
        try {
        ProcessFormHandler handler = processFormFactory.getAssembleFormHandler(CfgProcessRuleTypeEnum.getByCode(dto.getRuleType()).name());
        JSONArray objects = handler.assembleForm(formArray, dto.getVariablesMap(), fieldMapList, valueMapList);
        List<ApproveTaskDetailDTO.AddDTO> addDTOS = handler.generatePushDetailDTO(objects, fieldMapList, dto.getVariablesMap());
        //验证addDTOS
        log.info("三方查询生成明细：",JSONUtil.toJsonStr(addDTOS));
        //插入记录
        ApproveTaskInfoDTO.AddDTO addDTO = new ApproveTaskInfoDTO.AddDTO();
        addDTO.setDetailList(addDTOS);

        String form = JSONUtil.toJsonStr(objects);
        CreateInstanceReq req = CreateInstanceReq.newBuilder()
                .instanceCreate(InstanceCreate.newBuilder()
                        .approvalCode(code)
                        .userId(userId)
                        .form(form)
                        .build())
                .build();

            String instanceCode = fsService.createInstance(req);
            //生成三方查询记录
            addDTO.setThirdInstanceId(instanceCode);
            addDTO.setBussinessKey(dto.getBusinessKey());
            addDTO.setBussinessCode(dto.getBusinessCode());
            addDTO.setBussinessId(dto.getBusinessId());
            approveTaskInfoService.add(addDTO);
        } catch (Exception e) {
            throw new RuntimeException("飞书创建审批实例失败："+e);
        }
    }
}
