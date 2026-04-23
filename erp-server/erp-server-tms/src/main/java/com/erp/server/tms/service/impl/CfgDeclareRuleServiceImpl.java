package com.erp.server.tms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.common.business.dto.ApproveDTO;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.vo.LoginUser;

import cn.hutool.core.util.StrUtil;
import com.common.core.controller.vo.ApiResult;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.tms.entity.CfgDeclareRuleEntity;
import com.erp.server.tms.mapper.CfgDeclareRuleMapper;
import com.erp.server.tms.service.CfgDeclareRuleService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.tms.service.OperateLogService;
import com.common.core.exception.ServiceException;
import cn.hutool.core.util.ObjectUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.CfgDeclareRuleDTO;
import com.erp.server.tms.service.CfgDeclareRuleConditionService;
import com.erp.server.tms.service.CommonService;
import com.erp.model.tms.entity.CfgDeclareRuleConditionEntity;
import com.erp.model.tms.dto.CfgDeclareRuleConditionDTO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import java.util.stream.Collectors;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Sets;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import com.erp.model.sys.dto.SysCodeDTO;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.model.sys.dto.SysAccountingCompanyDTO;
import com.erp.model.tms.enums.CfgDeclareRuleReceiverTypeEnum;
import com.erp.model.tms.enums.CfgDeclareRuleSenderTypeEnum;
import com.erp.rpc.sys.feign.SysFeign;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 报关规则主表 服务实现类
 * </p>
 *
 * @author jack
 * @since 2026-04-20
 */
