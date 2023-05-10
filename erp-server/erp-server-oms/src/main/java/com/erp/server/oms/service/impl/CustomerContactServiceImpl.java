package com.erp.server.oms.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.erp.model.oms.entity.CustomerContactEntity;
import com.erp.server.oms.mapper.CustomerContactMapper;
import com.erp.server.oms.service.CustomerContactService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 客户联系人信息 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Service
public class CustomerContactServiceImpl extends SuperServiceImpl<CustomerContactMapper, CustomerContactEntity> implements CustomerContactService {

}
