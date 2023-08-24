package com.erp.server.oms.service.impl;

import com.erp.model.oms.entity.OrderCategoryEntity;
import com.erp.server.oms.mapper.OrderCategoryMapper;
import com.erp.server.oms.service.OrderCategoryService;
import com.common.business.service.SuperServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 订单分类表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-08-24
 */
@Service
public class OrderCategoryServiceImpl extends SuperServiceImpl<OrderCategoryMapper, OrderCategoryEntity> implements OrderCategoryService {

}
