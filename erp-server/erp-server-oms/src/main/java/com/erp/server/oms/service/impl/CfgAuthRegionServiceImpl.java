package com.erp.server.oms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.oms.entity.CfgAuthRegionEntity;
import com.erp.server.oms.mapper.CfgAuthRegionMapper;
import com.erp.server.oms.service.CfgAuthRegionService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.oms.service.OperateLogService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 授权区域配置 服务实现类
 * </p>
 *
 * @author Jim
 * @since 2025-04-21
 */
@Slf4j
@Service
public class CfgAuthRegionServiceImpl extends SuperServiceImpl<CfgAuthRegionMapper, CfgAuthRegionEntity> implements CfgAuthRegionService {

}
