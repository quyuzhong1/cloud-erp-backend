package com.erp.server.oms.service;

import com.erp.model.oms.dto.ShopDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.common.business.service.SuperService;

/**
 * <p>
 * 店铺表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-06-28
 */
public interface ShopInfoService extends SuperService<ShopInfoEntity> {

    
    /**
     * 添加店铺
     * @author yl
     * @date 2023-06-29 10:39
     * @param dto
     * @return java.lang.String
     */
    String add(ShopDTO.AddDTO dto);
}
