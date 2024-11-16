package com.erp.server.oms.service.impl;


import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.UpdateStateDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.dto.SpElExpressionDTO;
import com.common.core.entity.ConditionElement;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.server.rule.SpElServer;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.dto.RuleConditionDTO;
import com.erp.model.oms.dto.RuleOrderApprovalDTO;
import com.erp.model.oms.entity.CfgConditionEntity;
import com.erp.model.oms.entity.RuleConditionEntity;
import com.erp.model.oms.entity.RuleOrderApprovalEntity;
import com.erp.model.oms.enums.CfgConditionRuleEnum;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.server.oms.mapper.RuleOrderApprovalMapper;
import com.erp.server.oms.service.CfgConditionService;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.RuleConditionService;
import com.erp.server.oms.service.RuleOrderApprovalService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 订单审核规则 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-08-28
 */
@Slf4j
@Service
public class RuleOrderApprovalServiceImpl extends SuperServiceImpl<RuleOrderApprovalMapper, RuleOrderApprovalEntity> implements RuleOrderApprovalService {
    @Resource
    private OperateLogService operateLogService;

    @Resource
    private RuleConditionService ruleConditionService;

    @Resource
    private SpElServer spElServer;

    @Resource
    private CfgConditionService cfgConditionService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public String add(RuleOrderApprovalDTO.AddDTO addDTO) {
        RuleOrderApprovalEntity ruleOrderApprovalEntity = new RuleOrderApprovalEntity();
        List<RuleConditionDTO.AddDTO> conditionList = addDTO.getConditionList();
        List<String> fieldList = conditionList.stream().map(RuleConditionDTO.AddDTO::getField).distinct().collect(Collectors.toList());
        List<CfgConditionEntity> cfgConditionEntities = cfgConditionService.listByFields(fieldList);
        //去除空格
        conditionList.forEach(v->{
            CfgConditionEntity cfgConditionEntity = cfgConditionEntities.stream().filter(e -> Objects.nonNull(e) && e.getConditionField().equals(v.getField()) && e.getRemark().contains(CfgConditionRuleEnum.REMOVE_SPACE.getCode())).findFirst().orElse(null);
            if (Objects.nonNull(cfgConditionEntity)){
                v.setValue(removeSpace(v.getValue()));
            }else if(StringUtils.isNotBlank(v.getValue())){
                v.setValue(v.getValue().replaceAll(" ",""));
            }
        });
        List<ConditionElement> conditionElementList = conditionList.stream().
                map(c -> new ConditionElement(c.getLeftBracket(), c.getField(),
                        c.getCompare(), c.getValue(),
                        c.getRightBracket(), c.getLogic(),"")).collect(Collectors.toList());
        SpElExpressionDTO splElDTO = spElServer.getConditionExpression(conditionElementList, Map.class);
        String expression = splElDTO.getExpression();
        Boolean checkResult = spElServer.checkExpressionIsEnabled(expression);
        if (!checkResult) {
            throw new ServiceException(ApiError.ERROR_RULE_EXPRESSION_ERROR);
        }
        BeanMapperUtils.copy(addDTO, ruleOrderApprovalEntity);
        List<String> categoryDetailIdList = addDTO.getCategoryDetailIdList();
        String categoryDetailId = CollectionUtils.isNotEmpty(categoryDetailIdList) ? categoryDetailIdList.stream().collect(Collectors.joining(",")) : "";
        ruleOrderApprovalEntity.setCategoryDetailId(categoryDetailId);
        List<String> operationTypeList = addDTO.getOperationTypeList();
        ruleOrderApprovalEntity.setOperationType(operationTypeList.stream().collect(Collectors.joining(",")));
        Boolean save = super.save(ruleOrderApprovalEntity);
        if (!save) {
            throw new ServiceException("订单审核规则保存失败");
        }
        String id = ruleOrderApprovalEntity.getId();
        //保存规则条件
        ruleConditionService.saveRuleCondition(id, conditionList);
        // 操作日志
        String msg =  CharSequenceUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "订单审核规则", ruleOrderApprovalEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.RULE_ORDER_APPROVAL.getCode(), id, "新增操作");
        // TODO 新增明细（如果有明细的话）
        return ruleOrderApprovalEntity.getId();
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(RuleOrderApprovalDTO.UpdateDTO updateDTO) {
        String id = updateDTO.getId();
        RuleOrderApprovalEntity old = super.getById(id);
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "订单审核规则"));
        List<RuleConditionDTO.UpdateDTO> conditionList = updateDTO.getConditionList();
        List<String> fieldList = conditionList.stream().map(RuleConditionDTO.UpdateDTO::getField).distinct().collect(Collectors.toList());
        List<CfgConditionEntity> cfgConditionEntities = cfgConditionService.listByFields(fieldList);
        conditionList.forEach(v->{
            CfgConditionEntity cfgConditionEntity = cfgConditionEntities.stream().filter(e -> Objects.nonNull(e) && e.getConditionField().equals(v.getField()) && e.getRemark().contains(CfgConditionRuleEnum.REMOVE_SPACE.getCode())).findFirst().orElse(null);
            if (Objects.nonNull(cfgConditionEntity)){
                v.setValue(removeSpace(v.getValue()));
            }else if(StringUtils.isNotBlank(v.getValue())){
                v.setValue(v.getValue().replaceAll(" ",""));
            }
        });
        List<ConditionElement> conditionElementList = conditionList.stream().
                map(c -> new ConditionElement(c.getLeftBracket(), c.getField(),
                        c.getCompare(), c.getValue(),
                        c.getRightBracket(), c.getLogic(),"")).collect(Collectors.toList());
        SpElExpressionDTO spElDTO = spElServer.getConditionExpression(conditionElementList, Map.class);
        String expression = spElDTO.getExpression();
        Boolean checkResult = spElServer.checkExpressionIsEnabled(expression);
        if (!checkResult) {
            throw new ServiceException(ApiError.ERROR_RULE_EXPRESSION_ERROR);
        }

        RuleOrderApprovalEntity ruleOrderApprovalEntity = BeanMapperUtils.map(RuleOrderApprovalEntity.class, updateDTO);
        List<String> categoryDetailIdList = updateDTO.getCategoryDetailIdList();
        String categoryDetailId = CollectionUtils.isNotEmpty(categoryDetailIdList) ? categoryDetailIdList.stream().collect(Collectors.joining(",")) : "";
        ruleOrderApprovalEntity.setCategoryDetailId(categoryDetailId);
        List<String> operationTypeList = updateDTO.getOperationTypeList();
        ruleOrderApprovalEntity.setOperationType(operationTypeList.stream().collect(Collectors.joining(",")));
        Boolean save = super.updateById(ruleOrderApprovalEntity);
        if (!save) {
            throw new ServiceException("订单审核规则保存失败");
        }


        ruleConditionService.updateRuleCondition(id, conditionList);

        String msg =  CharSequenceUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), ruleOrderApprovalEntity.getId(), "订单审核规则");
        operateLogService.addModuleOperateLogByObj(old, ruleOrderApprovalEntity, ModuleTypeEnum.RULE_ORDER_APPROVAL.getCode(), ruleOrderApprovalEntity.getId(), msg);
        return Boolean.TRUE;
    }

    /**
     * 去掉前后空格 以及逗号前后空格
     * @param value
     * @return
     */
    private static String removeSpace(String value) {
        if (StrUtil.isBlank(value)){
            return value;
        }
        //去掉前后空格
        value = value.trim();
        // 使用正则表达式去掉逗号前后的空格
        // 英文逗号前后的空格
        value = value.replaceAll("\\s*,\\s*", ",");
        // 中文逗号前后的空格
        value = value.replaceAll("\\s*，\\s*", "，");
        return value;
    }

    /**
     * 分页查询
     *
     * @param dto
     * @return
     */
    @Override
    public PagingVO<RuleOrderApprovalDTO.PagingViewDTO> paging(PagingDTO<RuleOrderApprovalDTO.PagingParamDTO> dto) {
        RuleOrderApprovalDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page<T> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage pageData = baseMapper.paging(query, params);
        return new PagingVO<>(pageData);
    }


    /**
     * 更改启用禁用状态
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-08-30 14:15
     */
    @Override
    public Boolean updateStatus(UpdateStateDTO dto) {
        RuleOrderApprovalEntity ruleOrderApproval = this.getById(dto.getId());
        Optional.ofNullable(ruleOrderApproval).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "订单审核规则"));
        Boolean disabled = ruleOrderApproval.getDisabled();
        if (disabled.equals(dto.getState())) {
            throw new ServiceException(ApiError.ERROR_98027);
        }
        String content = String.format("启用状态[%s]变更为[%s]", disabled ? "启用" : "停用", disabled ? "停用" : "启用");
        ruleOrderApproval.setDisabled(dto.getState());
        operateLogService.addModuleOperateLog(content, ModuleTypeEnum.RULE_ORDER_APPROVAL.getCode(), dto.getId(), "状态变更");
        return this.updateById(ruleOrderApproval);
    }

    @Override
    public RuleOrderApprovalDTO.ViewDTO view(String id) {
        RuleOrderApprovalEntity ruleOrderApproval = this.getById(id);
        Optional.ofNullable(ruleOrderApproval).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "订单审核规则"));
        RuleOrderApprovalDTO.ViewDTO view = new RuleOrderApprovalDTO.ViewDTO();
        BeanMapper.copy(ruleOrderApproval, view);
        String categoryDetailId = ruleOrderApproval.getCategoryDetailId();
        if (StringUtils.isNotBlank(categoryDetailId)) {
            view.setCategoryDetailIdList(Arrays.asList(categoryDetailId.split(",")));
        } else {
            view.setCategoryDetailIdList(Collections.emptyList());
        }

        String operationType = ruleOrderApproval.getOperationType();
        List<String> operationTypeList = StringUtils.isNotBlank(operationType) ? Arrays.asList(operationType.split(",")) : Collections.emptyList();
        view.setOperationTypeList(operationTypeList);
        String type = DictBasicTypeEnum.FIELD.getType();
        List<RuleConditionDTO.ViewDTO> conditionList = ruleConditionService.listByRuleId(id, type);
        view.setConditionList(conditionList);
        return view;
    }

    /**
     * 获取到订单审核匹配结果
     *
     * @param map
     * @return
     */
    @Override
    public RuleOrderApprovalDTO.RuleMatchDTO getRuleOrderMatchResult(Map<String,Object> map) {
        RuleOrderApprovalDTO.RuleMatchDTO ruleMatch = new RuleOrderApprovalDTO.RuleMatchDTO();
        if (Objects.isNull(map)) {
            ruleMatch.setApproveSuccess(Boolean.FALSE);
            return ruleMatch;
        }
        log.info("参数为=========={}", map);
        List<RuleOrderApprovalEntity> ruleOrderApprovalList = this.listOrderByPriority();
        List<String> ruleIdList = ruleOrderApprovalList.stream().map(RuleOrderApprovalEntity::getId).collect(Collectors.toList());
        //规则条件
        List<RuleConditionEntity> allRuleConditionList = ruleConditionService.listDbRuleIds(ruleIdList);
        for (RuleOrderApprovalEntity item : ruleOrderApprovalList) {
            String ruleId = item.getId();
            List<RuleConditionEntity> ruleConditionList = allRuleConditionList.stream().
                    filter(r -> r.getRuleId().equals(ruleId)).
                    sorted(Comparator.comparing(RuleConditionEntity::getIndex)).collect(Collectors.toList());

            List<ConditionElement> conditionElementList = BeanMapper.copyList(ruleConditionList, ConditionElement.class);
            //获取到表达式
            Boolean matchResult = spElServer.matchExpressionByConditionList(conditionElementList, map);
            if (matchResult) {
                ruleMatch.setFlowStatus(item.getFlowStatus());
                String categoryDetailId = item.getCategoryDetailId();
                if(StringUtils.isNotBlank(categoryDetailId)){
                    ruleMatch.setCategoryDetailIdList(Arrays.asList(categoryDetailId.split(",")));
                }
                ruleMatch.setApproveSuccess(Boolean.TRUE);
                ruleMatch.setRuleName(item.getName());
                return ruleMatch;
            }
        }
        return ruleMatch;
    }

    /**
     * 根据有限级获取到订单审核
     *
     * @return
     */
    private List<RuleOrderApprovalEntity> listOrderByPriority() {
        return this.lambdaQuery().eq(RuleOrderApprovalEntity::getDisabled, Boolean.FALSE).
                orderByAsc(RuleOrderApprovalEntity::getPriority).
                orderByDesc(RuleOrderApprovalEntity::getUpdateTime).
                list();
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(RuleOrderApprovalEntity ruleOrderApprovalEntity) {
        // TODO 验证数据 & 数据赋值
    }
}
