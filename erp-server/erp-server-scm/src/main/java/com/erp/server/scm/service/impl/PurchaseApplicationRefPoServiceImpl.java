package com.erp.server.scm.service.impl;

import com.common.core.serveice.SuperServiceImpl;
import com.erp.model.scm.entity.PurchaseApplicationRefPoEntity;
import com.erp.server.scm.mapper.PurchaseApplicationRefPoMapper;
import com.erp.server.scm.service.PurchaseApplicationRefPoService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 采购申请单和采购订单关联表 服务实现类
 * </p>
 *
 * @author will
 * @since 2023-03-16
 */
@Service
public class PurchaseApplicationRefPoServiceImpl extends SuperServiceImpl<PurchaseApplicationRefPoMapper, PurchaseApplicationRefPoEntity> implements PurchaseApplicationRefPoService {

}
