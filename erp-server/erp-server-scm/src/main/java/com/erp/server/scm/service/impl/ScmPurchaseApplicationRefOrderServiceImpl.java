package com.erp.server.scm.service.impl;

import com.common.core.serveice.SuperServiceImpl;
import com.erp.model.scm.entity.ScmPurchaseApplicationRefOrderEntity;
import com.erp.server.scm.mapper.ScmPurchaseApplicationRefOrderMapper;
import com.erp.server.scm.service.ScmPurchaseApplicationRefOrderService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 采购申请单和采购订单关联表 服务实现类
 * </p>
 *
 * @author will
 * @since 2023-03-15
 */
@Service
public class ScmPurchaseApplicationRefOrderServiceImpl extends SuperServiceImpl<ScmPurchaseApplicationRefOrderMapper, ScmPurchaseApplicationRefOrderEntity> implements ScmPurchaseApplicationRefOrderService {

}
