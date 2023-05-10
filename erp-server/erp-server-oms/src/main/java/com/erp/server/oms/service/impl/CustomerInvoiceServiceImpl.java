package com.erp.server.oms.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.erp.model.oms.entity.CustomerInvoiceEntity;
import com.erp.server.oms.mapper.CustomerInvoiceMapper;
import com.erp.server.oms.service.CustomerInvoiceService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 客户发票信息 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Service
public class CustomerInvoiceServiceImpl extends SuperServiceImpl<CustomerInvoiceMapper, CustomerInvoiceEntity> implements CustomerInvoiceService {

}
