package com.erp.server.oms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.dto.SpElExpressionDTO;
import com.common.core.entity.ConditionElement;
import com.common.core.server.rule.SpElServer;
import com.erp.model.oms.dto.CfgInvoiceSettingDTO;
import com.erp.model.oms.dto.RuleConditionDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.oms.enums.InvoiceInfoInvoiceTypeEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.server.oms.mapper.CfgRuleInvoiceMapper;
import com.erp.server.oms.service.CfgRuleInvoiceService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.oms.service.OperateLogService;
import com.common.core.exception.ServiceException;
import com.erp.server.oms.service.RuleConditionService;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.oms.dto.CfgRuleInvoiceDTO;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;

/**
 * <p>
 * 开票规则 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2025-05-23
 */
@Slf4j
@Service
public class CfgRuleInvoiceServiceImpl extends SuperServiceImpl<CfgRuleInvoiceMapper, CfgRuleInvoiceEntity> implements CfgRuleInvoiceService {
    @Autowired
    private OperateLogService operateLogService;
    @Resource
    private RuleConditionService ruleConditionService;
    @Resource
    private SpElServer spElServer;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String add(CfgRuleInvoiceDTO.AddDTO addDTO) {
        List<RuleConditionDTO.AddDTO> conditionList = addDTO.getConditionList();
        List<ConditionElement> conditionElementList = conditionList.stream().
                map(c -> new ConditionElement(c.getLeftBracket(), c.getField(),
                        c.getCompare(), c.getValue(),
                        c.getRightBracket(), c.getLogic(), "")).collect(Collectors.toList());
        SpElExpressionDTO sqElDTO = spElServer.getConditionExpression(conditionElementList, Map.class);
        String expression = sqElDTO.getExpression();
        Boolean checkResult = spElServer.checkExpressionIsEnabled(expression);
        if (Boolean.FALSE.equals(checkResult)) {
            throw new ServiceException(ApiError.ERROR_RULE_EXPRESSION_ERROR);
        }
        CfgRuleInvoiceEntity entity = new CfgRuleInvoiceEntity();
        BeanMapperUtils.copy(addDTO, entity);
        handleData(entity);
        boolean save = super.save(entity);
        if (!save) {
            throw new ServiceException("申报规则单保存失败");
        }

        String id = entity.getId();
        //保存规则条件
        ruleConditionService.saveRuleCondition(id, conditionList);
        // 操作日志
        String msg =  CharSequenceUtil.format("用户【{}】新增【{}】单据名称为【{}】", UserContext.getDefaultLoginUser().getUserName(), ModuleTypeEnum.CFG_RULE_INVOICE.getName(), entity.getName());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CFG_RULE_INVOICE.getCode(), id, "新增操作");
        return entity.getId();
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgRuleInvoiceDTO.UpdateDTO updateDTO) {
        String id = updateDTO.getId();
        CfgRuleInvoiceEntity old = super.getById(id);
        if(null == old){
            throw new ServiceException(ApiError.NOT_EXIST_BILL, ModuleTypeEnum.CFG_RULE_INVOICE.getName());
        }
        List<RuleConditionDTO.UpdateDTO> conditionList = updateDTO.getConditionList();
        List<ConditionElement> conditionElementList = conditionList.stream().
                map(c -> new ConditionElement(c.getLeftBracket(), c.getField(),
                        c.getCompare(), c.getValue(),
                        c.getRightBracket(), c.getLogic(), "")).collect(Collectors.toList());
        SpElExpressionDTO sqElDTO = spElServer.getConditionExpression(conditionElementList, Map.class);
        String expression = sqElDTO.getExpression();
        Boolean checkResult = spElServer.checkExpressionIsEnabled(expression);
        if (Boolean.FALSE.equals(checkResult)) {
            throw new ServiceException(ApiError.ERROR_RULE_EXPRESSION_ERROR);
        }
        CfgRuleInvoiceEntity entity = BeanMapperUtils.map(CfgRuleInvoiceEntity.class, updateDTO);
        // 数据处理
        handleData(entity);
        boolean save = lambdaUpdate().eq(CfgRuleInvoiceEntity::getId,entity.getId())
                .set(CfgRuleInvoiceEntity::getName,entity.getName())
                .set(CfgRuleInvoiceEntity::getPriority,entity.getPriority())
                .set(CfgRuleInvoiceEntity::getDisabled,entity.getDisabled())
                .set(CfgRuleInvoiceEntity::getRemark,entity.getRemark())
                .set(CfgRuleInvoiceEntity::getInvoiceType,entity.getInvoiceType())
                .update(new CfgRuleInvoiceEntity());
        if (!save) {
            throw new ServiceException("开票规则单保存失败");
        }
        ruleConditionService.updateRuleCondition(id, conditionList);
        // 记录主单操作日志
        String msg =  CharSequenceUtil.format("用户【{}】编辑名称为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), entity.getName(), ModuleTypeEnum.CFG_RULE_INVOICE.getName());
        operateLogService.addModuleOperateLogByObj(old, entity, ModuleTypeEnum.CFG_RULE_INVOICE.getCode(), entity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<CfgRuleInvoiceDTO.PagingViewDTO> paging(PagingDTO<CfgRuleInvoiceDTO.PagingParamDTO> dto) {
        CfgRuleInvoiceDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page<T> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<CfgRuleInvoiceDTO.PagingViewDTO> pageData = baseMapper.paging(query, params);
        fillPaging(pageData.getRecords());
        return new PagingVO<>(pageData);

    }

    @Override
    public CfgRuleInvoiceDTO.ViewDTO view(String id) {
        CfgRuleInvoiceEntity entity = this.getById(id);
        if(null == entity){
            throw new ServiceException(ApiError.NOT_EXIST_BILL, ModuleTypeEnum.CFG_RULE_INVOICE.getName());
        }
        CfgRuleInvoiceDTO.ViewDTO view = new CfgRuleInvoiceDTO.ViewDTO();
        BeanMapper.copy(entity, view);
        String type = DictBasicTypeEnum.FIELD.getType();
        List<RuleConditionDTO.ViewDTO> conditionList = ruleConditionService.listByRuleId(id, type);
        view.setConditionList(conditionList);
        return view;
    }

    @Override
    public BatchResultDTO updateStatus(CfgRuleInvoiceEntity entity, Boolean state) {
        Boolean disabled = entity.getDisabled();
        if (disabled.equals(state)) {
            throw new ServiceException(ApiError.ERROR_98027);
        }
        String content = String.format("启用状态[%s]变更为[%s]", Boolean.TRUE.equals(disabled) ? "停用" : "启用", Boolean.TRUE.equals(disabled) ? "启用" : "停用");
        entity.setDisabled(state);
        operateLogService.addModuleOperateLog(content, ModuleTypeEnum.CFG_RULE_INVOICE.getCode(), entity.getId(), "状态变更");
        this.updateById(entity);
        return BatchResultDTO.success();
    }

    @Override
    public BatchResultDTO delete(CfgRuleInvoiceEntity entity) {
        // 删除主单数据
        log.info("删除 开始删除开票规则主单数据，id：【{}】", entity.getId());
        super.removeById(entity.getId());
        // 删除日志数据
        log.info("删除 开始删除开票规则日志数据，id：【{}】", entity.getId());
        String msg = StrUtil.format("用户【{}】名称为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getName(), "开票规则");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CFG_RULE_INVOICE.getCode(), entity.getName(), "删除开票规则数据");
        return BatchResultDTO.success(entity.getId(), entity.getName(), OperationTypeEnum.DELETE);
    }

    @Override
    public CfgInvoiceSettingDTO.RuleMatchDTO getRuleInvoiceMatchResult(Map<String, Object> map) {
        CfgInvoiceSettingDTO.RuleMatchDTO ruleMatch = new CfgInvoiceSettingDTO.RuleMatchDTO();
        if (Objects.isNull(map)) {
            ruleMatch.setApproveSuccess(Boolean.FALSE);
            return ruleMatch;
        }
        log.info("参数为=========={}", map);
        List<CfgRuleInvoiceEntity> ruleInvoiceEntityList = this.listOrderByPriority();
        List<String> ruleIdList = ruleInvoiceEntityList.stream().map(CfgRuleInvoiceEntity::getId).collect(Collectors.toList());
        //规则条件
        List<RuleConditionEntity> allRuleConditionList = ruleConditionService.listDbRuleIds(ruleIdList);
        for (CfgRuleInvoiceEntity item : ruleInvoiceEntityList) {
            String ruleId = item.getId();
            List<RuleConditionEntity> ruleConditionList = allRuleConditionList.stream().
                    filter(r -> r.getRuleId().equals(ruleId)).
                    sorted(Comparator.comparing(RuleConditionEntity::getIndex)).collect(Collectors.toList());

            List<ConditionElement> conditionElementList = BeanMapper.copyList(ruleConditionList, ConditionElement.class);
            //获取到表达式
            Boolean matchResult = spElServer.matchExpressionByConditionList(conditionElementList, map);
            if (matchResult) {
                ruleMatch.setApproveSuccess(Boolean.TRUE);
                ruleMatch.setRuleName(item.getName());
                return ruleMatch;
            }
        }
        return ruleMatch;
    }
    /**
     * 根据优先级获取规则列表
     *
     * @return
     */
    private List<CfgRuleInvoiceEntity> listOrderByPriority() {
        return this.lambdaQuery().eq(CfgRuleInvoiceEntity::getDisabled, Boolean.FALSE).
                orderByAsc(CfgRuleInvoiceEntity::getPriority).
                orderByDesc(CfgRuleInvoiceEntity::getUpdateTime).
                list();
    }
    private void fillPaging(List<CfgRuleInvoiceDTO.PagingViewDTO> records) {
        if (CollUtil.isEmpty(records)) {
            return;
        }
        records.forEach(record -> {
            record.setInvoiceTypeName(InvoiceInfoInvoiceTypeEnum.getName(record.getInvoiceType()));
        });
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(CfgRuleInvoiceEntity cfgRuleInvoiceEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
