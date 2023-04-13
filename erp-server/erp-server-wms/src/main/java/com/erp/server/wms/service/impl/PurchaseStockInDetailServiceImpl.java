package com.erp.server.wms.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.erp.model.wms.dto.PurchaseStockInDetailDTO;
import com.erp.model.wms.entity.PurchaseStockInDetailEntity;
import com.erp.server.wms.mapper.PurchaseStorageDetailMapper;
import com.erp.server.wms.service.PurchaseStockInDetailService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 采购入库明细表 服务实现类
 * </p>
 *
 * @author will
 * @since 2023-04-10
 */
@Service
public class PurchaseStockInDetailServiceImpl extends SuperServiceImpl<PurchaseStorageDetailMapper, PurchaseStockInDetailEntity> implements PurchaseStockInDetailService {

    @Override
    public void add(List<PurchaseStockInDetailDTO.AddDTO> details, String id) {

    }
}
