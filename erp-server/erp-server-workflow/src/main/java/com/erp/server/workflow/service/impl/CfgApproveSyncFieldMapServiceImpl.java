package com.erp.server.workflow.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.workflow.entity.CfgApproveNoticeEntity;
import com.erp.model.workflow.entity.CfgApproveSyncFieldMapEntity;
import com.erp.server.workflow.mapper.CfgApproveSyncFieldMapMapper;
import com.erp.server.workflow.service.CfgApproveSyncFieldMapService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.workflow.service.OperateLogService;
import com.erp.server.workflow.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.workflow.dto.CfgApproveSyncFieldMapDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
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
