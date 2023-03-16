package com.erp.server.scm.service.impl;

import com.common.core.serveice.SuperServiceImpl;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.server.scm.mapper.SupplierMapper;
import com.erp.server.scm.service.SupplierService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 供应商表 服务实现类
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
@Service
public class SupplierServiceImpl extends SuperServiceImpl<SupplierMapper, SupplierEntity> implements SupplierService {

}
