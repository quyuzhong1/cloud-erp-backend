package com.erp.server.dmp.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.vo.PagingVO;
import com.common.core.dto.SpElExpressionDTO;
import com.common.core.entity.ConditionElement;
import com.common.core.server.rule.SpElServer;
import com.erp.model.dmp.entity.RulePromptWordEntity;
import com.erp.model.dmp.dto.RuleConditionDTO;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.server.dmp.mapper.RulePromptWordMapper;
import com.erp.server.dmp.service.RuleConditionService;
import com.erp.server.dmp.service.RulePromptWordService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.dmp.service.OperateLogService;

import com.common.core.exception.ServiceException;
import io.seata.common.util.StringUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.dmp.dto.RulePromptWordDTO;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;

/**
 * <p>
 * 汉化管理规则表 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2025-01-17
 */
@Slf4j
@Service
public class RulePromptWordServiceImpl extends SuperServiceImpl<RulePromptWordMapper, RulePromptWordEntity> implements RulePromptWordService {
    @Autowired
    private OperateLogService operateLogService;

    @Resource
    private RuleConditionService ruleConditionService;

    @Resource
    private SpElServer spElServer;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(RulePromptWordDTO.AddDTO addDTO) {
        List<RuleConditionDTO.AddDTO> conditionList = addDTO.getConditionList();
        List<ConditionElement> conditionElementList = conditionList.stream().
                map(c -> new ConditionElement(c.getLeftBracket(), c.getField(),
                        c.getCompare(), c.getValue(),
                        c.getRightBracket(), c.getLogic(),"")).collect(Collectors.toList());
        SpElExpressionDTO expressionDTO = spElServer.getConditionExpression(conditionElementList, Map.class);
        String expression = expressionDTO.getExpression();
        Boolean checkResult = spElServer.checkExpressionIsEnabled(expression);
        this.checkNameUnique(addDTO.getName(), null);
        if (!checkResult) {
            throw new ServiceException(ApiError.ERROR_RULE_EXPRESSION_ERROR);
        }
        RulePromptWordEntity rulePromptWordEntity = new RulePromptWordEntity();
        BeanMapperUtils.copy(addDTO, rulePromptWordEntity);

        // 数据处理
        handleData(rulePromptWordEntity);

        log.info("开始新增汉化管理规则单");
        boolean save = super.save(rulePromptWordEntity);
        if(!save) {
            throw new ServiceException("汉化管理规则单保存失败");
        }
        String id = rulePromptWordEntity.getId();
        //保存规则条件
        ruleConditionService.saveRuleCondition(id, conditionList);
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据名称为【{}】", UserContext.getDefaultLoginUser().getUserName(), "汉化管理规则单" , rulePromptWordEntity.getName());

        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.RULE_PROMPT_WORD.getCode(), rulePromptWordEntity.getId(), "新增操作");


