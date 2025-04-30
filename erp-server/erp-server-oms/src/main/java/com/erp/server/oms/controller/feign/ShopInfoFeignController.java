package com.erp.server.oms.controller.feign;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.dto.AmazonTokenUpdateDTO;
import com.erp.model.oms.dto.ShopDTO;
import com.erp.model.oms.dto.ShopInfoDTO;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.server.oms.query.ShopQueryHandler;
import com.erp.server.oms.service.ShopAuthService;
import com.erp.server.oms.service.ShopInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
 * 店铺管理
 *
 * @author Lambda
 * @since 2023-06-28
 */
@Slf4j
@RestController
@RequestMapping("/feign/shop")
public class ShopInfoFeignController extends BaseController {

    @Resource
    private ShopInfoService shopInfoService;
    @Resource
    private ShopAuthService shopAuthService;
    /**
     * 获取店铺列表
     *
     * @return
     */
    @GetMapping("/list")
    public ApiResult<List<ShopInfoEntity>> list() {
        List<ShopInfoEntity> list = shopInfoService.list();
        return success(list);
    }

    /**
     * 获取店铺授权列表
     *
     * @return
     */
    @PostMapping("/getAuthShopByPlatformType")
    public ApiResult<List<ShopAuthEntity>> getAuthShopByPlatformType(@RequestParam("platformType") String platformType) {
        List<ShopAuthEntity> list = shopAuthService.getAuthShopByPlatformType(platformType);
        return success(list);
    }
    /**
     * 更新店铺信息
     *
     * @param shopInfoEntity
     * @return
     */
    @PostMapping("/updateShopInfoById")
    Boolean updateShopInfoById(@RequestBody ShopInfoEntity shopInfoEntity){
        return shopInfoService.updateShopInfoById(shopInfoEntity);
    }

    /**
     * 通过ID查询店铺信息
     */
    @GetMapping("/getShopInfoById")
    public ShopInfoEntity getShopInfoById(@RequestParam("id") String id) {
        return shopInfoService.getById(id);
    }

    /**
     * 根据店铺id查询店铺信息
     * @Author Luo_WG
     * @Date 2023/11/1 12:09
     * @param ids
     * @return com.erp.model.oms.entity.ShopInfoEntity
     **/
    @PostMapping("/listShopInfoByIds")
    public List<ShopInfoEntity> listShopInfoByIds(@RequestBody List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<ShopInfoEntity> shopInfoEntityList = shopInfoService.listByIds(ids);
        List<ShopAuthEntity> shopAuthEntityList = shopAuthService.listShopAuthByShopIds(ids);
        shopInfoEntityList.forEach(v->{
            ShopAuthEntity shopAuthEntity = shopAuthEntityList.stream().filter(v1 -> v1.getShopId().equals(v.getId())).findFirst().orElse(null);
            if (shopAuthEntity != null) {
                v.setAccessToken(shopAuthEntity.getAccessToken());
            }
        });
        return shopInfoEntityList;
    }

    /**
     * 根据仓库id查询店铺
     * @Author Luo_WG
     * @Date 2023/11/23 16:03
     * @param warehouseIds
     * @return java.util.List<com.erp.model.oms.entity.ShopInfoEntity>
     **/
    @PostMapping("/listShopInfoByWarehouseIds")
    public List<ShopInfoEntity> listShopInfoByWarehouseIds(@RequestBody List<String> warehouseIds) {
        return shopInfoService.listShopInfoByWarehouseIds(warehouseIds);
    }

    /**
     * 获取授权信息
     * @param shopId
     * @return
     */
    @PostMapping("/getShopAuthByShopId")
    public ShopAuthEntity getShopAuth(@RequestBody String shopId) {
        return shopAuthService.getByShopId(shopId);
    }

    /**
     * 更新店铺信息
     *
     * @param shopAuthEntity
     * @return
     */
    @PostMapping("/updateShopAuthById")
    public Boolean updateShopAuthById(@RequestBody ShopAuthEntity shopAuthEntity){
        return shopAuthService.updateShopAuthById(shopAuthEntity);
    }

    /**
     * 查询店铺关联的国家店铺
     *
     */
    @PostMapping("/getRelatedShopByIdAndCountry")
    public ShopInfoEntity getRelatedShopByIdAndCountry(@RequestBody ShopInfoDTO.RelatedDTO relateDTO){
        return shopInfoService.getRelatedShopByIdAndCountry(relateDTO);
    }


