package com.erp.server.oms.service.impl;

import com.erp.model.oms.entity.ListingInfoEntity;
import com.erp.server.oms.mapper.ListingInfoMapper;
import com.erp.server.oms.service.ListingInfoService;
import com.common.business.service.SuperServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 对应平台sku 表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-08-18
 */
@Service
public class ListingInfoServiceImpl extends SuperServiceImpl<ListingInfoMapper, ListingInfoEntity> implements ListingInfoService {

}
