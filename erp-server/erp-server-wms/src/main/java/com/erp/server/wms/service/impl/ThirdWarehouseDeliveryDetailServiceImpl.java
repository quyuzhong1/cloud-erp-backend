package com.erp.server.wms.service.impl;


import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.scm.entity.SubcontractOrderDetailEntity;
import com.erp.model.wms.entity.ThirdWarehouseDeliveryDetailEntity;
import com.erp.server.wms.mapper.ThirdWarehouseDeliveryDetailMapper;
import com.erp.server.wms.service.ThirdWarehouseDeliveryDetailService;
import io.seata.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * 三方仓发货单明细 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2024-10-17
 */
@Slf4j
@Service
public class ThirdWarehouseDeliveryDetailServiceImpl extends SuperServiceImpl<ThirdWarehouseDeliveryDetailMapper, ThirdWarehouseDeliveryDetailEntity> implements ThirdWarehouseDeliveryDetailService {

    @Override
    public List<ThirdWarehouseDeliveryDetailEntity> listByMainId(String mainId) {
        if(StringUtils.isBlank(mainId)){
            return new ArrayList<>();
        }
        return lambdaQuery().eq(ThirdWarehouseDeliveryDetailEntity::getMainId,mainId).list();
    }

    @Override
    public void removeByMainIds(List<String> mainIds) {
        lambdaUpdate()
                .in(ThirdWarehouseDeliveryDetailEntity::getMainId,mainIds)
                .remove();
    }
}
