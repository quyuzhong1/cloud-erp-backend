package com.erp.server.wms.service.impl;

import com.erp.model.wms.entity.StocktakingTaskEntity;
import com.erp.server.wms.mapper.StocktakingTaskMapper;
import com.erp.server.wms.service.StocktakingTaskService;
import com.common.business.service.SuperServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 盘点任务表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-07-31
 */
@Service
public class StocktakingTaskServiceImpl extends SuperServiceImpl<StocktakingTaskMapper, StocktakingTaskEntity> implements StocktakingTaskService {

}
