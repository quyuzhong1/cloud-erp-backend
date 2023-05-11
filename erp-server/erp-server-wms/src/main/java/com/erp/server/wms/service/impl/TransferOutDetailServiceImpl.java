package com.erp.server.wms.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.erp.model.wms.entity.TransferOutDetailEntity;
import com.erp.server.wms.mapper.TransferOutDetailMapper;
import com.erp.server.wms.service.TransferOutDetailService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 分布式调出单明细 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Service
public class TransferOutDetailServiceImpl extends SuperServiceImpl<TransferOutDetailMapper, TransferOutDetailEntity> implements TransferOutDetailService {

}
