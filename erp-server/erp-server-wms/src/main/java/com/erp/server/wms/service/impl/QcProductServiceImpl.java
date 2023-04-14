package com.erp.server.wms.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.erp.model.wms.entity.QcProductEntity;
import com.erp.server.wms.mapper.QcProductMapper;
import com.erp.server.wms.service.QcProductService;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-04-14
 */
@Service
public class QcProductServiceImpl extends SuperServiceImpl<QcProductMapper, QcProductEntity> implements QcProductService {

}
