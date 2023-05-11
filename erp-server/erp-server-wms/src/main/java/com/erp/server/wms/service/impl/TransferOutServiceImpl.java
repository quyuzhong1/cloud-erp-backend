package com.erp.server.wms.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.erp.model.wms.entity.TransferOutEntity;
import com.erp.server.wms.mapper.TransferOutMapper;
import com.erp.server.wms.service.TransferOutService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 分布式调出单 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Service
public class TransferOutServiceImpl extends SuperServiceImpl<TransferOutMapper, TransferOutEntity> implements TransferOutService {

}
