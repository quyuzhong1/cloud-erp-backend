package com.erp.server.wms.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.erp.model.wms.entity.PurchaseStorageEntity;
import com.erp.server.wms.mapper.PurchaseStorageMapper;
import com.erp.server.wms.service.PurchaseStorageService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 采购入库单 服务实现类
 * </p>
 *
 * @author will
 * @since 2023-04-10
 */
@Service
public class PurchaseStorageServiceImpl extends SuperServiceImpl<PurchaseStorageMapper, PurchaseStorageEntity> implements PurchaseStorageService {

}
