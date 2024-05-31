package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.wms.entity.CfgRulePackingActionEntity;
import com.erp.server.wms.mapper.CfgRulePackingActionMapper;
import com.erp.server.wms.service.CfgRulePackingActionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 * 仓位分配规则表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2024-05-28
 */
@Service
public class CfgRulePackingActionServiceImpl extends SuperServiceImpl<CfgRulePackingActionMapper, CfgRulePackingActionEntity> implements CfgRulePackingActionService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByRuleIds(List<String> ids) {
        remove(Wrappers.<CfgRulePackingActionEntity>lambdaQuery().in(CfgRulePackingActionEntity::getRuleId, ids));
    }
}
