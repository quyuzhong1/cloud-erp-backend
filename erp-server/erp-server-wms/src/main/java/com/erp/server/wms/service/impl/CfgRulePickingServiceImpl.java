package com.erp.server.wms.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.UpdateStateDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.pickingstrategy.CfgRuleActionDTO;
import com.erp.model.wms.dto.pickingstrategy.CfgRuleConditionDTO;
import com.erp.model.wms.dto.pickingstrategy.CfgRulePickingDTO;
import com.erp.model.wms.entity.CfgRuleConditionEntity;
import com.erp.model.wms.entity.CfgRulePackingActionEntity;
import com.erp.model.wms.entity.CfgRulePickingEntity;
import com.erp.server.wms.mapper.CfgRulePickingMapper;
import com.erp.server.wms.service.CfgRuleConditionService;
import com.erp.server.wms.service.CfgRulePackingActionService;
import com.erp.server.wms.service.CfgRulePickingService;
import com.erp.server.wms.service.OperateLogService;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 拣货规则表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2024-05-28
 */
@Service
public class CfgRulePickingServiceImpl extends SuperServiceImpl<CfgRulePickingMapper, CfgRulePickingEntity> implements CfgRulePickingService {

    @Resource
    private CfgRuleConditionService cfgRuleConditionService;
    @Resource
    private CfgRulePackingActionService cfgRulePackingActionService;
    @Resource
    private OperateLogService operateLogService;

    @Override
    public PagingVO<CfgRulePickingDTO.PagingView> paging(PagingDTO<CfgRulePickingDTO.PagingParam> dto) {
        IPage<CfgRulePickingDTO.PagingView> page = baseMapper.paging(new Page<>(dto.getCurrPage(), dto.getPageSize()), dto.getParams());
        return new PagingVO<>(page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(CfgRulePickingDTO.Add dto) {
        CfgRulePickingEntity entity = BeanMapperUtils.map(CfgRulePickingEntity.class, dto);
        save(entity);
        // 操作日志
        String msg = CharSequenceUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "拣货策略规则", entity.getId());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.PICKING_STRATEGY.getCode(), entity.getId(), "新增操作");
        cfgRuleConditionService.saveRuleCondition(entity.getId(), dto.getConditionList(), "PICKING_STRATEGY");
        cfgRulePackingActionService.saveRuleAction(entity.getId(), dto.getActions());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @SuppressWarnings("all")
    public void update(CfgRulePickingDTO.Update dto) {
        CfgRulePickingEntity old = getById(dto.getId());
        CfgRulePickingEntity entity = BeanMapperUtils.map(CfgRulePickingEntity.class, dto);
        updateById(entity);
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dto.getId(), "拣货策略规则");
        operateLogService.addModuleOperateLogByObj(old, entity, ModuleTypeEnum.PICKING_STRATEGY.getCode(), dto.getId(), msg);
        cfgRuleConditionService.updateRuleCondition(dto.getId(), dto.getConditionList(), ModuleTypeEnum.PICKING_STRATEGY.getCode(), "");
        cfgRulePackingActionService.updateRuleAction(dto.getId(), dto.getActions());
    }

    @Override
    public CfgRulePickingDTO.View view(String id) {
        CfgRulePickingEntity entity = getById(id);
        CfgRulePickingDTO.View view = BeanMapperUtils.map(CfgRulePickingDTO.View.class, entity);
        //查询规则条件
        List<CfgRuleConditionEntity> ruleConditionEntities = cfgRuleConditionService.list(Wrappers.<CfgRuleConditionEntity>lambdaQuery()
                .eq(CfgRuleConditionEntity::getRuleId, id)
                .orderByAsc(CfgRuleConditionEntity::getIndex));
        List<CfgRuleConditionDTO.View> ruleConditions = BeanMapperUtils.copyList(CfgRuleConditionDTO.View.class, ruleConditionEntities);
        view.setConditionList(ruleConditions);
        // 查询规则动作
        List<CfgRulePackingActionEntity> actionEntities = cfgRulePackingActionService.list(Wrappers.<CfgRulePackingActionEntity>lambdaQuery()
                .eq(CfgRulePackingActionEntity::getRuleId, id)
                .orderByAsc(CfgRulePackingActionEntity::getIndex));
        List<CfgRuleActionDTO.View> actions = BeanMapperUtils.copyList(CfgRuleActionDTO.View.class, actionEntities);
        view.setActions(actions);
        return view;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<String> ids) {
        //删除拣货规则
        removeByIds(ids);
        //删除规则
        cfgRuleConditionService.removeByRuleIds(ids);
        //删除拣货动作
        cfgRulePackingActionService.removeByRuleIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(UpdateStateDTO.BatchUpdateDTO dto) {
        List<CfgRulePickingEntity> list = listByIds(dto.getIds());
        List<Pair<String, String>> pairs = list.stream().map(e -> Pair.create(e.getId(), Boolean.TRUE.equals(e.getDisabled()) ? "停用" : "启用")).collect(Collectors.toList());
        update(Wrappers.<CfgRulePickingEntity>lambdaUpdate()
                .set(CfgRulePickingEntity::getDisabled, dto.getDisabled())
                .in(CfgRulePickingEntity::getId, dto.getIds()));
        String content = "启用状态由[%s]变更为" + (Boolean.TRUE.equals(dto.getDisabled()) ? "停用" : "启用");
        operateLogService.batchAddModuleOperateLog(content, ModuleTypeEnum.PICKING_STRATEGY.getCode(), pairs, "状态变更");
    }
}
