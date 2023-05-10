package com.erp.server.oms.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.erp.model.oms.entity.CustomerAddressEntity;
import com.erp.server.oms.mapper.CustomerAddressMapper;
import com.erp.server.oms.service.CustomerAddressService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 客户地址信息 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Service
public class CustomerAddressServiceImpl extends SuperServiceImpl<CustomerAddressMapper, CustomerAddressEntity> implements CustomerAddressService {

}