@Slf4j
@Service
public class CfgDeclareRuleServiceImpl extends SuperServiceImpl<CfgDeclareRuleMapper, CfgDeclareRuleEntity> implements CfgDeclareRuleService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private CfgDeclareRuleConditionService cfgDeclareRuleConditionService;
    @Resource
    private CommonService commonService;
    @Resource
    private SysFeign sysFeign;

    private static final String SENDER = "sender";
    private static final String RECEIVER = "receiver";

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgDeclareRuleDTO.AddDTO addDTO) {
        CfgDeclareRuleEntity cfgDeclareRuleEntity = new CfgDeclareRuleEntity();
        BeanMapperUtils.copy(addDTO, cfgDeclareRuleEntity);

        // 数据处理
        handleData(cfgDeclareRuleEntity);

        log.info("开始新增报关规则主单");
        boolean save = super.save(cfgDeclareRuleEntity);
        if(!save) {
            throw new ServiceException("报关规则主单保存失败");
        }

        // 处理明细
        if (CollUtil.isNotEmpty(addDTO.getDetailList())) {
            List<CfgDeclareRuleConditionEntity> conditions = addDTO.getDetailList().stream().map(d -> {
                CfgDeclareRuleConditionEntity entity = BeanMapperUtils.map(CfgDeclareRuleConditionEntity.class, d);
                entity.setRuleId(cfgDeclareRuleEntity.getId());
                return entity;
            }).collect(Collectors.toList());
            // 自动设置 index
            for (int i = 0; i < conditions.size(); i++) {
                conditions.get(i).setIndex(i);
            }
            cfgDeclareRuleConditionService.saveBatch(conditions);
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增报关规则，ID为【{}】", UserContext.getDefaultLoginUser().getUserName(), cfgDeclareRuleEntity.getId());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CFG_DECLARE_RULE.getCode(), cfgDeclareRuleEntity.getId(), "新增操作");

        return new BaseResultDTO.AddDTO(cfgDeclareRuleEntity.getId(), cfgDeclareRuleEntity.getId());
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgDeclareRuleDTO.UpdateDTO addOrUpdateDTO) {
        CfgDeclareRuleEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "报关规则主单"));
        CfgDeclareRuleEntity cfgDeclareRuleEntity =  BeanMapperUtils.map(CfgDeclareRuleEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(cfgDeclareRuleEntity);
        log.info("编辑 开始修改报关规则主单数据，id：【{}】", old.getId());
        boolean save = super.updateById(cfgDeclareRuleEntity);
        if(!save) {
            throw new ServiceException("报关规则主单保存失败");
        }

        // 处理明细差分更新
        List<CfgDeclareRuleConditionEntity> oldDetails = cfgDeclareRuleConditionService.lambdaQuery()
                .eq(CfgDeclareRuleConditionEntity::getRuleId, cfgDeclareRuleEntity.getId())
                .list();
        List<CfgDeclareRuleConditionEntity> newDetails = Optional.ofNullable(addOrUpdateDTO.getDetailList())
                .orElse(Collections.emptyList()).stream().map(d -> {
                    CfgDeclareRuleConditionEntity entity = BeanMapperUtils.map(CfgDeclareRuleConditionEntity.class, d);
                    entity.setRuleId(cfgDeclareRuleEntity.getId());
                    return entity;
                }).collect(Collectors.toList());
        // 自动计算并设置 index
        for (int i = 0; i < newDetails.size(); i++) {
            newDetails.get(i).setIndex(i);
        }
        commonService.updateDetail(cfgDeclareRuleEntity.getId(), ModuleTypeEnum.CFG_DECLARE_RULE.getCode(), 
                cfgDeclareRuleConditionService, newDetails, oldDetails,Collections.singletonList("id"));

        // 记录主单操作日志
        log.info("编辑 开始记录报关规则主单日志数据，id：【{}】", cfgDeclareRuleEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑报关规则，ID为【{}】", UserContext.getDefaultLoginUser().getUserName(), cfgDeclareRuleEntity.getId());
        operateLogService.addModuleOperateLogByObj(old, cfgDeclareRuleEntity, ModuleTypeEnum.CFG_DECLARE_RULE.getCode(), cfgDeclareRuleEntity.getId(), msg);
        return Boolean.TRUE;
    }


    @Override
    public PagingVO<CfgDeclareRuleDTO.ListDTO> paging(PagingDTO<CfgDeclareRuleDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<CfgDeclareRuleDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<CfgDeclareRuleDTO.TabListDTO> tabList(PermissionsDTO param) {
        CfgDeclareRuleDTO.PagingParamDTO searchParam = new CfgDeclareRuleDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<CfgDeclareRuleDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        // 获取状态列表
        // TODO 替换当前表Tab状态字段
        List<String> statusList = null;
        // 不存在的状态赋值为0
        List<String> existStatusList = list.stream().map(CfgDeclareRuleDTO.TabListDTO::getTabFlag).collect(Collectors.toList());
        statusList.parallelStream().forEach(status -> {
            if(!existStatusList.contains(status)) {
            list.add(new CfgDeclareRuleDTO.TabListDTO(status, 0));
        }
        });
        list.add(new CfgDeclareRuleDTO.TabListDTO("all", list.stream().mapToInt(CfgDeclareRuleDTO.TabListDTO::getCount).sum()));
        // 计算合计数量
        return list;
    }

    @Override
    public void exportList(CfgDeclareRuleDTO.ExportDTO param, HttpServletResponse response) {
        List<CfgDeclareRuleDTO.ListDTO> list = this.baseMapper.listExport(param);
        if(CollUtil.isEmpty(list)) {
           return;
        }
        // 数据处理
        fillList(list);

        // 导出数据
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/cfgDeclareRule.xlsx";
        String name = "报关规则主单导出";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date).append(name);
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
        } catch (Exception e) {
            throw new ServiceException(ApiError.FILE_EXPORT_FAILED);
        }
    }
    /**
    * 新增修改处理数据
    */
    private void handleData(CfgDeclareRuleEntity cfgDeclareRuleEntity) {
        // 校验唯一性
        if (ObjectUtil.isAllNotEmpty(cfgDeclareRuleEntity.getRuleType(), cfgDeclareRuleEntity.getSenderId(), cfgDeclareRuleEntity.getReceiverId())) {
            CfgDeclareRuleEntity exist = this.lambdaQuery()
                    .eq(CfgDeclareRuleEntity::getRuleType, cfgDeclareRuleEntity.getRuleType())
                    .eq(CfgDeclareRuleEntity::getSenderId, cfgDeclareRuleEntity.getSenderId())
                    .eq(CfgDeclareRuleEntity::getReceiverId, cfgDeclareRuleEntity.getReceiverId())
                    .ne(ObjectUtil.isNotEmpty(cfgDeclareRuleEntity.getId()), CfgDeclareRuleEntity::getId, cfgDeclareRuleEntity.getId())
                    .last("LIMIT 1")
                    .one();
            if (exist != null) {
                throw new ServiceException(ApiError.BILL_ALREADY_EXIST, "相同规则类型、发货人及收货人的报关规则");
            }
        }

        // 填充结算公司名称
        Set<String> companyIds = Sets.newHashSet();
        if (StrUtil.isNotEmpty(cfgDeclareRuleEntity.getSenderId()) && CfgDeclareRuleSenderTypeEnum.BY_COMPANY.getCode().equals(cfgDeclareRuleEntity.getSenderType())) {
            companyIds.add(cfgDeclareRuleEntity.getSenderId());
        }
        if (StrUtil.isNotEmpty(cfgDeclareRuleEntity.getReceiverId()) && CfgDeclareRuleReceiverTypeEnum.BY_COMPANY.getCode().equals(cfgDeclareRuleEntity.getReceiverType())) {
            companyIds.add(cfgDeclareRuleEntity.getReceiverId());
        }

        if (CollUtil.isNotEmpty(companyIds)) {
            ApiResult<List<SysAccountingCompanyDTO.ListDTO>> companyResult = sysFeign.companyList("");
            if (companyResult != null && companyResult.isSuccess() && CollUtil.isNotEmpty(companyResult.getData())) {
                Map<String, String> companyMap = companyResult.getData().stream()
                        .filter(c -> companyIds.contains(c.getId()))
                        .collect(Collectors.toMap(SysAccountingCompanyDTO.ListDTO::getId, SysAccountingCompanyDTO.ListDTO::getCompanyName, (k1, k2) -> k1));

                if (companyMap.containsKey(cfgDeclareRuleEntity.getSenderId())) {
                    cfgDeclareRuleEntity.setSenderName(companyMap.get(cfgDeclareRuleEntity.getSenderId()));
                }
                if (companyMap.containsKey(cfgDeclareRuleEntity.getReceiverId())) {
                    cfgDeclareRuleEntity.setReceiverName(companyMap.get(cfgDeclareRuleEntity.getReceiverId()));
                }
            }
        }
    }

    @Override
    public CfgDeclareRuleDTO.ViewDTO view(String id) {
        CfgDeclareRuleEntity cfgDeclareRuleEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到报关规则数据"));
        CfgDeclareRuleDTO.ViewDTO data = BeanMapperUtils.map(CfgDeclareRuleDTO.ViewDTO.class, cfgDeclareRuleEntity);
        // 数据填充处理
        fillOne(data);
        // 查询明细数据
        List<CfgDeclareRuleConditionEntity> conditions = cfgDeclareRuleConditionService.lambdaQuery()
                .eq(CfgDeclareRuleConditionEntity::getRuleId, id)
                .orderByAsc(CfgDeclareRuleConditionEntity::getIndex)
                .list();
        if (CollUtil.isNotEmpty(conditions)) {
            data.setDetailList(BeanMapperUtils.copyList(CfgDeclareRuleConditionDTO.ListDTO.class,conditions));
        }
        return data;
    }

    private void fillOne(CfgDeclareRuleDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
          return;
        }
    }

   /**
    * 分页查询、导出 数据处理
   */
   private void fillList(List<CfgDeclareRuleDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
            return;
        }
        // 属性赋值
        for(CfgDeclareRuleDTO.ListDTO data : list) {
        // TODO 其他如需要显示名称的字段赋值
        }
   }

    @Override
    public List<BaseDropDownDTO.Tree> dropDownList(String type, String name) {
        if (SENDER.equals(type)) {
            return Collections.singletonList(buildDropDown(
                    type,
                    CfgDeclareRuleSenderTypeEnum.BY_COMPANY.getCode(),
                    CfgDeclareRuleSenderTypeEnum.BY_COMPANY.getName(),
                    accountingCompanyChildList(name)));
        }

        if (RECEIVER.equals(type)) {
            List<BaseDropDownDTO.Tree> result = new ArrayList<>(2);
            result.add(buildDropDown(
                    type,
                    CfgDeclareRuleReceiverTypeEnum.BY_COMPANY.getCode(),
                    CfgDeclareRuleReceiverTypeEnum.BY_COMPANY.getName(),
                    accountingCompanyChildList(name)));
            result.add(buildDropDown(
                    type,
                    CfgDeclareRuleReceiverTypeEnum.BY_CUSTOMER.getCode(),
                    CfgDeclareRuleReceiverTypeEnum.BY_CUSTOMER.getName(),
                    Collections.emptyList()));
            return result;
        }

        throw new ServiceException("类型参数错误，type必须为sender或receiver");
    }

    /**
     * 构建下拉树形结构
     */
    private BaseDropDownDTO.Tree buildDropDown(String type,
                                               String code,
                                               String value,
                                               List<BaseDropDownDTO.ChildTree> childTreeList) {
        BaseDropDownDTO.Tree tree = new BaseDropDownDTO.Tree();
        tree.setType(type);
        tree.setCode(code);
        tree.setValue(value);
        tree.setDisabled(Boolean.FALSE);
        tree.setChildTreeList(childTreeList);
        return tree;
    }

    /**
     * 获取会计公司子列表
     */
    private List<BaseDropDownDTO.ChildTree> accountingCompanyChildList(String name) {
        ApiResult<List<SysAccountingCompanyDTO.ListDTO>> companyResult = sysFeign.companyList(name);
        if (companyResult == null || !companyResult.isSuccess()) {
            throw new ServiceException("获取会计公司列表失败");
        }
        return Optional.ofNullable(companyResult.getData())
                .orElse(Collections.emptyList())
                .stream()
                .map(company -> BaseDropDownDTO.ChildTree.builder()
                        .code(company.getId())
                        .value(company.getCompanyName())
                        .disabled(company.getDisabled())
                        .build())
                .collect(Collectors.toList());
    }
}
