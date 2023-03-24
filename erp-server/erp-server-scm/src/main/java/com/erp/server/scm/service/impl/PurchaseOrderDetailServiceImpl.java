package com.erp.server.scm.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.scm.dto.PurchaseOrderDetailDTO;
import com.erp.model.scm.entity.PurchaseOrderDetailEntity;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.scm.mapper.PurchaseOrderDetailMapper;
import com.erp.server.scm.service.PurchaseOrderDetailService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author will
 * @since 2023-03-16
 */
@Service
public class PurchaseOrderDetailServiceImpl extends SuperServiceImpl<PurchaseOrderDetailMapper, PurchaseOrderDetailEntity> implements PurchaseOrderDetailService {

    @Resource
    private WmsTaskFeign wmsTaskFeign;

    @Override
    public void add(List<PurchaseOrderDetailDTO.AddDTO> details, String purchaseOrderId) {
        if (CollectionUtils.isEmpty(details)) {
            return;
        }
        List<PurchaseOrderDetailEntity> list = BeanMapperUtils.copyList(PurchaseOrderDetailEntity.class, details);
        doOpHandleDataId(list,purchaseOrderId);
        this.saveBatch(list);

    }

    /**
     * 处理明细中的数据id
     */
    private void doOpHandleDataId (List<PurchaseOrderDetailEntity> newList, String purchaseApplicationId) {
    }
}
