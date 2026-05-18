package com.erp.server.wms.service.impl;

import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.wms.dto.AfterSalesWarehouseLocationSuggestDto;
import com.erp.model.wms.entity.AfterSalesFullBoxTransferDetailEntity;
import com.erp.server.wms.mapper.AfterSalesFullBoxTransferDetailMapper;
import com.erp.server.wms.service.AfterSalesFullBoxTransferDetailService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 整箱移仓箱唛明细表 服务实现
 *
 * @author liuchao
 * @since 2026-05-18
 */
@Service
public class AfterSalesFullBoxTransferDetailServiceImpl
        extends SuperServiceImpl<AfterSalesFullBoxTransferDetailMapper, AfterSalesFullBoxTransferDetailEntity>
        implements AfterSalesFullBoxTransferDetailService {

    @Override
    public List<AfterSalesWarehouseLocationSuggestDto.BoxMoveDetailListDto> listBoxMoveDetail(String mainId) {
        List<AfterSalesFullBoxTransferDetailEntity> entityList = lambdaQuery()
                .eq(AfterSalesFullBoxTransferDetailEntity::getMainId, mainId)
                .orderByAsc(AfterSalesFullBoxTransferDetailEntity::getSkuNo)
                .list();
        return BeanMapperUtils.copyList(AfterSalesWarehouseLocationSuggestDto.BoxMoveDetailListDto.class, entityList);
    }
}
