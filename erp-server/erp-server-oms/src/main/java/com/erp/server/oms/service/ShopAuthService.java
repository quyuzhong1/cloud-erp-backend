package com.erp.server.oms.service;

import com.common.business.service.SuperService;
import com.erp.model.oms.dto.ShopAuthDTO;
import com.erp.model.oms.dto.ShopAuthorizeDTO;
import com.erp.model.oms.dto.ShopAuthorizeUrlDTO;
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
     *
     * @param dto
     * @return
     * @author Lambda
     * @date: 2023-08-28
     */
    String add(ShopAuthDTO.AddDTO dto);

    /**
     * 修改
     *
     * @param dto
     * @return
     * @author Lambda
     * @date: 2023-08-28
     */
    Boolean update(ShopAuthDTO.UpdateDTO dto);


    /**
     * 获取到授权信息 根据店铺id
     *
     * @param shopId
     * @return com.erp.model.oms.entity.ShopAuthEntity
     * @author yl
     * @date 2023-08-29 16:15
     */
    ShopAuthEntity getByShopId(String shopId);

    /**
     * 删除根据店铺id
     *
     * @param id
     * @return void
     * @author yl
     * @date 2023-08-29 16:48
     */
    void removeByShopId(String id);

    /**
     * 获取虾皮授权链接
     *
     * @return
     */
    String getShopeeCodeUrl(ShopAuthorizeUrlDTO dto);

    List<ShopAuthEntity> getShopeeShopList(String type, String stauts);

    /**
     * 根据店铺类型获取授权列表
     *
     * @param platformType
     * @return
     */
    List<ShopAuthEntity> getAuthShopByPlatformType(String platformType);

    ShopAuthEntity getShopeeShopById(String shopeeId);

    void updateShopeeToken(ShopAuthEntity shopAuthEntity);

    void getProductAll();

    void getOrderAll();
}
