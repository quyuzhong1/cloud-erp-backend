package com.erp.server.oms.service;

import com.common.business.service.SuperService;
import com.erp.model.oms.dto.ShopAuthDTO;
import com.erp.model.oms.dto.ShopAuthorizeDTO;
import com.erp.model.oms.entity.ShopAuthEntity;

import java.util.List;

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


    /**
     * 获取到授权信息 根据店铺id
     * @author yl
     * @date 2023-08-29 16:15
     * @param shopId
     * @return com.erp.model.oms.entity.ShopAuthEntity
     */
    ShopAuthEntity getByShopId(String shopId);

    /**
     * 删除根据店铺id
     * @author yl
     * @date 2023-08-29 16:48
     * @param id
     * @return void
     */
    void removeByShopId(String id);

    /**
     * 获取虾皮授权链接
     * @return
     */
    String getShopeeCodeUrl(ShopAuthorizeDTO dto);

    List<ShopAuthEntity> getShopeeShopList(String type);

    ShopAuthEntity getShopeeShopById(String shopeeId);

    void updateShopeeToken(ShopAuthEntity shopAuthEntity);

    void getProductAll();
    void getOrderAll();
}
