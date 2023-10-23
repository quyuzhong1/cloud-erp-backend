package com.erp.server.oms.service;

import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.ShopAuthorizeDTO;
import com.erp.model.oms.dto.ShopAuthDTO;
import com.erp.model.oms.dto.ShopDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.sdk.oms.shopee.dto.base.ShopeeTokenAuth;

import java.util.List;

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
     *
     * @param dto
     * @return java.lang.String
     * @author yl
     * @date 2023-06-29 10:39
     */
    Boolean add(ShopDTO.AddDTO dto);

    /**
     * 修改店铺
     *
     * @param dto
     * @return java.lang.String
     * @author yl
     * @date 2023-07-03 9:05
     */
    String updateShop(ShopDTO.UpdateDTO dto);


    /**
     * 店铺分页
     *
     * @param dto
     * @return
     */
    PagingVO<ShopDTO.PagingViewDTO> paging(PagingDTO<ShopDTO.PagingParamDTO> dto);

    /**
     * 批量启用或者禁用店铺
     *
     * @param shop     店铺信息
     * @param disabled 禁用状态
     * @return
     */
    BatchResultDTO updateStatus(ShopInfoEntity shop, Boolean disabled);

    /**
     * 获取详情
     *
     * @param id
     * @return com.erp.model.oms.dto.ShopDTO.ViewDTO
     * @author yl
     * @date 2023-08-22 16:13
     */
    ShopDTO.ViewDTO view(String id);

    /**
     * 更新店铺信息
     *
     * @param shopInfoEntity
     * @return
     */
    Boolean updateShopInfoById(ShopInfoEntity shopInfoEntity);

    /**
     * 店铺授权
     * @param dto
     * @return
     */
    Boolean shopAuthorize(ShopAuthorizeDTO dto);

    /**
     * 取消授权
     *
     * @param id
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-08-29 16:41
     */
    Boolean cancelAuthorize(String id);

    /**
     * 根据域名来查询
     *
     * @param shopDomain
     * @return com.erp.model.oms.entity.ShopInfoEntity
     * @author yl
     * @date 2023-08-29 18:08
     */
    ShopInfoEntity getByDomain(String shopDomain);

    /**
     * 店铺账号下拉
     *
     * @param
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2023-08-30 16:43
     */
    List<String> accountList();


    /**
     * 获取到安装的url
     *
     * @param
     * @return java.lang.String
     * @author yl
     * @date 2023-09-06 16:34
     */
    String getShopifyInstallUrl(String id);


    /**
     * 获取到ShopifyAuthorizeUrl 授权的url
     *
     * @param hmac
     * @param host
     * @param shop
     * @param timestamp
     * @return java.lang.String
     * @author yl
     * @date 2023-09-06 16:54
     */
    String getShopifyAuthorizeUrl(String hmac, String host, String shop, String timestamp);

    /**
     * @return List<ListTreeDTO>
     * @description: 获取店铺列表
     * @author Will
     * @date: 2023/9/7 16:31
     */
    List<ShopDTO.ListTreeDTO> listTree();

    /**
     * 检查店铺是否授权
     *
     * @param id
     * @return
     */
    Boolean checkShopIsAuthorize(String id);

    /**
     * @return List<ShopInfoEntity>
     * @description: 获取已授权店铺
     * @author Will
     * @date: 2023/10/18 10:05
     */
    List<ShopInfoEntity> listAuth(ShopDTO.PlatformDTO platformDTO);

    /**
     * 获取虾皮授权回调
     *
     * @param dto
     * @return
     */
    Boolean getShopeeReturn(ShopAuthDTO.ReturnDTO dto);

    /**
     *  更新授权信息
     * @param shopeeTokenAuth
     * @param type
     * @param shopeeId
     * @param shopInfo
     * @param cfClientId
     */
    void saveOrUpdateShopee(ShopeeTokenAuth shopeeTokenAuth, String type, String shopeeId, ShopInfoEntity shopInfo, String cfClientId);
}
