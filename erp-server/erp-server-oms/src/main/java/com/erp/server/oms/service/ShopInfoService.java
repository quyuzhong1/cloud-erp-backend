package com.erp.server.oms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.common.business.dto.AdvanceQueryContainer;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BaseDropDownDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.*;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.sdk.oms.shopee.dto.base.ShopeeTokenAuth;
import com.sdk.oms.shopify.api.dto.AssociatedUserBean;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
    List<ShopInfoEntity> add(ShopDTO.AddDTO dto);

    /**
     * 修改店铺
     *
     * @param dto
     * @return java.lang.String
     * @author yl
     * @date 2023-07-03 9:05
     */
    ShopInfoEntity updateShop(ShopDTO.UpdateDTO dto);


    /**
     * 修改店铺
     *
     * @param dto
     * @return java.lang.String
     * @author yl
     * @date 2023-07-03 9:05
     */
    ShopInfoEntity updateInternalShop(ShopDTO.UpdateInternalDTO dto);


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
    Boolean shopAuthorize(ShopAuthorizeDTO dto, HttpServletResponse response);

    /**
     * 取消授权
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-08-29 16:41
     */
    Boolean cancelAuthorize(CancelAuthorizeDTO dto);

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
     * 获取店铺授权地址
     * @param dto
     * @return
     */
    String getShopAuthorizeUrl(ShopAuthorizeUrlDTO dto);

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
    Boolean saveOrUpdateShopee(ShopeeTokenAuth shopeeTokenAuth, String type, String shopeeId, ShopInfoEntity shopInfo, String cfClientId);

    /**
     * 查询亚马逊店铺信息
     * @Author Luo_WG
     * @Date 2023/11/1 18:56
     * @return java.util.List<com.erp.model.oms.entity.ShopInfoEntity>
     **/
    List<ShopInfoEntity> listShopByAmazon();

    List<ShopSysUserAuthDTO.ViewShopDTO> listShopByAmazonAuth();
    /**
     * 根据条件查询是否存在店铺
     *
     * @author Jim
     * @since 2023-11-09
     */
    boolean checkExist(String dictCountryCode, String dictPlatform, String authStatus);

    /**
     * 根据仓库id查询店铺
     * @Author Luo_WG
     * @Date 2023/11/23 16:03
     * @param warehouseIds
     * @return java.util.List<com.erp.model.oms.entity.ShopInfoEntity>
     **/
    List<ShopInfoEntity> listShopInfoByWarehouseIds(List<String> warehouseIds);

    /**
     * 添加并授权店铺
     */
    ShopDTO.RedirectDTO addAndAuth(ShopDTO.AddDTO dto);

    /**
     * 添加亚马逊店铺
     * @param dto
     * @return
     */
    List<ShopInfoEntity> handleAmazonShop(ShopDTO.AddDTO dto);


    /**
     * 更新亚马逊店铺
     * @param dto
     * @return
     */
    ShopDTO.RedirectDTO updateAndAuth(ShopDTO.UpdateDTO dto);

    /**
     * 查询店铺关联的国家店铺
     * @return
     */
    ShopInfoEntity getRelatedShopByIdAndCountry(ShopInfoDTO.RelatedDTO relateDTO);

    CustomerInfoEntity autoCreateShopCustomer(String id);

    /**
     * Shopify直接安装授权Url
     * @param dto
     * @return
     */
    String getShopifyAuthorizeUrl(ShopifyAuthorizeUrlDTO dto);

    /**
     * 查询指定或所有店铺
     */
    List<ShopInfoEntity> listByParams(ShopInfoDTO.ListParamDTO dto);

    /**
     * 根据shopify平台用户id查询用户信息
     * @Author Luo_WG
     * @Date 2024/2/23 14:07
     * @param id
     * @return com.common.core.controller.vo.ApiResult
     **/
    AssociatedUserBean getShopifyShopByUserId(String id);

    /**
     * 请求查看存储的客户数据
     * @Author Luo_WG
     * @Date 2024/2/23 15:53
     * @param dto
     **/
    void customersDataRequest(ShopifyWebhookDTO.CustomersDataRequestDTO dto, HttpServletResponse response, HttpServletRequest request);

    /**
     * 要求删除客户数据
     * @Author Luo_WG
     * @Date 2024/2/23 14:07
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    void customersRedact(ShopifyWebhookDTO.CustomersRedactDTO dto, HttpServletResponse response, HttpServletRequest request);

    /**
     * 要求删除店铺数据
     * @Author Luo_WG
     * @Date 2024/2/23 14:07
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    void shopRedact(ShopifyWebhookDTO.ShopRedactDTO dto, HttpServletResponse response, HttpServletRequest request);

    /**
     * 测试
     * @Author Luo_WG
     * @Date 2024/2/29 15:52
     * @param data
     * @return void
     **/
    ResponseEntity<String> shopRedactTest(String data, HttpServletResponse response, HttpServletRequest request);

    /**
     * 店铺下拉
     * @return
     */
    List<BaseDropDownDTO.DisabledDTO> listShopSelect();


    /**
     * 查询亚马逊同账号所有店铺
     * @Author Jim
     * @Date 2024/03/28
     **/
    List<ShopInfoEntity> getRelatedShopById(String platformShopCode);

    List<BatchResultDTO> deleteByIds(BaseIdsDTO.IdsDTO dto);

    void listExport(ShopDTO.ExportDTO dto);


    /**
     * 添加国内店铺
     *
     * @param dto
     * @return java.lang.String
     * @author hyj
     * @date 2024-05-23 16:39
     */
    List<ShopInfoEntity> addIntenal(ShopDTO.AddInternalDTO dto);

    /**
     * 远程搜索
     * @param dto
     * @return
     */
    PagingVO<ShopDTO.ListDTO> pagingSelect(PagingDTO<ShopDTO.SelectDTO> dto);

    /**
     * 导出店铺
     */
    PagingVO<ShopDTO.PagingViewDTO> exportShop(PagingDTO<ShopDTO.ExportDTO> dto);

    PagingVO<SkuMappingDTO.SyncPlatformProductView> pageAuthShop(PagingDTO<AdvanceQueryContainer> advanceQueryDTO, List<String> shopIds);
    /**
     * 区域远程搜索
     * @author will
     * @date 2024/8/28 17:15
     * @param dto
     * @return PagingVO<AreaDTO>
     */
    PagingVO<ShopDTO.AreaDTO> pagingSelectArea(PagingDTO<ShopDTO.AreaParamDTO> dto);
    /**
     * 店铺下拉
     * @author will
     * @date 2024/8/28 18:28
     * @param dto
     * @return List<ListDTO>
     */
    List<ShopDTO.ListDTO> listSelect(ShopDTO.SelectDTO dto);

    /**
     * 根据平台获取店铺
     * @param platform 平台
     */
    List<String> listShopInfoByPlatform(String platform);
    void saveCustom(ShopInfoEntity shopInfoEntity);

    /**
     * 检查和更新亚马逊同账号店铺授权
     */
    Boolean checkAndSaveAllAmazonToken(AmazonTokenUpdateDTO updateDTO);
}
