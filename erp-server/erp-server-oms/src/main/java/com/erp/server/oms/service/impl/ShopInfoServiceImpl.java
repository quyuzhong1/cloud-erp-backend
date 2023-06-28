package com.erp.server.oms.service.impl;

import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.server.oms.mapper.ShopInfoMapper;
import com.erp.server.oms.service.ShopInfoService;
import com.common.business.service.SuperServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 店铺表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-06-28
 */
@Service
public class ShopInfoServiceImpl extends SuperServiceImpl<ShopInfoMapper, ShopInfoEntity> implements ShopInfoService {

}
