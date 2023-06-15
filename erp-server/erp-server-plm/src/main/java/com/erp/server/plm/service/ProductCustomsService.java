package com.erp.server.plm.service;
import com.erp.model.plm.entity.ProductCustomsEntity;
import com.common.business.service.SuperService;

import java.util.List;


/**
 * <p>
 *  目的国海关编码服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-06-12
 */
public interface ProductCustomsService extends SuperService<ProductCustomsEntity> {


    /**
     * 根据产品id查询目的国海关编码
     * @Author Luo_WG
     * @Date 2023/6/15 16:51
     * @param productId
     * @return java.util.List<com.erp.model.plm.entity.ProductCustomsEntity>
     **/
    List<ProductCustomsEntity> listByProductId(String productId);
}
