package com.erp.server.oms.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.erp.model.oms.entity.CustomerSellerEntity;
import com.erp.server.oms.mapper.CustomerSellerMapper;
import com.erp.server.oms.service.CustomerSellerService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 客户销售员信息 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Service
public class CustomerSellerServiceImpl extends SuperServiceImpl<CustomerSellerMapper, CustomerSellerEntity> implements CustomerSellerService {

}
