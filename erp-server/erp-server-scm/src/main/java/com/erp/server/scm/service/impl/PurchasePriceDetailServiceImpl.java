package com.erp.server.scm.service.impl;

import com.common.core.serveice.SuperServiceImpl;
import com.erp.model.scm.entity.PurchasePriceDetailEntity;
import com.erp.server.scm.mapper.ScmPurchasePriceDetailMapper;
import com.erp.server.scm.service.PurchasePriceDetailService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 产品采购价格明细表 服务实现类
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
@Service
public class PurchasePriceDetailServiceImpl extends SuperServiceImpl<ScmPurchasePriceDetailMapper, PurchasePriceDetailEntity> implements PurchasePriceDetailService {

}
