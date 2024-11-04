package com.erp.rpc.oms.feign;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.AmazonTokenUpdateDTO;
import com.erp.model.oms.dto.ShopDTO;
import com.erp.model.oms.dto.ShopInfoDTO;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "erp-oms", contextId = "ShopInfo")
public interface ShopInfoFeign {

    /**
     * 获取店铺列表
     *
     * @return
     */
    @GetMapping("feign/shop/list")
    ApiResult<List<ShopInfoEntity>> list();

    /**
     * 更新店铺信息
     *
     * @param shopInfoEntity
     * @return
     */
    @PostMapping("feign/shop/updateShopInfoById")
    Boolean updateShopInfoById(@RequestBody ShopInfoEntity shopInfoEntity);
    /**
     * 获取店铺授权列表
     *
     * @param platformType
     * @return
     */
    @PostMapping("/feign/shop/getAuthShopByPlatformType")
    ApiResult<List<ShopAuthEntity>> getAuthShopByPlatformType(@RequestParam("platformType") String platformType);

    /**
     * 通过ID查询店铺信息
     */
    @GetMapping("feign/shop/getShopInfoById")
    ShopInfoEntity getShopInfoById(@RequestParam("id") String id);

    /**
     * 根据店铺id查询店铺信息
     * @Author Luo_WG
     * @Date 2023/11/1 12:09
     * @param ids
     * @return com.erp.model.oms.entity.ShopInfoEntity
     **/
    @PostMapping("feign/shop/listShopInfoByIds")
    List<ShopInfoEntity> listShopInfoByIds(@RequestBody List<String> ids);

    /**
     * 根据仓库id查询店铺
     * @Author Luo_WG
     * @Date 2023/11/23 16:03
     * @param warehouseIds
     * @return java.util.List<com.erp.model.oms.entity.ShopInfoEntity>
     **/
    @PostMapping("feign/shop/listShopInfoByWarehouseIds")
    List<ShopInfoEntity> listShopInfoByWarehouseIds(@RequestBody List<String> warehouseIds);


    /**
     * 获取授权信息
     * @param shopId
     * @return
     */
    @PostMapping("feign/shop/getShopAuthByShopId")
    ShopAuthEntity getShopAuthByShopId(String shopId);

    /**
     * 更新店铺授权信息
     *
     * @param shopAuthEntity
     * @return
     */
    @PostMapping("feign/shop/updateShopAuthById")
    Boolean updateShopAuthById(@RequestBody ShopAuthEntity shopAuthEntity);


    /**
     * 查询店铺关联的国家店铺
     *
     */
    @PostMapping("feign/shop/getRelatedShopByIdAndCountry")
    ShopInfoEntity getRelatedShopByIdAndCountry(@RequestBody ShopInfoDTO.RelatedDTO relateDTO);


    /**
     * 查询指定或所有店铺
     */
    @PostMapping("feign/shop/listByParams")
    List<ShopInfoEntity> listByParams(@RequestBody ShopInfoDTO.ListParamDTO dto);

    /**
     * 批量更新店铺授权信息
     *
     */
    @PostMapping("feign/shop/batchUpdateShopAuthById")
    Boolean batchUpdateShopAuthById(@RequestBody List<ShopAuthEntity> shopAuthEntity);

    /**
     * 批量获取授权信息
     */
    @PostMapping("feign/shop/listShopAuthByShopIds")
    List<ShopAuthEntity> listShopAuthByShopIds(@RequestBody List<String> shopIdList);


    /**
     * 根据店铺获取所有同账号的店铺
     */
    @PostMapping("feign/shop/getRelatedShopById")
    List<ShopInfoEntity> getRelatedShopById(@RequestBody ShopInfoEntity shopInfo);


    /**
     * 根据店铺ID获取所有同账号的店铺
     */
    @GetMapping("feign/shop/getRelatedByShopId")
    List<ShopInfoEntity> getRelatedByShopId(@RequestParam("shopId") String shopId);

    /**
     * 获取店铺--showByAuth true已授权 false所有数据
     *
     * @return ApiResult<List < ShopInfoEntity>>
     * @author hyj
     */
    @PostMapping("feign/shop/pagingSelect")
    PagingVO<ShopDTO.ListDTO> pagingSelect(@RequestBody @Validated PagingDTO<ShopDTO.SelectDTO> dto);

    /**
     * 检查和更新亚马逊同账号店铺授权
     */
    @PostMapping("feign/shop/checkAndSaveAllAmazonToken")
    Boolean checkAndSaveAllAmazonToken(@RequestBody @Validated AmazonTokenUpdateDTO updateDTO);
}
