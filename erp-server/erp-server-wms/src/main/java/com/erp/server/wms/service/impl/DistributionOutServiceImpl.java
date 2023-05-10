package com.erp.server.wms.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.erp.model.wms.entity.DistributionOutEntity;
import com.erp.server.wms.mapper.DistributionOutMapper;
import com.erp.server.wms.service.DistributionOutService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 分布式调出单 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Service
public class DistributionOutServiceImpl extends SuperServiceImpl<DistributionOutMapper, DistributionOutEntity> implements DistributionOutService {

}
