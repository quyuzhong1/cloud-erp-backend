package com.erp.server.oms.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.erp.model.oms.entity.CustomerGroupEntity;
import com.erp.server.oms.mapper.CustomerGroupMapper;
import com.erp.server.oms.service.CustomerGroupService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 客户分组表 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Service
public class CustomerGroupServiceImpl extends SuperServiceImpl<CustomerGroupMapper, CustomerGroupEntity> implements CustomerGroupService {

}
