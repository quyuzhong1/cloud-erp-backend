package com.erp.server.oms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.UpdateStateDTO;
import com.common.business.vo.PagingVO;
import com.common.core.dto.SpElExpressionDTO;
import com.common.core.entity.ConditionElement;
import com.common.core.server.rule.SpElServer;
import com.erp.model.oms.dto.RuleConditionDTO;
import com.erp.model.oms.dto.RuleLogisticsDTO;
import com.erp.model.oms.entity.CfgDeclareEntity;
import com.erp.model.oms.entity.RuleLogisticsEntity;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.oms.enums.RuleTypeEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.server.oms.mapper.CfgDeclareMapper;
import com.erp.server.oms.service.CfgDeclareService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.CommonService;
import com.common.core.exception.ServiceException;
import com.erp.server.oms.service.RuleConditionService;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.oms.dto.CfgDeclareDTO;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 申报规则表 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2024-05-08
 */
@Slf4j
@Service
public class CfgDeclareServiceImpl extends SuperServiceImpl<CfgDeclareMapper, CfgDeclareEntity> implements CfgDeclareService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;
    @Autowired
    private SpElServer spElServer;
    @Autowired
    private RuleConditionService ruleConditionService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String add(CfgDeclareDTO.AddDTO addDTO) {
        List<RuleConditionDTO.AddDTO> conditionList = addDTO.getConditionList();
        List<ConditionElement> conditionElementList = conditionList.stream().
                map(c -> new ConditionElement(c.getLeftBracket(), c.getField(),
                        c.getCompare(), c.getValue(),
                        c.getRightBracket(), c.getLogic(), "")).collect(Collectors.toList());
        SpElExpressionDTO sqElDTO = spElServer.getConditionExpression(conditionElementList, Map.class);
        String expression = sqElDTO.getExpression();
        Boolean checkResult = spElServer.checkExpressionIsEnabled(expression);
        if (!checkResult) {
            throw new ServiceException(ApiError.ERROR_RULE_EXPRESSION_ERROR, expression);
        }
        CfgDeclareEntity entity = new CfgDeclareEntity();
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
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "申报规则单", id);
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.RULE_DECLARE.getCode(), id, "新增操作");
        return entity.getId();
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgDeclareDTO.UpdateDTO updateDTO) {
        String id = updateDTO.getId();
        CfgDeclareEntity old = super.getById(id);
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "申报规则单"));
        List<RuleConditionDTO.UpdateDTO> conditionList = updateDTO.getConditionList();
        List<ConditionElement> conditionElementList = conditionList.stream().
                map(c -> new ConditionElement(c.getLeftBracket(), c.getField(),
                        c.getCompare(), c.getValue(),
                        c.getRightBracket(), c.getLogic(), "")).collect(Collectors.toList());
        SpElExpressionDTO sqElDTO = spElServer.getConditionExpression(conditionElementList, Map.class);
        String expression = sqElDTO.getExpression();
        Boolean checkResult = spElServer.checkExpressionIsEnabled(expression);
        if (!checkResult) {
            throw new ServiceException(ApiError.ERROR_RULE_EXPRESSION_ERROR, expression);
        }
        CfgDeclareEntity entity = BeanMapperUtils.map(CfgDeclareEntity.class, updateDTO);
        // 数据处理
        handleData(entity);
        boolean save = super.updateById(entity);
        if (!save) {
            throw new ServiceException("申报规则单保存失败");
        }
        ruleConditionService.updateRuleCondition(id, conditionList);
        // 记录主单操作日志
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), entity.getId(), "申报规则单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, entity, ModuleTypeEnum.RULE_DECLARE.getCode(), entity.getId(), msg);
        return Boolean.TRUE;
    }

    /**
     * 申报规则分页查询
     * @param dto
     * @return
     */
    @Override
    public PagingVO<CfgDeclareDTO.PagingViewDTO> paging(PagingDTO<CfgDeclareDTO.PagingParamDTO> dto) {
        CfgDeclareDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage pageData = baseMapper.paging(query, params);
        return new PagingVO<>(pageData);

    }

    @Override
    public CfgDeclareDTO.ViewDTO view(String id) {
        CfgDeclareEntity entity = this.getById(id);
        Optional.ofNullable(entity).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "申报规则单"));
        CfgDeclareDTO.ViewDTO view = new CfgDeclareDTO.ViewDTO();
        BeanMapper.copy(entity, view);
        String type = DictBasicTypeEnum.FIELD.getType();
        List<RuleConditionDTO.ViewDTO> conditionList = ruleConditionService.listByRuleId(id, type);
        view.setConditionList(conditionList);
        return view;
    }

    @Override
    public Boolean updateStatus(UpdateStateDTO dto) {
        CfgDeclareEntity entity = this.getById(dto.getId());
        Optional.ofNullable(entity).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "申报规则单"));
        Boolean disabled = entity.getDisabled();
        if (disabled.equals(dto.getState())) {
            throw new ServiceException(ApiError.ERROR_98027);
        }
        String content = String.format("启用状态[%s]变更为[%s]", disabled ? "启用" : "停用", disabled ? "停用" : "启用");
        entity.setDisabled(dto.getState());
        operateLogService.addModuleOperateLog(content, ModuleTypeEnum.RULE_DECLARE.getCode(), dto.getId(), "状态变更");
        return this.updateById(entity);

    }


    /**
    * 新增修改处理数据
    */
    private void handleData(CfgDeclareEntity cfgDeclareEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
