package com.erp.server.sys.service.impl;


import cn.hutool.core.collection.CollUtil;
import com.erp.model.sys.entity.CfgApproveSyncFieldMapEntity;
import com.erp.server.sys.mapper.CfgApproveSyncFieldMapMapper;
import com.erp.server.sys.service.CfgApproveSyncFieldMapService;
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
 * @since 2025-05-23
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
