package com.erp.server.oms.service;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.oms.dto.ShopAuthDTO;

/**
 * <p>
 * 店铺授权表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-08-28
 */
public interface ShopAuthService extends SuperService<ShopAuthEntity> {

    /**
    * 新增
    * @author Lambda
    * @date: 2023-08-28
    * @param dto
    * @return
    */
    String add(ShopAuthDTO.AddDTO dto);

    /**
    * 修改
    * @author Lambda
    * @date: 2023-08-28
    * @param dto
    * @return
    */
    Boolean update(ShopAuthDTO.UpdateDTO dto);


}
