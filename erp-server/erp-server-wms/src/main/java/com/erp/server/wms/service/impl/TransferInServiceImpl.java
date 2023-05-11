package com.erp.server.wms.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.erp.model.wms.entity.TransferInEntity;
import com.erp.server.wms.mapper.TransferInMapper;
import com.erp.server.wms.service.TransferInService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 分布式调入单 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Service
public class TransferInServiceImpl extends SuperServiceImpl<TransferInMapper, TransferInEntity> implements TransferInService {

}
