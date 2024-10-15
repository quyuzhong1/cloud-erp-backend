package com.erp.server.oms.service.impl;

import com.erp.model.oms.entity.RefundOrderEntity;
import com.erp.server.oms.mapper.RefundOrderMapper;
import com.erp.server.oms.service.RefundOrderService;
import com.common.business.service.impl.SuperServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 退款订单 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2024-09-12
 */
@Service
public class RefundOrderServiceImpl extends SuperServiceImpl<RefundOrderMapper, RefundOrderEntity> implements RefundOrderService {

}
