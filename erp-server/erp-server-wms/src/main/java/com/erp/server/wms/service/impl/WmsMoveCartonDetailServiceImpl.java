package com.erp.server.wms.service.impl;

import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.wms.dto.AfterSalesWarehouseLocationSuggestDto;
import com.erp.model.wms.entity.WmsMoveCartonDetailEntity;
import com.erp.server.wms.mapper.WmsMoveCartonDetailMapper;
import com.erp.server.wms.service.WmsMoveCartonDetailService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 仓位移动箱唛明细表 服务实现
 *
 * @author liuchao
 * @since 2026-05-18
 */
@Service
public class WmsMoveCartonDetailServiceImpl
        extends SuperServiceImpl<WmsMoveCartonDetailMapper, WmsMoveCartonDetailEntity>
        implements WmsMoveCartonDetailService {

    @Override
    public List<AfterSalesWarehouseLocationSuggestDto.BoxMoveDetailListDto> listBoxMoveDetail(String mainId) {
        List<WmsMoveCartonDetailEntity> entityList = lambdaQuery()
                .eq(WmsMoveCartonDetailEntity::getMainId, mainId)
                .orderByAsc(WmsMoveCartonDetailEntity::getSkuNo)
                .list();
        return BeanMapperUtils.copyList(AfterSalesWarehouseLocationSuggestDto.BoxMoveDetailListDto.class, entityList);
    }
}