        return new BaseResultDTO.AddDTO(rulePromptWordEntity.getId(), rulePromptWordEntity.getId());
    }

    /**
     * 校验名称唯一
     */
    public void checkNameUnique(String name, String id) {
        RulePromptWordEntity rulePromptWordEntity = this.lambdaQuery().eq(RulePromptWordEntity::getName, name).ne(StringUtils.isNotBlank(id),RulePromptWordEntity::getId,id).last("limit 1").one();
        if (rulePromptWordEntity != null) {
            throw new ServiceException(ApiError.COMMON_NAME_EXIST,name);
        }
    }
    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(RulePromptWordDTO.UpdateDTO addOrUpdateDTO) {
        RulePromptWordEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "汉化管理规则单"));
        this.checkNameUnique(addOrUpdateDTO.getName(), addOrUpdateDTO.getId());
        List<RuleConditionDTO.UpdateDTO> conditionList = addOrUpdateDTO.getConditionList();
        List<ConditionElement> conditionElementList = conditionList.stream().
                map(c -> new ConditionElement(c.getLeftBracket(), c.getField(),
                        c.getCompare(), c.getValue(),
                        c.getRightBracket(), c.getLogic(),"")).collect(Collectors.toList());
        SpElExpressionDTO expressionDTO = spElServer.getConditionExpression(conditionElementList, Map.class);
        String expression = expressionDTO.getExpression();
        Boolean checkResult = spElServer.checkExpressionIsEnabled(expression);
        if (!checkResult) {
            throw new ServiceException(ApiError.ERROR_RULE_EXPRESSION_ERROR);
        }
        RulePromptWordEntity rulePromptWordEntity =  BeanMapperUtils.map(RulePromptWordEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(rulePromptWordEntity);
        boolean save = super.updateById(rulePromptWordEntity);
        if(!save) {
            throw new ServiceException("汉化管理规则单保存失败");
        }

        ruleConditionService.updateRuleCondition(rulePromptWordEntity.getId(), conditionList);
        String msg = StrUtil.format("用户【{}】编辑名称为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), rulePromptWordEntity.getName(), "汉化管理规则单");
        operateLogService.addModuleOperateLogByObj(old, rulePromptWordEntity, ModuleTypeEnum.RULE_PROMPT_WORD.getCode(), rulePromptWordEntity.getId(), msg);
        return Boolean.TRUE;

    }

    @Override
    public PagingVO<RulePromptWordDTO.ListDTO> paging(PagingDTO<RulePromptWordDTO.PagingParamDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());

        IPage<RulePromptWordDTO.ListDTO> pageData = this.baseMapper.paging(query, dto.getParams());
        return new PagingVO<>(pageData);
    }

    @Override
    public RulePromptWordDTO.ViewDTO view(String id) {
        RulePromptWordEntity rulePromptWordEntity = this.getById(id);
        if(null == rulePromptWordEntity){
            throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "汉化管理单");
        }
        RulePromptWordDTO.ViewDTO viewDTO = BeanMapperUtils.map(RulePromptWordDTO.ViewDTO.class, rulePromptWordEntity);
        String type = DictBasicTypeEnum.FIELD.getType();
        List<RuleConditionDTO.ViewDTO> conditionList = ruleConditionService.listByRuleId(id, type);
        viewDTO.setConditionList(conditionList);
        return viewDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateStatus(RulePromptWordDTO.UpdateStatusDTO dto) {
        List<RulePromptWordEntity> rulePromptWordEntityList = this.listByIds(dto.getIds());
        if(CollectionUtils.isEmpty(rulePromptWordEntityList)){
            throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "汉化管理单");
        }
        rulePromptWordEntityList = rulePromptWordEntityList.stream().filter(v->!v.getDisabled().equals(dto.getDisabled())).collect(Collectors.toList());
        rulePromptWordEntityList.forEach(v->{
            operateLogService.addModuleOperateLog(StrUtil.format("[{}]启用状态[{}]变更为[{}]",v.getName(),v.getDisabled()?"停用":"启用",dto.getDisabled()?"停用":"启用"), ModuleTypeEnum.RULE_PROMPT_WORD.getCode(), v.getId(), "状态更新");
            v.setDisabled(dto.getDisabled());
        });
        this.updateBatchById(rulePromptWordEntityList);
    }

    @Override
    public List<RulePromptWordDTO.TabListDTO> tabList(PermissionsDTO dto) {
        List<RulePromptWordEntity> list = baseMapper.list();
        List<RulePromptWordDTO.TabListDTO> resultList = new ArrayList<>(2);
        RulePromptWordDTO.TabListDTO tabListDTO = new RulePromptWordDTO.TabListDTO();
        tabListDTO.setTabFlag("false");
        tabListDTO.setTabFlagName("启用");
        tabListDTO.setCount((int) list.stream().filter(v -> v.getDisabled().equals(false)).count());
        resultList.add(tabListDTO);
        RulePromptWordDTO.TabListDTO tabListDTO2 = new RulePromptWordDTO.TabListDTO();
        tabListDTO2.setTabFlag("true");
        tabListDTO2.setTabFlagName("停用");
        tabListDTO2.setCount((int) list.stream().filter(v -> v.getDisabled().equals(true)).count());
        resultList.add(tabListDTO2);
        return resultList;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(RulePromptWordEntity rulePromptWordEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
