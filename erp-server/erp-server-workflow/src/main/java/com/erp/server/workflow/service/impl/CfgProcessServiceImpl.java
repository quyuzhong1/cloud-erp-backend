package com.erp.server.workflow.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.ProcessFormEvent;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.workflow.dto.CfgProcessDTO;
import com.erp.model.workflow.entity.*;
import com.erp.model.workflow.enums.CfgProcessRuleTypeEnum;
import com.erp.model.workflow.enums.ThirdProcessDefinitionStatusEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.sys.feign.SysUserThirdFeign;
import com.erp.sdk.fs.service.FsService;
import com.erp.server.workflow.context.FsProcessFormFactory;
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

import static com.common.business.enums.FileTaskEventEnum.EXPORT_CFG_PROCESS;

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
    private FsService fsService;

    @Resource
    private SysUserThirdFeign sysUserThirdFeign;

    @Resource
    private ThirdProcessDefinitionService thirdProcessDefinitionService;

    @Resource
    private CfgProcessFieldMapService cfgProcessFieldMapService;

    @Resource
    private CfgProcessValueMapService cfgProcessValueMapService;

    @Resource
    private FsProcessFormFactory fsProcessFormFactory;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(@RequestBody @Validated CfgProcessDTO.AddOrUpdateDTO dto) {
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
        CfgProcessEntity old = this.getById(dto.getId());
        CfgProcessEntity cfgProcessEntity = new CfgProcessEntity();
        BeanMapperUtils.copy(dto, cfgProcessEntity);
        //code不能修改
        cfgProcessEntity.setCode(old.getCode());
        log.info("开始新增流程配置");
        boolean save = super.saveOrUpdate(cfgProcessEntity);
        if (!save) {
            throw new ServiceException("流程配置保存失败");
        }
        //保存执行条件
        cfgProcessRuleService.update(cfgProcessEntity.getBussinessKey(), cfgProcessEntity.getId(), dto.getProcessRuleDTOList());
        // 操作日志
        operateLogService.addModuleOperateLogByObj(old, cfgProcessEntity, ModuleTypeEnum.CFG_PROCESS.getCode(), cfgProcessEntity.getId(), "更新操作");
        return new BaseResultDTO.AddDTO(cfgProcessEntity.getId(), dto.getCode());
    }

    @Override
    public PagingVO<CfgProcessDTO.ProcessViewDTO> paging(@RequestBody @Validated PagingDTO<CfgProcessDTO.SearchParamDTO> dto) {
        CfgProcessDTO.SearchParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<CfgProcessDTO.ProcessDTO> pageData = baseMapper.getProcessWithRulesAndExps(query, params);
        //处理processResultMap
        List<CfgProcessDTO.ProcessViewDTO> viewDTOList = pageData.getRecords().stream()
                .flatMap(processDTO -> processDTO.getRuleList().stream().map(rule -> {
                    CfgProcessDTO.ProcessViewDTO viewDTO = BeanUtil.copyProperties(rule, CfgProcessDTO.ProcessViewDTO.class);
                    // 从 ProcessDTO 中获取字段
                    viewDTO.setId(processDTO.getId());
                    viewDTO.setCode(processDTO.getCode());
                    viewDTO.setName(processDTO.getName());
                    viewDTO.setBussinessKey(SourceTypeEnum.getName(processDTO.getBussinessKey()));
                    // 从 ProcessRuleDTO 中获取字段
                    viewDTO.setType(CfgProcessRuleTypeEnum.getName(rule.getType()));
                    viewDTO.setRuleId(rule.getId());
                    // 拼接 expList 中的 expDesc 按 index 排序
                    String ruleDesc = rule.getExpList().stream()
                            .sorted(Comparator.comparing(CfgProcessDTO.ProcessExpDTO::getIndex))
                            .map(CfgProcessDTO.ProcessExpDTO::getExpDesc)
                            .collect(Collectors.joining(" "));
                    viewDTO.setRuleDesc(ruleDesc);

                    return viewDTO;
                }))
                .collect(Collectors.toList());
        //创建一个IpageData 并设置数据listDTOs
        IPage<CfgProcessDTO.ProcessViewDTO> newPageData = new Page<>(dto.getCurrPage(), dto.getPageSize());
        newPageData.setRecords(viewDTOList);
        return new PagingVO(newPageData);
    }

    @Override
    public CfgProcessDTO.ViewDTO view(String settingId) {
        CfgProcessDTO.ViewDTO viewDTO = baseMapper.getViewDTOById(settingId);
        if (ObjectUtil.isEmpty(viewDTO)) {
            throw new ServiceException("此流程配置不存在！,id{}", settingId);
        }

        return viewDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<String> ids) {
        cfgProcessRuleService.delete(ids);
    }

    @Override
    public void exportList(PagingDTO<CfgProcessDTO.SearchParamDTO> dto) {
        downloadTaskFeign.saveDownloadTask("流程配置导出", EXPORT_CFG_PROCESS.getCode(), dto);
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
        list.forEach(item -> item.setTabFlagName(Objects.equals(item.getTabFlag(), "f") ? "停用" : "启用"));

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
        return null;
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(CfgProcessEntity cfgProcessEntity) {
        // TODO 验证数据 & 数据赋值
    }

    /**
     * 创建飞书审批实例
     */
    @Override
    public void startThirdProcess(CfgProcessDTO.StartDTO dto) {
        //查询approvalCode
        CfgProcessRuleEntity cfgProcessRuleEntity = cfgProcessRuleService.getById(dto.getBusinessId());
//        String code = cfgProcessRuleEntity.getProcessDefinitionId();
        //
        //查询userid
//        String userId = sysUserThirdFeign.findByUserId(dto.getUserId()).getThirdUserId();
        //查询字段映射表
        List<CfgProcessFieldMapEntity> fieldMapList = cfgProcessFieldMapService.list(new LambdaQueryWrapper<CfgProcessFieldMapEntity>().eq(CfgProcessFieldMapEntity::getCfgId, dto.getBusinessId()).eq(CfgProcessFieldMapEntity::getIsDeleted, false));
        List<String> fieldIds = fieldMapList.stream().map(CfgProcessFieldMapEntity::getId).collect(Collectors.toList());
        //查询值映射表
        List<CfgProcessValueMapEntity> valueMapList = cfgProcessValueMapService.list(new LambdaQueryWrapper<CfgProcessValueMapEntity>().in(CfgProcessValueMapEntity::getFieldMapId, fieldIds).eq(CfgProcessValueMapEntity::getIsDeleted, false));
        //组装form，1、实时获取 2、查询流程定义表
        ThirdProcessDefinitionEntity body = thirdProcessDefinitionService.getOne(new LambdaQueryWrapper<ThirdProcessDefinitionEntity>().eq(ThirdProcessDefinitionEntity::getStatus, ThirdProcessDefinitionStatusEnum.ACTIVE.getCode()).eq(ThirdProcessDefinitionEntity::getApprovalCode, "7DCF7A99-6E25-4A24-8386-5E2639712983").eq(ThirdProcessDefinitionEntity::getIsDeleted, false));
        JSONArray formArray = JSONUtil.parseArray(body.getFormJson());
        //组装Json
        ProcessFormHandler handler = fsProcessFormFactory.getFileHandler(ProcessFormEvent.FS_PROCESS_FORM.getCode());
        formArray = handler.assemble(formArray, dto.getVariablesMap(),fieldMapList, valueMapList);
        // 创建请求对象（创建样式）
//        CreateInstanceReq req = CreateInstanceReq.newBuilder()
//                .instanceCreate(InstanceCreate.newBuilder()
//                        .approvalCode(approvalCode)
//                        .userId(userId)
//                        .form("[{\"id\":\"111\",\"type\":\"input\",\"value\":\"11111\"},{\"id\":\"222\",\"required\":true,\"type\":\"dateInterval\",\"value\":{\"end\":\"2019-10-02T08:12:01+08:00\",\"interval\":2,\"start\":\"2019-10-01T08:12:01+08:00\"}},{\"id\":\"333\",\"type\":\"radioV2\",\"value\":\"1\"},{\"id\":\"444\",\"type\":\"number\",\"value\":\"4\"},{\"id\":\"555\",\"type\":\"textarea\",\"value\":\"fsafs\"}]")
//                        .build())
//                .build();
        try {
//            fsService.createInstance(req);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
