package com.erp.server.scm.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.erp.model.scm.entity.PurchasePriceEntity;
import com.erp.server.scm.mapper.PurchasePriceMapper;
import com.erp.server.scm.service.PurchasePriceService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 采购价目表 服务实现类
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
@Service
public class PurchasePriceServiceImpl extends SuperServiceImpl<PurchasePriceMapper, PurchasePriceEntity> implements PurchasePriceService {

}
