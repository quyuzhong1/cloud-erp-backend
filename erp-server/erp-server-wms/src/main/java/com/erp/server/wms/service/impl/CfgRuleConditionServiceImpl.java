package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.erp.model.wms.entity.CfgRuleConditionEntity;
import com.erp.server.wms.mapper.CfgRuleConditionMapper;
import com.erp.server.wms.service.CfgRuleConditionService;
import com.common.business.service.impl.SuperServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 * 规则条件表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2024-05-28
 */
@Service
public class CfgRuleConditionServiceImpl extends SuperServiceImpl<CfgRuleConditionMapper, CfgRuleConditionEntity> implements CfgRuleConditionService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByRuleIds(List<String> ids) {
        remove(Wrappers.<CfgRuleConditionEntity>lambdaQuery().in(CfgRuleConditionEntity::getRuleId, ids));
    }
}
