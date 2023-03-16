package com.erp.server.scm.service.impl;

import com.common.core.serveice.SuperServiceImpl;
import com.erp.model.scm.entity.ScmPurchaseOrderEntity;
import com.erp.server.scm.mapper.ScmPurchaseOrderMapper;
import com.erp.server.scm.service.ScmPurchaseOrderService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 采购订单表 服务实现类
 * </p>
 *
 * @author will
 * @since 2023-03-16
 */
@Service
public class ScmPurchaseOrderServiceImpl extends SuperServiceImpl<ScmPurchaseOrderMapper, ScmPurchaseOrderEntity> implements ScmPurchaseOrderService {

}
