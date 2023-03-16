package com.erp.server.scm.service.impl;

import com.common.core.serveice.SuperServiceImpl;
import com.erp.model.scm.entity.SupplierContactEntity;
import com.erp.server.scm.mapper.ScmSupplierContactMapper;
import com.erp.server.scm.service.ScmSupplierContactService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 供应商联系人表 服务实现类
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
@Service
public class ScmSupplierContactServiceImpl extends SuperServiceImpl<ScmSupplierContactMapper, SupplierContactEntity> implements ScmSupplierContactService {

}
