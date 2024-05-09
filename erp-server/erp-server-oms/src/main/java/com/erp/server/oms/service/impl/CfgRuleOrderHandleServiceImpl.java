package com.erp.server.oms.service.impl;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.UpdateStateDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.dto.SpElExpressionDTO;
import com.common.core.entity.ConditionElement;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.server.rule.SpElServer;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.dto.CfgRuleOrderHandleDTO;
import com.erp.model.oms.dto.RuleConditionDTO;
import com.erp.model.oms.entity.CfgRuleOrderHandleEntity;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.server.oms.mapper.CfgRuleOrderHandleMapper;
import com.erp.server.oms.service.CfgRuleOrderHandleService;
import com.erp.server.oms.service.CommonService;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.RuleConditionService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>
 * 订单处理规则表 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-05-09
 */
@Slf4j
@Service
public class CfgRuleOrderHandleServiceImpl extends SuperServiceImpl<CfgRuleOrderHandleMapper, CfgRuleOrderHandleEntity> implements CfgRuleOrderHandleService {
    @Autowired
    private OperateLogService operateLogService;

    @Autowired
    private CommonService commonService;

    @Autowired
    private RuleConditionService ruleConditionService;

    @Autowired
    private SpElServer spElServer;


    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgRuleOrderHandleDTO.AddDTO addDTO) {
        CfgRuleOrderHandleEntity cfgRuleOrderHandleEntity = new CfgRuleOrderHandleEntity();
        BeanMapperUtils.copy(addDTO, cfgRuleOrderHandleEntity);

        List<RuleConditionDTO.AddDTO> conditionList = addDTO.getConditionList();
        List<ConditionElement> conditionElementList = conditionList.stream().
                map(c -> new ConditionElement(c.getLeftBracket(), c.getField(),
                        c.getCompare(), c.getValue(),
                        c.getRightBracket(), c.getLogic(), "")).collect(Collectors.toList());
        //数据校验
        checkData(cfgRuleOrderHandleEntity,conditionElementList);

        log.info("开始新增订单处理规则单");
        boolean save = super.save(cfgRuleOrderHandleEntity);
        if(!save) {
            throw new ServiceException("订单处理规则单保存失败");
        }
        //保存规则条件
        ruleConditionService.saveRuleCondition(cfgRuleOrderHandleEntity.getId(), conditionList);

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "订单处理规则单" , cfgRuleOrderHandleEntity.getId());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CFG_RULE_ORDER_HANDLE.getCode(), cfgRuleOrderHandleEntity.getId(), "新增操作");

        return new BaseResultDTO.AddDTO(cfgRuleOrderHandleEntity.getId(), cfgRuleOrderHandleEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgRuleOrderHandleDTO.UpdateDTO updateDTO) {
        CfgRuleOrderHandleEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "订单处理规则单"));
        CfgRuleOrderHandleEntity cfgRuleOrderHandleEntity =  BeanMapperUtils.map(CfgRuleOrderHandleEntity.class, updateDTO);

        List<RuleConditionDTO.UpdateDTO> conditionList = updateDTO.getConditionList();
        List<ConditionElement> conditionElementList = conditionList.stream().
                map(c -> new ConditionElement(c.getLeftBracket(), c.getField(),
                        c.getCompare(), c.getValue(),
                        c.getRightBracket(), c.getLogic(), "")).collect(Collectors.toList());
        //数据校验
        checkData(cfgRuleOrderHandleEntity,conditionElementList);

        log.info("编辑 开始修改订单处理规则单数据，id：【{}】", old.getId());
        boolean save = super.updateById(cfgRuleOrderHandleEntity);
        if(!save) {
            throw new ServiceException("订单处理规则单保存失败");
        }
        //保存规则条件
        ruleConditionService.updateRuleCondition(cfgRuleOrderHandleEntity.getId(), updateDTO.getConditionList());

        // 记录主单操作日志
        log.info("编辑 开始记录订单处理规则单日志数据，id：【{}】", cfgRuleOrderHandleEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), cfgRuleOrderHandleEntity.getId(), "订单处理规则单");
        operateLogService.addModuleOperateLogByObj(old, cfgRuleOrderHandleEntity, ModuleTypeEnum.CFG_RULE_ORDER_HANDLE.getCode(), cfgRuleOrderHandleEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<CfgRuleOrderHandleDTO.ListDTO> paging(PagingDTO<CfgRuleOrderHandleDTO.PagingParamDTO> dto) {
        CfgRuleOrderHandleDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage pageData = baseMapper.paging(query, params);
        return new PagingVO<>(pageData);
    }

    @Override
    public CfgRuleOrderHandleDTO.ViewDTO view(String id) {
        CfgRuleOrderHandleEntity ruleOrderHandle = this.getById(id);
        Optional.ofNullable(ruleOrderHandle).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "订单处理规则"));
        CfgRuleOrderHandleDTO.ViewDTO view = new CfgRuleOrderHandleDTO.ViewDTO();
        BeanMapper.copy(ruleOrderHandle, view);
        String type = DictBasicTypeEnum.FIELD.getType();
        List<RuleConditionDTO.ViewDTO> conditionList = ruleConditionService.listByRuleId(id, type);
        view.setConditionList(conditionList);
        return view;
    }

    @Override
    public Boolean updateStatus(UpdateStateDTO dto) {
        CfgRuleOrderHandleEntity ruleOrderHandle = this.getById(dto.getId());
        Optional.ofNullable(ruleOrderHandle).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "物流规则单"));
        Boolean disabled = ruleOrderHandle.getDisabled();
        if (disabled.equals(dto.getState())) {
            throw new ServiceException(ApiError.ERROR_98027);
        }
        String content = String.format("启用状态[%s]变更为[%s]", disabled ? "启用" : "停用", disabled ? "停用" : "启用");
        ruleOrderHandle.setDisabled(dto.getState());
        operateLogService.addModuleOperateLog(content, ModuleTypeEnum.CFG_RULE_ORDER_HANDLE.getCode(), dto.getId(), "状态更新");
        return this.updateById(ruleOrderHandle);
    }

    /**
     * 根据规则名称查询
     */
    private CfgRuleOrderHandleEntity getByName(String name) {
        return lambdaQuery().eq(CfgRuleOrderHandleEntity::getName, name).one();
    }

    /**
     * @description: 数据校验
     * @author Will
     * @date: 2024/5/9 10:43
     * @param cfgRuleOrderHandleEntity
     */
    private void checkData(CfgRuleOrderHandleEntity cfgRuleOrderHandleEntity,List<ConditionElement> conditionElementList) {
        //校验名称是否存在
        CfgRuleOrderHandleEntity old = this.getByName(cfgRuleOrderHandleEntity.getName());
        if (ObjectUtil.isNotEmpty(old) && !StrUtil.equals(cfgRuleOrderHandleEntity.getId(),old.getId())) {
            throw new ServiceException(ApiError.ERROR_NAME_EXIST,cfgRuleOrderHandleEntity.getName());
        }
        //校验规则表达式是否有效
        SpElExpressionDTO sqElDTO = spElServer.getConditionExpression(conditionElementList, Map.class);
        String expression = sqElDTO.getExpression();
        Boolean checkResult = spElServer.checkExpressionIsEnabled(expression);
        if (!checkResult) {
            throw new ServiceException(ApiError.ERROR_RULE_EXPRESSION_ERROR, expression);
        }
    }

}
