package com.erp.server.oms.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.server.oms.mapper.CustomerInfoMapper;
import com.erp.server.oms.service.CustomerInfoService;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Service
public class CustomerInfoServiceImpl extends SuperServiceImpl<CustomerInfoMapper, CustomerInfoEntity> implements CustomerInfoService {

}
