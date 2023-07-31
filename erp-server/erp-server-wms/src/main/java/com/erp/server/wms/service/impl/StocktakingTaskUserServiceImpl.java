package com.erp.server.wms.service.impl;

import com.erp.model.wms.entity.StocktakingTaskUserEntity;
import com.erp.server.wms.mapper.StocktakingTaskUserMapper;
import com.erp.server.wms.service.StocktakingTaskUserService;
import com.common.business.service.SuperServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 盘点任务 盘点人表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-07-31
 */
@Service
public class StocktakingTaskUserServiceImpl extends SuperServiceImpl<StocktakingTaskUserMapper, StocktakingTaskUserEntity> implements StocktakingTaskUserService {

}
