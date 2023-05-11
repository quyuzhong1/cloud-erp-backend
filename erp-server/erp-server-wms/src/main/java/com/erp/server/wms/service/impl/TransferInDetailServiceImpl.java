package com.erp.server.wms.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.erp.model.wms.entity.TransferInDetailEntity;
import com.erp.server.wms.mapper.TransferInDetailMapper;
import com.erp.server.wms.service.TransferInDetailService;
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
public class TransferInDetailServiceImpl extends SuperServiceImpl<TransferInDetailMapper, TransferInDetailEntity> implements TransferInDetailService {

}
