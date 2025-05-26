package com.erp.server.oms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.UpdateStateDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.entity.ConditionElement;
import com.common.core.server.rule.SpElServer;
import com.erp.model.dmp.entity.AfterSaleEntity;
import com.erp.model.oms.dto.CfgInvoiceSettingDTO;
import com.erp.model.oms.dto.CfgRuleDeclareDTO;
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
import com.erp.server.oms.service.CommonService;
import com.common.core.exception.ServiceException;
import com.erp.server.oms.service.RuleConditionService;
import org.apache.commons.lang3.StringUtils;
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
    public BaseResultDTO.AddDTO add(CfgRuleInvoiceDTO.AddDTO addDTO) {
        CfgRuleInvoiceEntity cfgRuleInvoiceEntity = new CfgRuleInvoiceEntity();
        BeanMapperUtils.copy(addDTO, cfgRuleInvoiceEntity);

        // 数据处理
        handleData(cfgRuleInvoiceEntity);

        log.info("开始新增开票规则");
        boolean save = super.save(cfgRuleInvoiceEntity);
        if(!save) {
            throw new ServiceException("开票规则保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "开票规则" , cfgRuleInvoiceEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, cfgRuleInvoiceEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(cfgRuleInvoiceEntity.getId(), cfgRuleInvoiceEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgRuleInvoiceDTO.UpdateDTO addOrUpdateDTO) {
        CfgRuleInvoiceEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "开票规则"));
        CfgRuleInvoiceEntity cfgRuleInvoiceEntity =  BeanMapperUtils.map(CfgRuleInvoiceEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(cfgRuleInvoiceEntity);
        log.info("编辑 开始修改开票规则数据，id：【{}】", old.getId());
        boolean save = super.updateById(cfgRuleInvoiceEntity);
        if(!save) {
            throw new ServiceException("开票规则保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录开票规则日志数据，id：【{}】", cfgRuleInvoiceEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), cfgRuleInvoiceEntity.getId(), "开票规则");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, cfgRuleInvoiceEntity, null, cfgRuleInvoiceEntity.getId(), msg);
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
        operateLogService.addModuleOperateLog(content, ModuleTypeEnum.RULE_DECLARE.getCode(), entity.getId(), "状态变更");
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
