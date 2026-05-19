package com.erp.server.wms.service.impl;

import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.wms.dto.AfterSalesWarehouseLocationSuggestDto;
import com.erp.model.wms.entity.WmsMoveSrcDetailEntity;
import com.erp.server.wms.mapper.WmsMoveSrcDetailMapper;
import com.erp.server.wms.service.WmsMoveSrcDetailService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 仓位移动来源明细表 服务实现
 *
 * @author liuchao
 * @since 2026-05-18
 */
@Service
public class WmsMoveSrcDetailServiceImpl
        extends SuperServiceImpl<WmsMoveSrcDetailMapper, WmsMoveSrcDetailEntity>
        implements WmsMoveSrcDetailService {

    @Override
    public List<AfterSalesWarehouseLocationSuggestDto.BoxMoveDetailListDto> listBoxMoveDetail(String mainId) {
        List<WmsMoveSrcDetailEntity> entityList = lambdaQuery()
                .eq(WmsMoveSrcDetailEntity::getMainId, mainId)
                .orderByAsc(WmsMoveSrcDetailEntity::getSkuNo)
                .list();
        return BeanMapperUtils.copyList(AfterSalesWarehouseLocationSuggestDto.BoxMoveDetailListDto.class, entityList);
    }
}
