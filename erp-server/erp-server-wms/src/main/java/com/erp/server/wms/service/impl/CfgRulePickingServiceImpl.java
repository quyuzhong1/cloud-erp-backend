package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.UpdateStateDTO;
import com.common.business.vo.PagingVO;
import com.common.core.utils.BeanMapperUtils;
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
import com.common.business.service.impl.SuperServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

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

    @Override
    public PagingVO<CfgRulePickingDTO.PagingView> paging(PagingDTO<CfgRulePickingDTO.PagingParam> dto) {
        IPage<CfgRulePickingDTO.PagingView> page = baseMapper.paging(new Page<>(dto.getCurrPage(), dto.getPageSize()), dto.getParams());
        return new PagingVO<>(page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(CfgRulePickingDTO.Add dto) {

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(CfgRulePickingDTO.Add dto, String id) {

    }

    @Override
    public CfgRulePickingDTO.View view(String id) {
        CfgRulePickingEntity entity = getById(id);
        CfgRulePickingDTO.View view = BeanMapperUtils.map(CfgRulePickingDTO.View.class, entity);
        //查询规则条件
        List<CfgRuleConditionEntity> ruleConditionEntities = cfgRuleConditionService.list(Wrappers.<CfgRuleConditionEntity>lambdaQuery()
                .eq(CfgRuleConditionEntity::getRuleId, id));
        List<CfgRuleConditionDTO> ruleConditions = BeanMapperUtils.copyList(CfgRuleConditionDTO.class, ruleConditionEntities);
        view.setConditionList(ruleConditions);
        // 查询规则动作
        List<CfgRulePackingActionEntity> actionEntities = cfgRulePackingActionService.list(Wrappers.<CfgRulePackingActionEntity>lambdaQuery()
                .eq(CfgRulePackingActionEntity::getRuleId, id));
        List<CfgRuleActionDTO> actions = BeanMapperUtils.copyList(CfgRuleActionDTO.class, actionEntities);
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
        update(Wrappers.<CfgRulePickingEntity>lambdaUpdate()
                .set(CfgRulePickingEntity::getDisabled, dto.getDisabled())
                .in(CfgRulePickingEntity::getId, dto.getIds()));
    }
}
