package com.erp.server.wms.service.impl;

import com.erp.model.wms.entity.StocktakingTaskDetailEntity;
import com.erp.server.wms.mapper.StocktakingTaskDetailMapper;
import com.erp.server.wms.service.StocktakingTaskDetailService;
import com.common.business.service.SuperServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 盘点任务明细表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-07-31
 */
@Service
public class StocktakingTaskDetailServiceImpl extends SuperServiceImpl<StocktakingTaskDetailMapper, StocktakingTaskDetailEntity> implements StocktakingTaskDetailService {

}
