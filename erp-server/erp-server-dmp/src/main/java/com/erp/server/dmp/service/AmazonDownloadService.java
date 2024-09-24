package com.erp.server.dmp.service;

import com.erp.model.dmp.entity.CfgTimezoneEntity;
import com.erp.model.dmp.entity.PlatformApiTaskEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.wms.entity.CfgAmzFulfillmentCenterEntity;
import com.erp.sdk.oms.amz.spapi.dto.*;
import com.erp.sdk.oms.amz.spapi.enums.AmazonMarketplaceEnum;

import java.util.List;
import java.util.Map;

/**
 * 亚马逊下载服务
 *
 * @author Jim
 * @date 2024/6/14
 */
public interface AmazonDownloadService {


    /**
     * 处理订单详情下载
     */
    void handlerOrderDetailDownload(String key, List<PlatformApiTaskEntity> value, Integer size, String platform, String category, List<ShopInfoEntity> list);

    /**
     * 单处理订单详情下载
     */
    void singleHandlerOrderDetailDownload(String key, String platform, String category, PlatformAmazonOrderDTO dto);

    /**
     * 处理订单地址详情下载
     */
    void handlerAddressDetail(String key, List<PlatformApiTaskEntity> value, Integer size, String platform, String category, String business, List<ShopInfoEntity> list);

    /**
     * 转换所有同账号关联店铺ID
     */
    List<String> convertQueryShopIds(List<PlatformApiTaskEntity> value, List<ShopInfoEntity> list);

    /**
     * 分组查询mongo
     */
    List<PlatformAmazonOrderDTO> findMongoByFulfillmentChannel(String value,
                                                               List<String> shopIds,
                                                               int currentPage,
                                                               Integer pageSize
    );

    /**
     * 根据条件查询mongo亚马逊订单
     */
    List<PlatformAmazonOrderDTO> findDownloadStatusAnShopId(Integer downloadStatus, List<String> shopIds, int currentPage, Integer pageSize);


    /**
     * 根据条件查询mongo亚马逊商品
     */
    List<PlatformAmazonListingDTO> findProductDownloadStatusAnShopId(Integer downloadStatus, List<String> shopIds, int currentPage, Integer pageSize);


    /**
     * 根据条件查询mongo亚马逊货件
     */
    List<PlatformAmazonFbaShipmentDTO> findFbaShipmentDownloadStatusAnShopId(Integer downloadStatus, List<String> shopIds, int currentPage, Integer pageSize);

    /**
     * 根据亚马逊账号分组查询
     */
    Map<String, List<PlatformAmazonOrderDTO>> groupByPlatformShopCode(List<PlatformAmazonOrderDTO> orderEntityList);

    /**
     * 处理商品明细
     */
    void handlerProductDetail(String key, List<PlatformApiTaskEntity> value, Integer size, String platform, String category, String business);

    /**
     * 商品过滤启用
     */
    List<PlatformAmazonListingDTO> filterAndGetEnableList(List<PlatformAmazonListingDTO> listingDTOList, AmazonMarketplaceEnum marketPlaceEnum);

    /**
     * 处理货件明细下载
     */
    void handlerFbaShipmentDetail(String groupId, List<PlatformApiTaskEntity> value, int size, String platform, String category, String business);

    /**
     * 检查跳过
     */
    Boolean checkSkipList(String uniqueId);

    /**
     * 根据订单ID补充订单
     */
    void handlerFulfilledCheckOrder(String groupId, List<PlatformApiTaskEntity> value, Integer size, String platform, String category, String business, List<CfgTimezoneEntity> timeZoneList, Map<String, ShopInfoEntity> shopMap);

    /**
     * 根据订单ID查询Mongo
     */
    List<PlatformAmazonOrderDTO> findMongoOrderByIdsAndPlatformShopCode(List<PlatformAmazonFulfilledShipmentsDTO> allOrderIds);

    /**
     * 根据账号ID查询Mongo
     */
    List<PlatformAmazonFulfilledShipmentsDTO> findHandleStatusAndPlatformShopCode(String handleStatus, String platformShopCode, int currentPage, Integer pageSize);

    /**
     * 远程查询仓储中心配置
     */
    List<CfgAmzFulfillmentCenterEntity> feignQueryFulfillmentCenterlist(List<String> centerCodeList);


    /**
     * 填充报告数据
     */
    List<ReportFulfilledShipmentsMongoDTO> reportFulfillmentFillData(List<ReportFulfilledShipmentsMongoDTO> allList);
}
