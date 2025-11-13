package com.erp.server.workflow.service.impl;


import cn.hutool.core.collection.CollUtil;
import com.erp.model.workflow.entity.CfgApproveSyncFieldMapEntity;
import com.erp.server.workflow.mapper.CfgApproveSyncFieldMapMapper;
import com.erp.server.workflow.service.CfgApproveSyncFieldMapService;
import com.common.business.service.impl.SuperServiceImpl;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * <p>
 * ERP审批同步-推送信息配置 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-05-12
 */
@Slf4j
@Service
public class CfgApproveSyncFieldMapServiceImpl extends SuperServiceImpl<CfgApproveSyncFieldMapMapper, CfgApproveSyncFieldMapEntity> implements CfgApproveSyncFieldMapService {

    @Override
    public List<CfgApproveSyncFieldMapEntity> listByMainIds(List<String> ids) {
        if(CollUtil.isNotEmpty(ids)){
            return lambdaQuery().in(CfgApproveSyncFieldMapEntity::getMainId, ids).list();
        }
        return Collections.emptyList();
    }

}
