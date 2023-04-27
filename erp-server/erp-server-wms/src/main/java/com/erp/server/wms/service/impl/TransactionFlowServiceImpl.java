package com.erp.server.wms.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.erp.model.wms.entity.TransactionFlowEntity;
import com.erp.server.wms.mapper.TransactionFlowMapper;
import com.erp.server.wms.service.TransactionFlowService;
import org.springframework.stereotype.Service;

/**
 * @Classname: TransactionFlowServiceImpl
 * @Description: TODO
 * @CreateTime: 2023-04-25  19:43
 * @Author: zhangchunlin
 */
@Service
public class TransactionFlowServiceImpl extends SuperServiceImpl<TransactionFlowMapper, TransactionFlowEntity> implements TransactionFlowService {
}