    /**
     * 查询指定或所有店铺
     */
    @PostMapping("/listByParams")
    public List<ShopInfoEntity> listByParams(@RequestBody ShopInfoDTO.ListParamDTO dto){
        return shopInfoService.listByParams(dto);
    }

    /**
     * 批量更新店铺授权信息
     *
     */
    @PostMapping("/batchUpdateShopAuthById")
    public Boolean batchUpdateShopAuthById(@RequestBody List<ShopAuthEntity> shopAuthEntity){
        return shopAuthService.batchUpdateShopAuthById(shopAuthEntity);
    }

    /**
     * 批量获取授权信息
     */
    @PostMapping("/listShopAuthByShopIds")
    public List<ShopAuthEntity> listShopAuthByShopIds(@RequestBody List<String> shopIdList){
        return shopAuthService.listShopAuthByShopIds(shopIdList);
    }

    /**
     * 根据店铺ID获取所有同账号的店铺
     */
    @PostMapping("/getRelatedShopById")
    public List<ShopInfoEntity> getRelatedShopById(@RequestBody ShopInfoEntity shopInfo){
        return shopInfoService.getRelatedShopById(shopInfo.getPlatformShopCode());
    }

    /**
     * 根据店铺ID获取所有同账号的店铺
     */
    @GetMapping("/getRelatedByShopId")
    public List<ShopInfoEntity> getRelatedByShopId(@RequestParam("shopId") String shopId){
        ShopInfoEntity shopInfo = shopInfoService.getById(shopId);
        if (null == shopInfo){
            throw new ServiceException("店铺不存在：id=" + shopId);
        }
        return shopInfoService.getRelatedShopById(shopInfo.getPlatformShopCode());
    }

    /**
     * 获取店铺--showByAuth true已授权 false所有数据
     *
     * @return ApiResult<List <ShopInfoEntity>>
     * @author hyj
     */
    @PostMapping("/pagingSelect")
    public PagingVO<ShopDTO.ListDTO> pagingSelect(@RequestBody @Validated PagingDTO<ShopDTO.SelectDTO> dto) {
        return shopInfoService.pagingSelect(dto);
    }

    /**
     * 检查和更新亚马逊同账号店铺授权
     */
    @PostMapping("/checkAndSaveAllAmazonToken")
    public Boolean checkAndSaveAllAmazonToken(@RequestBody @Validated AmazonTokenUpdateDTO updateDTO){
        return shopInfoService.checkAndSaveAllAmazonToken(updateDTO);
    }

    /**
     * 根据平台获取店铺
     * @param platform 平台
     */
    @GetMapping("/listShopInfoByPlatform")
    public List<String> listShopInfoByPlatform(@RequestParam String platform){
        return shopInfoService.listShopInfoByPlatform(platform);
    }

    /**
     * 获取店铺列表
     *
     * @return
     */
    @GetMapping("/getShopListByParam")
    public ApiResult<List<ShopAuthEntity>> getShopListByParam(@RequestParam(value = "type") String type,
                                                              @RequestParam(value = "status") String status,
                                                              @RequestParam(value = "dictPlatform") String dictPlatform) {
        return success(shopAuthService.getShopListByParam(type,status, dictPlatform));
    }

    /**
     * 获取商铺详情
     *
     * @return
     */
    @GetMapping("/getShopAuthById")
    public ApiResult<ShopAuthEntity> getShopAuthById(@RequestParam(value = "shopId") String shopId) {
        return success(shopAuthService.getByShopId(shopId));
    }

    /**
     * 高级查询分页店铺
     */
    @PostMapping("/paging")
//    @WebAdvanceQuery(handler = ShopQueryHandler.class)
    public PagingVO<ShopDTO.PagingViewDTO> paging(@RequestBody @Validated PagingDTO<ShopDTO.PagingParamDTO> dto) {
        return shopInfoService.paging(dto);
    }

    /**
     * 根据店铺名称查询店铺信息
     * @param shopNameList
     * @return
     */
    @PostMapping("/listShopByName")
    public List<ShopInfoDTO.ListDTO> listShopByName(@RequestBody List<String> shopNameList){
        return shopInfoService.listShopByName(shopNameList);
    }
}
