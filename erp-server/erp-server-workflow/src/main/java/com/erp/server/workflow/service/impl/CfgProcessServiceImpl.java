package com.erp.server.workflow.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.workflow.dto.CfgProcessRuleDTO;
import com.erp.model.workflow.entity.CfgProcessEntity;
import com.erp.model.workflow.enums.CfgProcessBussinessKeyEnum;
import com.erp.model.workflow.enums.CfgProcessRuleTypeEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.workflow.mapper.CfgProcessMapper;
import com.erp.server.workflow.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
//import com.erp.server.workflow.service.OperateLogService;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.workflow.dto.CfgProcessDTO;

import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;

import javax.annotation.Resource;

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
    //    @Autowired
    //    private OperateLogService operateLogService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;

    @Resource
    private CfgProcessRuleService cfgProcessRuleService;

    @Resource
    private DownloadTaskFeign downloadTaskFeign;


    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO addOrUpdate(CfgProcessDTO.AddOrUpdateDTO addDTO) {
        CfgProcessEntity cfgProcessEntity = new CfgProcessEntity();
        BeanMapperUtils.copy(addDTO, cfgProcessEntity);
        //TODO 数据处理，处理映射
        handleData(cfgProcessEntity);
        log.info("开始新增流程配置");
        String code = "";
        if (StrUtil.isNotEmpty(addDTO.getId())) {
            code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_LCPZ);
            // 生成单号
            cfgProcessEntity.setCode(code);
        }
        boolean save = super.saveOrUpdate(cfgProcessEntity);
        if (!save) {
            throw new ServiceException("流程配置保存失败");
        }
        try{
            //保存执行条件
            cfgProcessRuleService.addOrUpdate(cfgProcessEntity.getBussinessKey(),cfgProcessEntity.getId(),addDTO.getProcessRuleDTOList());
        }catch (Exception e){
            throw new ServiceException(e.getMessage());
        }
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "流程配置", cfgProcessEntity.getCode());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        // TODO operateLogService.addModuleOperateLog(msg, null, cfgProcessEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）
        return new BaseResultDTO.AddDTO(cfgProcessEntity.getId(), code);
    }

    @Override
    public PagingVO<CfgProcessDTO.ProcessViewDTO> paging(PagingDTO<CfgProcessDTO.SearchParamDTO> dto) {
        CfgProcessDTO.SearchParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        IPage<CfgProcessDTO.ProcessDTO> pageData = baseMapper.getProcessWithRulesAndExps(params);
        //处理processResultMap
        List<CfgProcessDTO.ProcessViewDTO> viewDTOList = pageData.getRecords().stream()
                .flatMap(processDTO -> processDTO.getRuleList().stream().map(rule -> {
                    CfgProcessDTO.ProcessViewDTO viewDTO = BeanUtil.copyProperties(rule, CfgProcessDTO.ProcessViewDTO.class);
                    // 从 ProcessDTO 中获取字段
                    viewDTO.setId(processDTO.getId());
                    viewDTO.setCode(processDTO.getCode());
                    viewDTO.setName(processDTO.getName());
                    viewDTO.setBussinessKey(CfgProcessBussinessKeyEnum.getName(processDTO.getBussinessKey()));
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
    public CfgProcessDTO.ProcessDTO view(String settingId) {
        CfgProcessDTO.ProcessDTO processDTO = baseMapper.getProcessById(settingId);
        if (ObjectUtil.isEmpty(processDTO)) {
            throw new ServiceException("此流程配置不存在！,id{}", settingId);
        }
        //迭代器遍历processDTO.getRuleList()
        for (CfgProcessDTO.ProcessRuleDTO rule : processDTO.getRuleList()) {
            // 拼接 expList 中的 expDesc 按 index 排序
            String ruleDesc = rule.getExpList().stream()
                    .sorted(Comparator.comparing(CfgProcessDTO.ProcessExpDTO::getIndex))
                    .map(CfgProcessDTO.ProcessExpDTO::getExpDesc)
                    .collect(Collectors.joining(" "));
            rule.setRuleDesc(ruleDesc);
            // 从 ProcessRuleDTO 中获取字段
            rule.setType(CfgProcessRuleTypeEnum.getName(rule.getType()));
        }
        // TODO type:fsProcess、sysProcess的,processDefinitionName映射的列表不一样
        processDTO.setBussinessKey(CfgProcessBussinessKeyEnum.getName(processDTO.getBussinessKey()));
        return processDTO;
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
    public BaseResultDTO.UpdateDTO updateDefault(CfgProcessDTO.AddOrUpdateDTO dto) {
        return null;
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(CfgProcessEntity cfgProcessEntity) {
        // TODO 验证数据 & 数据赋值
    }
}
