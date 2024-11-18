package com.erp.server.dmp.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.business.annotation.DataIdempotent;
import com.common.business.constant.BusinessCommonConstants;
import com.common.business.constant.MongoTableNameContant;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.dto.*;
import com.common.business.enums.BusinessTypeEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.utils.RedisUtil;
import com.common.business.wrapper.FeignQuery;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MapUtil;
import com.common.message.constant.RedisKeyConstant;
import com.erp.model.dmp.dto.AmazonShopInfoDTO;
import com.erp.model.dmp.entity.CfgTimezoneEntity;
import com.erp.model.dmp.entity.PlatformApiTaskEntity;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.entity.CfgAmzFulfillmentCenterEntity;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.wms.feign.WmsAmazonFeign;
import com.erp.rpc.wms.feign.WmsFbaInventoryFeign;
import com.erp.rpc.wms.feign.WmsWarehouseFeign;
import com.erp.sdk.oms.amz.spapi.convert.SdkFbaShipmentConverter;
import com.erp.sdk.oms.amz.spapi.dto.*;
import com.erp.sdk.oms.amz.spapi.enums.AmazonHandleStatusEnum;
import com.erp.sdk.oms.amz.spapi.enums.AmazonListingStatusEnum;
import com.erp.sdk.oms.amz.spapi.enums.AmazonMarketplaceEnum;
import com.erp.sdk.oms.amz.spapi.enums.AmazonRequestTypeRateLimiterEnum;
import com.erp.sdk.oms.amz.spapi.handler.AmazonFbaShipmentHandler;
import com.erp.sdk.oms.amz.spapi.handler.AmazonListingHandler;
import com.erp.sdk.oms.amz.spapi.handler.AmazonOrderHandler;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentinbound.InboundShipmentItemList;
import com.erp.sdk.oms.amz.spapi.model.orders.Order;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.service.*;
import com.xxl.job.core.context.XxlJobHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RedissonClient;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 亚马逊下载服务
 *
 * @author Jim
 * @date 2024/6/14
 */
@Slf4j
@Service
public class AmazonDownloadServiceImpl implements AmazonDownloadService {

    @Resource
    private MongoService mongoService;
    @Resource
    private BusinessServiceImpl businessService;
    @Resource
    private PlatformApiTaskService platformApiTaskService;
    @Resource
    private AmazonOrderHandler amazonOrderHandler;
    @Resource
    private AmazonListingHandler amazonListingHandler;
    @Resource
    private AmazonFbaShipmentHandler amazonFbaShipmentHandler;
    @Resource
    private ShopInfoFeign shopInfoFeign;
    @Resource
    private AmzReportHandleService amzReportHandleService;
    @Resource
    private WmsFbaInventoryFeign wmsFbaInventoryFeign;
    @Resource
    private DmpPushTaskService dmpPushTaskService;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private MongoTemplate mongoTemplate;
    @Resource
    private CfgSettingService cfgSettingService;
    @Resource
    private CfgAppClientService cfgAppClientService;
    @Resource
    private RedisUtil redisUtil;
    @Resource
    private CfgTimezoneService cfgTimezoneService;
    @Resource
    private WmsWarehouseFeign wmsWarehouseFeign;
    @Resource
    private WmsAmazonFeign wmsAmazonFeign;

    /**
     * 处理订单详情下载
     */
    @Override
    public void handlerOrderDetailDownload(String key, List<PlatformApiTaskEntity> value, Integer size, String platform, String category, List<ShopInfoEntity> list) {
        // 需要查询的店铺
        List<String> queryShopIds = convertQueryShopIds(value, list);

        // 查询当前分组未下载的mongo订单
        List<PlatformAmazonOrderDTO> orderEntityList = this.findDownloadStatusAnShopId(0, queryShopIds, 1, size);
        if (CollectionUtil.isEmpty(orderEntityList)) {
            XxlJobHelper.log("[拉取亚马逊订单详情任务] 无需要执行的详情,shopId={}", JSONUtil.toJsonStr(queryShopIds));
            return;
        }
        for (PlatformAmazonOrderDTO dto : orderEntityList) {
            String handleKey = StrUtil.format("Amazon:orderDetailDownload:{}:{}", dto.getPlatformShopCode(), dto.getOrder().getAmazonOrderId());
            singleHandlerOrderDetailDownload(key, platform, category, dto, handleKey);
        }
    }


    @Override
    @DataIdempotent(keyIdName = "handleKey", waitTime = 60)
    public void singleHandlerOrderDetailDownload(String key, String platform, String category, PlatformAmazonOrderDTO dto, String handleKey) {
        AmazonRequestTypeRateLimiterEnum requestTypeRateLimiterEnum = AmazonRequestTypeRateLimiterEnum.ORDER_ITEMS;
        try {
            // 动态请求配置
            // 平台请求中:平台类型:sellerId:业务类型:请求的端点区域
            String redissonKey = StrUtil.format(RedisCacheConstants.PLATFORM_RATE_LIMIT, platform, key, requestTypeRateLimiterEnum.getBusinessTypeName());
            JSONObject extentJsonObj = requestTypeRateLimiterEnum.getExtentJsonObj();
            extentJsonObj.put(AmazonRequestTypeRateLimiterEnum.limitKey, redissonKey);
            dto.setRedissonKey(redissonKey);
            PlatformAmazonOrderDTO newDto = amazonOrderHandler.downloadDetail(dto, extentJsonObj);

            newDto.setDownloadStatus(1);
            newDto.setDownloadTime(LocalDateTime.now(ZoneId.systemDefault()).toString());
            newDto.setRedissonKey(null);
            List<PlatformOrderDTO> convertDtoList = amazonOrderHandler.convert(Collections.singletonList(newDto));
            PlatformOrderDTO convertDto = convertDtoList.get(0);
            newDto.setDownloadAddressStatus(dto.getDownloadAddressStatus());
            String business = BusinessTypeEnum.ORDER.getCode();

            // 区分多渠道订单
            if (SourceTypeEnum.SO_MULTI_CHANNEL.getCode().equalsIgnoreCase(convertDto.getSourceType())) {
                // 不需要下载地址
                newDto.setDownloadAddressStatus(-1);
            }

            businessService.pullDetailProcess(newDto, convertDto, category, platform, business);
            log.info("[拉取亚马逊订单详情任务] amazonSalesOrderDetail下载成功，uniqueId={}", dto.getUniqueId());
            XxlJobHelper.log("[拉取亚马逊订单详情任务] amazonSalesOrderDetail下载成功，uniqueId={}", dto.getUniqueId());
        } catch (Exception error) {
            // 获取锁异常等重试
            if (error instanceof InterruptedException) {
                XxlJobHelper.log("请求亚马逊逊获取锁异常：{}", error.getMessage());
                throw new ServiceException(ApiError.ERROR_1026);
            }
            XxlJobHelper.log("[拉取亚马逊订单详情任务] amazonSalesOrderDetail下载失败，uniqueId={}, error={}",
                    dto.getUniqueId(),
                    error.getMessage());
            // 发送预警
            dmpPushTaskService.sendWarnMsg(dto.getDmpSyncTaskId());
        }
    }


    /**
     * 处理地址详情下载
     */
    @Override
    public void handlerAddressDetail(String key, List<PlatformApiTaskEntity> value, Integer size, String platform, String category, String business, List<ShopInfoEntity> list) {
        // 需要查询的店铺
        List<String> queryShopIds = convertQueryShopIds(value, list);

        // 查出下MFN订单
        List<PlatformAmazonOrderDTO> orderEntityList = this.findMongoByFulfillmentChannel(Order.FulfillmentChannelEnum.MFN.getValue(), queryShopIds, 1, size);
        if (CollectionUtil.isEmpty(orderEntityList)) {
            // 查询AFN订单
            orderEntityList = this.findMongoByFulfillmentChannel(Order.FulfillmentChannelEnum.AFN.getValue(), queryShopIds, 1, size);
            if (CollectionUtil.isEmpty(orderEntityList)) {
                XxlJobHelper.log("[拉取亚马逊订单地址任务] 无需要查询的地址,shopId={}", JSONUtil.toJsonStr(queryShopIds));
                return;
            }
        }

        AmazonRequestTypeRateLimiterEnum requestTypeRateLimiterEnum = AmazonRequestTypeRateLimiterEnum.ORDER_ADDRESS;
        // 平台请求中:平台类型:sellerId:业务类型:请求的端点区域
        String redissonKey = StrUtil.format(RedisCacheConstants.PLATFORM_REQUEST_PREFIX, key);
        for (PlatformAmazonOrderDTO dto : orderEntityList) {
            try {
                // 动态请求配置
                // 平台请求中:平台类型:sellerId:业务类型:请求的端点区域
                String limitKey = StrUtil.format(RedisCacheConstants.PLATFORM_RATE_LIMIT, platform, key, requestTypeRateLimiterEnum.getBusinessTypeName());
                JSONObject extentJsonObj = requestTypeRateLimiterEnum.getExtentJsonObj();
                extentJsonObj.put(AmazonRequestTypeRateLimiterEnum.limitKey, limitKey);
                dto.setRedissonKey(redissonKey);
                // 下载和处理地址
                PlatformAmazonOrderDTO newDto = amazonOrderHandler.downloadAddressAndBuyInfo(dto, extentJsonObj);
                newDto.setDownloadStatus(1);
                newDto.setDownloadAddressStatus(1);
                newDto.setDownloadTime(LocalDateTime.now(ZoneId.systemDefault()).toString());
                newDto.setRedissonKey(null);
                List<PlatformOrderDTO> convertDto = amazonOrderHandler.convert(Collections.singletonList(newDto));
                // 保存和发送mq
                businessService.pullDetailProcess(newDto, convertDto.get(0), category, platform, business);
                log.info("[拉取亚马逊订单地址任务] 无需要查询的地址,shopId={}", JSONUtil.toJsonStr(queryShopIds));
                XxlJobHelper.log("[拉取亚马逊订单地址任务] amazonSalesOrderAddressDownload下载成功, groupId={}, uniqueId={}", key, dto.getUniqueId());

                // 缓存移除结果
                String addressKey = StrUtil.format(RedisCacheConstants.AMZ_SP_API_RESULT_PREFIX, AmazonRequestTypeRateLimiterEnum.ORDER_ADDRESS.getBusinessTypeName(), dto.getUniqueId());
                String buyerKey = StrUtil.format(RedisCacheConstants.AMZ_SP_API_RESULT_PREFIX, AmazonRequestTypeRateLimiterEnum.BUYER_INFO.getBusinessTypeName(), dto.getUniqueId());
                List<String> delKeys = new LinkedList<>();
                if (redisUtil.hasKey(addressKey)) {
                    delKeys.add(addressKey);
                }
                if (redisUtil.hasKey(buyerKey)) {
                    delKeys.add(buyerKey);
                }
                if (!delKeys.isEmpty()) {
                    redisUtil.del(delKeys.toArray(new String[0]));
                }

            } catch (Exception e) {
                // 获取锁异常等重试
                if (e instanceof InterruptedException) {
                    XxlJobHelper.log("请求亚马逊逊获取锁异常：{}", e.getMessage());
                    throw new ServiceException(ApiError.ERROR_1026);
                }
                XxlJobHelper.log("[拉取亚马逊订单地址任务] amazonSalesOrderAddressDownload下载失败，groupId={},uniqueId={}, error={}",
                        key,
                        dto.getUniqueId(),
                        e.getMessage());
                // 发送预警
                dmpPushTaskService.sendWarnMsg(dto.getDmpSyncTaskId());
            }
        }
    }

    @Override
    public List<String> convertQueryShopIds(List<PlatformApiTaskEntity> value, List<ShopInfoEntity> list) {
        // 店铺IDS
        List<String> shopIds = value.stream().map(PlatformApiTaskEntity::getShopId).distinct().collect(Collectors.toList());
        // 所有关联的店铺
        List<String> platformCodeList = list.stream().filter(e -> shopIds.contains(e.getId())).map(ShopInfoEntity::getPlatformShopCode).distinct().collect(Collectors.toList());
        // 需要查询的店铺
        return list.stream()
                .filter(e -> platformCodeList.contains(e.getPlatformShopCode()))
                .map(BaseEntity::getId)
                .distinct()
                .collect(Collectors.toList());

    }


    @Override
    public List<PlatformAmazonOrderDTO> findMongoByFulfillmentChannel(String value,
                                                                      List<String> shopIds,
                                                                      int currentPage,
                                                                      Integer pageSize
    ) {
        Query query = new Query();
        query.addCriteria(Criteria.where("order.fulfillmentChannel").is(value)
                .and("shopId").in(shopIds)
                .and("downloadStatus").is(1)
                .and("downloadAddressStatus").is(0));

        if (currentPage > 0 && pageSize > 0) {
            query.skip((long) (currentPage - 1) * pageSize).limit(pageSize);
        }
        return mongoTemplate.find(query, PlatformAmazonOrderDTO.class, MongoTableNameContant.THIRD_SYSTEM_AMAZON_ORDER);
    }



    @Override
    public List<PlatformAmazonOrderDTO> findDownloadStatusAnShopId(Integer downloadStatus, List<String> shopIds, int currentPage, Integer pageSize) {
        Query query = new Query();
        query.addCriteria(Criteria.where("downloadStatus").is(downloadStatus)
                .and("shopId").in(shopIds));

        if (currentPage > 0 && pageSize > 0) {
            query.skip((long) (currentPage - 1) * pageSize).limit(pageSize);
        }
        return mongoTemplate.find(query, PlatformAmazonOrderDTO.class, MongoTableNameContant.THIRD_SYSTEM_AMAZON_ORDER);
    }


    @Override
    public List<PlatformAmazonListingDTO> findProductDownloadStatusAnShopId(Integer downloadStatus, List<String> shopIds, int currentPage, Integer pageSize) {
        Query query = new Query();
        query.addCriteria(Criteria.where("shopId").in(shopIds)
                .and("downloadStatus").is(downloadStatus)
        );

        if (currentPage > 0 && pageSize > 0) {
            query.skip((long) (currentPage - 1) * pageSize).limit(pageSize);
        }
        return mongoTemplate.find(query, PlatformAmazonListingDTO.class, MongoTableNameContant.THIRD_SYSTEM_AMAZON_PRODUCT);
    }


    @Override
    public List<PlatformAmazonFbaShipmentDTO> findFbaShipmentDownloadStatusAnShopId(Integer downloadStatus, List<String> shopIds, int currentPage, Integer pageSize) {
        Query query = new Query();
        query.addCriteria(Criteria.where("shopId").in(shopIds).and("downloadStatus").is(downloadStatus));
        if (currentPage > 0 && pageSize > 0) {
            query.skip((long) (currentPage - 1) * pageSize).limit(pageSize);
        }
        return mongoTemplate.find(query, PlatformAmazonFbaShipmentDTO.class, MongoTableNameContant.THIRD_SYSTEM_AMAZON_FBA_SHIPMENT);
    }


    @Override
    public Map<String, List<PlatformAmazonOrderDTO>> groupByPlatformShopCode(List<PlatformAmazonOrderDTO> orderEntityList) {
        Map<String, List<PlatformAmazonOrderDTO>> sourceDtoMap = orderEntityList.stream().collect(Collectors.groupingBy(PlatformAmazonOrderDTO::getShopId));
        List<String> shopIds = orderEntityList.stream().map(PlatformAmazonOrderDTO::getShopId).distinct().collect(Collectors.toList());
        List<ShopInfoEntity> shopInfoEntityList = shopInfoFeign.listShopInfoByIds(shopIds);
        // 根据平台Seller分组
        Map<String, List<ShopInfoEntity>> shopGroupMap = shopInfoEntityList.stream().collect(Collectors.groupingBy(ShopInfoEntity::getPlatformShopCode));

        Map<String, List<PlatformAmazonOrderDTO>> dtoMap = new HashMap<>();
        shopGroupMap.forEach((key, value) -> {
            List<PlatformAmazonOrderDTO> currentList = new LinkedList<>();
            for (ShopInfoEntity shopInfo : value) {
                currentList.addAll(sourceDtoMap.get(shopInfo.getId()));
            }
            dtoMap.put(key, currentList);
        });
        return dtoMap;
    }


    @Override
    public void handlerProductDetail(String key, List<PlatformApiTaskEntity> value, Integer size, String platform, String category, String business) {
        List<String> shopIds = value.stream().map(PlatformApiTaskEntity::getShopId).distinct().collect(Collectors.toList());
        // 查询当前分组未下载的mongo订单
        List<PlatformAmazonListingDTO> listingEntityList = this.findProductDownloadStatusAnShopId(0, shopIds, 1, size);
        if (CollectionUtil.isEmpty(listingEntityList)) {
            XxlJobHelper.log("[拉取亚马逊商品详情任务] 无需要执行的详情,shopId={}", JSONUtil.toJsonStr(shopIds));
            return;
        }
        // 根据shopId分组
        Map<String, List<PlatformAmazonListingDTO>> dtoGroupList = listingEntityList.stream().collect(Collectors.groupingBy(PlatformAmazonListingDTO::getShopId));


        AmazonRequestTypeRateLimiterEnum requestTypeRateLimiterEnum = AmazonRequestTypeRateLimiterEnum.PRODUCT_ITEMS;
        for (Map.Entry<String, List<PlatformAmazonListingDTO>> entry : dtoGroupList.entrySet()) {
            AmazonShopInfoDTO shopInfoDTO = cfgAppClientService.cacheAndFindShopAuth(entry.getKey());
            AmazonMarketplaceEnum marketPlaceEnum = AmazonMarketplaceEnum.getByCountryCode(shopInfoDTO.getDictCountryCode());
            // 过滤亚马逊异常数据或无法查询明细的数据
            List<PlatformAmazonListingDTO> currentListingDTOList = this.filterAndGetEnableList(entry.getValue(), marketPlaceEnum);
            if (CollectionUtils.isEmpty(currentListingDTOList)) {
                XxlJobHelper.log("[拉取亚马逊商品详情任务] 当前店铺无有效数据需下载，shopId={}", entry.getKey());
                continue;
            }

            // 动态请求配置
            // 平台请求中:平台类型:sellerId:业务类型:请求的端点区域
            String redissonKey = StrUtil.format(RedisCacheConstants.PLATFORM_RATE_LIMIT, platform, key, requestTypeRateLimiterEnum.getBusinessTypeName());
            JSONObject extentJsonObj = requestTypeRateLimiterEnum.getExtentJsonObj();
            extentJsonObj.put(AmazonRequestTypeRateLimiterEnum.limitKey, redissonKey);
            // 根据IdentifiersType分组查询
            List<PlatformAmazonListingDTO> newDtoList = amazonListingHandler.downloadDetailListByIdentifiersType(currentListingDTOList, extentJsonObj, shopInfoDTO, marketPlaceEnum, size);
            for (PlatformAmazonListingDTO newDto : newDtoList) {
                try {
                    newDto.setDownloadStatus(1);
                    newDto.setDownloadTime(LocalDateTime.now(ZoneId.systemDefault()).toString());
                    newDto.setRedissonKey(null);
                    List<PlatformProductDTO> convertDto = amazonListingHandler.convert(Collections.singletonList(newDto));
                    businessService.pullDetailProcess(newDto, convertDto.get(0), category, platform, business);
                    log.info("[拉取亚马逊商品详情任务] amazonProductDetail下载成功，uniqueId={}", newDto.getUniqueId());
                    XxlJobHelper.log("[拉取亚马逊商品详情任务] amazonProductDetail下载成功，uniqueId={}", newDto.getUniqueId());
                } catch (Exception error) {
                    // 获取锁异常等重试
                    if (error instanceof InterruptedException) {
                        XxlJobHelper.log("请求亚马逊逊获取锁异常：{}", error.getMessage());
                        throw new ServiceException(ApiError.ERROR_1026);
                    }
                    XxlJobHelper.log("[拉取亚马逊商品详情任务] amazonProductDetail下载失败，uniqueId={}, error={}",
                            newDto.getUniqueId(),
                            ExceptionUtil.stacktraceToString(error, 2000));
                    // 发送预警
                    dmpPushTaskService.sendWarnMsg(newDto.getDmpSyncTaskId());
                }
            }
        }


    }

    @Override
    public List<PlatformAmazonListingDTO> filterAndGetEnableList(List<PlatformAmazonListingDTO> listingDTOList, AmazonMarketplaceEnum marketPlaceEnum) {
        List<PlatformAmazonListingDTO> resultList = new ArrayList<>();
        Class<?> tClass = PlatformAmazonListingDTO.class;
        String tableName = MongoTableNameContant.THIRD_SYSTEM_AMAZON_PRODUCT;
        for (PlatformAmazonListingDTO dto : listingDTOList) {
            if ("1".equalsIgnoreCase(dto.getProductIdType()) && AmazonListingStatusEnum.INACTIVE.getCode().equalsIgnoreCase(dto.getStatus())) {
                dto.setDownloadStatus(-1);
                dto.setDownloadDesc("ProductIdType=ASIN,停售无法更新明细");
                //更新mongo
                UniqueDto updateDto = UniqueDto.getUniqId(dto.getUniqueId());
                MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(dto), MapUtil.class);
                mongoService.updateMongoData(updateDto, mapUtil, tableName, tClass);
                continue;
            }
            // 日本异常数据
            if (AmazonMarketplaceEnum.JP.equals(marketPlaceEnum)) {
                if ("4".equals(dto.getProductIdType())) {
                    dto.setDownloadStatus(-1);
                    dto.setDownloadDesc("日本站点ProductIdType=4无法更新明细");
                    //更新mongo
                    UniqueDto updateDto = UniqueDto.getUniqId(dto.getUniqueId());
                    MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(dto), MapUtil.class);
                    mongoService.updateMongoData(updateDto, mapUtil, tableName, tClass);
                    continue;
                }
            }
            resultList.add(dto);
        }
        return resultList;
    }

    /**
     * 处理FBA货件详情
     */
    @Override
    public void handlerFbaShipmentDetail(String groupId, List<PlatformApiTaskEntity> value, int size, String platform, String category, String business) {
        List<String> shopIds = value.stream().map(PlatformApiTaskEntity::getShopId).distinct().collect(Collectors.toList());
        // 查询当前分组未下载的mongo订单
        List<PlatformAmazonFbaShipmentDTO> dtoList = this.findFbaShipmentDownloadStatusAnShopId(0, shopIds, 1, size);
        if (CollectionUtil.isEmpty(dtoList)) {
            XxlJobHelper.log("[拉取亚马逊Fba货件详情任务] 无需要执行的详情,shopId={}", JSONUtil.toJsonStr(shopIds));
            return;
        }
        // 根据shopId分组
        Map<String, List<PlatformAmazonFbaShipmentDTO>> dtoGroupList = dtoList.stream().collect(Collectors.groupingBy(PlatformAmazonFbaShipmentDTO::getShopId));

        AmazonRequestTypeRateLimiterEnum requestTypeRateLimiterEnum = AmazonRequestTypeRateLimiterEnum.FBA_SHIPMENT_DETAIL;
        for (Map.Entry<String, List<PlatformAmazonFbaShipmentDTO>> entry : dtoGroupList.entrySet()) {
            AmazonShopInfoDTO shopInfoDTO = cfgAppClientService.cacheAndFindShopAuth(entry.getKey());
            AmazonMarketplaceEnum marketPlaceEnum = AmazonMarketplaceEnum.getByCountryCode(shopInfoDTO.getDictCountryCode());
            // 过滤亚马逊异常数据或无法查询明细的数据
            List<PlatformAmazonFbaShipmentDTO> currentDTOList = entry.getValue();
            if (CollectionUtils.isEmpty(currentDTOList)) {
                XxlJobHelper.log("[拉取亚马逊Fba货件详情任务] 当前店铺无有效数据需下载，shopId={}", entry.getKey());
                continue;
            }

            // 动态请求配置
            // 平台请求中:平台类型:sellerId:业务类型:请求的端点区域
            String redissonKey = StrUtil.format(RedisCacheConstants.PLATFORM_RATE_LIMIT, platform, groupId, requestTypeRateLimiterEnum.getBusinessTypeName());
            JSONObject extentJsonObj = requestTypeRateLimiterEnum.getExtentJsonObj();
            extentJsonObj.put(AmazonRequestTypeRateLimiterEnum.limitKey, redissonKey);
            // 批量检查并填充主数据
            currentDTOList = amazonFbaShipmentHandler.checkAndDownloadMainInfo(currentDTOList, shopInfoDTO, marketPlaceEnum);

            for (PlatformAmazonFbaShipmentDTO dto : currentDTOList) {
                try {
                    // 校验黑名单不调用亚马逊接口
                    Boolean skipRequest = this.checkSkipList(dto.getUniqueId());
                    // 下载和处理详情
                    PlatformAmazonFbaShipmentDTO newDto;
                    // 非线上支持手动
                    if ((!BusinessCommonConstants.hasProfile("prod") && dto.getUniqueId().contains("手动测试")) || skipRequest) {
                        newDto = dto;
                    } else {
                        newDto = amazonFbaShipmentHandler.downloadDetail(dto, null);
                    }
                    XxlJobHelper.log("[拉取亚马逊Fba货件详情任务] 下载完：{}", JSONUtil.toJsonStr(dto));

                    newDto.setDownloadStatus(1);
                    newDto.setDownloadTime(LocalDateTime.now(ZoneId.systemDefault()).toString());
                    XxlJobHelper.log("[拉取亚马逊Fba货件详情任务] 主体转换前：{}", JSONUtil.toJsonStr(newDto));
                    // 转换
                    PlatformFbaShipmentDTO shipmentDTO = SdkFbaShipmentConverter.INSTANCE.downloadDtoToSaveDto(newDto);
                    InboundShipmentItemList sourceDetailList = newDto.getDetailList();
                    XxlJobHelper.log("[拉取亚马逊Fba货件详情任务] 详情转换前：{}", JSONUtil.toJsonStr(newDto));
                    List<PlatformFbaShipmentReceiveDTO> receiveDTOList = sourceDetailList.stream()
                            .map(SdkFbaShipmentConverter.INSTANCE::receiveDtoToSaveDto)
                            .collect(Collectors.toList());
                    shipmentDTO.setReceiveDTOList(receiveDTOList);
                    // 合并成详情
                    List<PlatformFbaShipmentReceiveDTO> detailListDTO = new ArrayList<>(
                            receiveDTOList.stream()
                                    .collect(Collectors.toMap(
                                            shipment -> shipment.getFbaShipmentId() + shipment.getFnSku() + shipment.getSellerSku(),
                                            shipment -> shipment,
                                            PlatformFbaShipmentReceiveDTO::merge))
                                    .values()
                    );
                    // 判断签收时间
                    detailListDTO.forEach(e -> {
                        if (e.getReceiveQty() == 0) {
                            e.setReceiveDate(null);
                        }
                    });
                    shipmentDTO.setDetailList(detailListDTO);
                    XxlJobHelper.log("[拉取亚马逊Fba货件详情任务] 推送前：{}", JSONUtil.toJsonStr(newDto));
                    businessService.pullDetailProcess(newDto, shipmentDTO, category, platform, business);
                    XxlJobHelper.log("[拉取亚马逊Fba货件详情任务] 推送后：{}", JSONUtil.toJsonStr(newDto));
                } catch (Exception e) {
                    XxlJobHelper.log("[拉取亚马逊Fba货件详情任务] amazonFbaShipmentDetailDownload下载失败，uniqueId={}, error={}",
                            dto.getUniqueId(),
                            e.getMessage());
                    // 发送预警
                    dmpPushTaskService.sendWarnMsg(dto.getDmpSyncTaskId());
                }
            }
        }
    }

    /**
     * 检查配置是否存在
     * 存在=跳过请求亚马逊接口
     */
    @Override
    public Boolean checkSkipList(String uniqueId) {
        String listStr = cfgSettingService.getValue(SettingEnum.AMAZON_FBA_SHIPMENT_SKIP_LIST);
        // 无配置
        if (StringUtils.isBlank(listStr)) {
            return false;
        }
        List<String> skipList;
        if (listStr.contains(",")) {
            skipList = Arrays.stream(listStr.split(",")).collect(Collectors.toList());
        } else {
            skipList = Collections.singletonList(listStr);
        }
        return skipList.contains(uniqueId);
    }



    @Override
    public void handlerFulfilledCheckOrder(String groupId, List<PlatformApiTaskEntity> value, Integer size, String platform, String category, String business, List<CfgTimezoneEntity> timeZoneList, Map<String, ShopInfoEntity> shopMap) {
        // 亚马逊账号
        String platformShopCode = StringUtils.substringBetween(groupId, ":", ":");
        // 查询当前分组未下载的mongo订单
        List<PlatformAmazonFulfilledShipmentsDTO> dtoList = this.findHandleStatusAndPlatformShopCode(AmazonHandleStatusEnum.WAIT_DOWNLOAD.getCode(), platformShopCode, 1, size);

        if (CollectionUtils.isEmpty(dtoList)) {
            XxlJobHelper.log("不存在需要补充的订单,店铺ID={}", groupId);
            return;
        }

        // 查询已存在的订单
        List<PlatformAmazonOrderDTO> existOrderList = this.findMongoOrderByIdsAndPlatformShopCode(dtoList);

        // 转换Map
        Map<String, PlatformAmazonOrderDTO> existOrderMap = existOrderList.stream()
                .collect(Collectors.toMap(PlatformAmazonOrderDTO::convertOrderIdWithPlatformShopCode, Function.identity()));

        // 任意店铺ID
        String shopId = value.get(0).getShopId();

        // 按订单主表是否已下载分组
        Map<Boolean, List<PlatformAmazonFulfilledShipmentsDTO>> groupMap = dtoList.stream()
                .collect(Collectors.groupingBy(e -> existOrderMap.containsKey(e.convertOrderIdWithPlatformShopCode())));

        List<PlatformAmazonFulfilledShipmentsDTO> existList = groupMap.get(true);
        List<PlatformAmazonFulfilledShipmentsDTO> queryList = groupMap.get(false);

        if (!CollectionUtils.isEmpty(queryList)) {
            // Map<单号， 店铺ID> （当前同一账号下的单号对应唯一店铺ID）
            Map<String, String> dtoOrderIdShopIdMap = queryList.stream()
                    .filter(e-> StringUtils.isNotBlank(e.getShopId()))
                    .collect(Collectors.toMap(
                            PlatformAmazonFulfilledShipmentsDTO::getAmazonOrderId,
                            PlatformAmazonFulfilledShipmentsDTO::getShopId,
                            (existing, replacement) -> existing,
                            HashMap::new));
            Set<String> orderIds = dtoOrderIdShopIdMap.keySet();
            try {
                // 根据订单ID下载分组查询
                List<PlatformAmazonOrderDTO> newOrderDTOList = amazonOrderHandler.downloadByOrderIds(dtoOrderIdShopIdMap, shopId, groupId, timeZoneList);
                // 订单主体保存倒mongo
                businessService.handleSaveOrUpdateMongo(newOrderDTOList, MongoTableNameContant.THIRD_SYSTEM_AMAZON_ORDER, PlatformAmazonOrderDTO.class, new ArrayList<>());
                // 更新物流销售记录mongo
                Map<String, PlatformAmazonOrderDTO> orderMap = newOrderDTOList.stream().collect(Collectors.toMap(e -> e.getOrder().getAmazonOrderId(), Function.identity()));
                fillDataAndSetHandleStatus(queryList, orderMap, timeZoneList, shopMap);
                businessService.handleSaveOrUpdateMongo(queryList, MongoTableNameContant.THIRD_SYSTEM_AMAZON_SO_OUT_STOCK, PlatformAmazonFulfilledShipmentsDTO.class, new ArrayList<>());
            } catch (Exception e) {
                XxlJobHelper.log("补充的订单失败,店铺ID={}, orderIds={}, error={}", shopId, orderIds, e.getMessage());
            }
        }
        if (!CollectionUtils.isEmpty(existList)) {
            // 更新物流销售记录mongo
            fillDataAndSetHandleStatus(existList, existOrderMap, timeZoneList, shopMap);
            businessService.handleSaveOrUpdateMongo(existList, MongoTableNameContant.THIRD_SYSTEM_AMAZON_SO_OUT_STOCK, PlatformAmazonFulfilledShipmentsDTO.class, new ArrayList<>());

        }
        XxlJobHelper.log("补充的订单成功,店铺ID={}", shopId);
    }


    public static void fillDataAndSetHandleStatus(List<PlatformAmazonFulfilledShipmentsDTO> existList,
                                                  Map<String, PlatformAmazonOrderDTO> existOrderMap,
                                                  List<CfgTimezoneEntity> timeZoneList,
                                                  Map<String, ShopInfoEntity> shopMap) {
        existList.forEach(e -> {
            e.setHandleStatus(AmazonHandleStatusEnum.WAIT_HANDLE.getCode());
            PlatformAmazonOrderDTO orderDTO = existOrderMap.get(e.convertOrderIdWithPlatformShopCode());
            if (null == orderDTO) {
                return;
            }
            e.setShopId(orderDTO.getShopId());
            ShopInfoEntity shopInfoEntity = shopMap.get(orderDTO.getShopId());
            if (null == shopInfoEntity) {
                return;
            }
            CfgTimezoneEntity timeZoneEntity = timeZoneList.stream()
                    .filter(t -> t.getCountry().equalsIgnoreCase(shopInfoEntity.getDictCountryCode()))
                    .findFirst()
                    .orElse(null);
            if (null == timeZoneEntity) {
                // 配置找不到
                return;
            }
            // 设置所有本地时区
            e.checkAndSetAllDateLocale(timeZoneEntity.getTimeZone());

        });
    }

    @Override
    public List<PlatformAmazonOrderDTO> findMongoOrderByIdsAndPlatformShopCode(List<PlatformAmazonFulfilledShipmentsDTO> dtoList) {
        // 构建查询条件
        List<Criteria> criteriaList = new ArrayList<>();
        for (PlatformAmazonFulfilledShipmentsDTO dto : dtoList) {
            Criteria criteria = Criteria.where("order.amazonOrderId").is(dto.getAmazonOrderId())
                    .and("platformShopCode").is(dto.getPlatformShopCode());
            criteriaList.add(criteria);
        }
        Query query = new Query(new Criteria().orOperator(criteriaList.toArray(new Criteria[0])));

        List<PlatformAmazonOrderDTO> orderDTOList = mongoTemplate.find(query, PlatformAmazonOrderDTO.class, MongoTableNameContant.THIRD_SYSTEM_AMAZON_ORDER);
        if (CollectionUtils.isEmpty(orderDTOList)) {
            return Collections.emptyList();
        }
        return orderDTOList;
    }

    @Override
    public List<PlatformAmazonFulfilledShipmentsDTO> findHandleStatusAndPlatformShopCode(String handleStatus, String platformShopCode, int currentPage, Integer pageSize) {
        Query query = new Query();
        query.addCriteria(Criteria.where("platformShopCode").is(platformShopCode)
                .and("handleStatus").is(handleStatus)
                .and("shopId").exists(true)
                .and("downloadStatus").is(0)
        );

        if (currentPage > 0 && pageSize > 0) {
            query.skip((long) (currentPage - 1) * pageSize).limit(pageSize);
        }
        return mongoTemplate.find(query, PlatformAmazonFulfilledShipmentsDTO.class, MongoTableNameContant.THIRD_SYSTEM_AMAZON_SO_OUT_STOCK);
    }


    @Override
    public List<CfgAmzFulfillmentCenterEntity> feignQueryFulfillmentCenterlist(List<String> centerCodeList) {
        // 查询仓库中心配置
        if (CollectionUtils.isEmpty(centerCodeList)){
            return FeignQuery.create(CfgAmzFulfillmentCenterEntity.class)
                    .list();
        } else {
            return FeignQuery.create(CfgAmzFulfillmentCenterEntity.class)
                    .in(CfgAmzFulfillmentCenterEntity::getCode, centerCodeList)
                    .list();
        }
    }

    @Override
    public List<ReportFulfilledShipmentsMongoDTO> reportFulfillmentFillData(List<ReportFulfilledShipmentsMongoDTO> allList) {
        // 仓储中心列表
        List<String> centerCodeList = allList.stream()
                .map(ReportFulfilledShipmentsMongoDTO::getFulfillmentCenterId)
                .distinct()
                .collect(Collectors.toList());

        // 查询仓库中心配置
        List<CfgAmzFulfillmentCenterEntity> centerEntityList = feignQueryFulfillmentCenterlist(centerCodeList);
        Map<String, CfgAmzFulfillmentCenterEntity> centerMap = centerEntityList
                .stream()
                .collect(Collectors.toMap(CfgAmzFulfillmentCenterEntity::getCode, Function.identity()));

        // 新增未存在的仓储中心
        List<String> notExistCenterIds = centerCodeList.stream().filter(e -> !centerMap.containsKey(e)).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(notExistCenterIds)){
            List<CfgAmzFulfillmentCenterEntity> newCenterList = notExistCenterIds.stream()
                    .map(CfgAmzFulfillmentCenterEntity::new)
                    .collect(Collectors.toList());
            wmsAmazonFeign.addCfgAmzFulfillmentCenterList(newCenterList);
        }


        // 店铺信息
        List<ShopInfoEntity> shopList =  FeignQuery.create(ShopInfoEntity.class)
                .eq(ShopInfoEntity::getDictPlatform, PlatformDictEnum.AMAZON.getCode())
                .ne(ShopInfoEntity::getPlatformShopCode, "")
                .list();

        // 所有店铺信息Map<亚马逊账号， Map<国家代号, 店铺信息>
        Map<String, Map<String, ShopInfoEntity>> shopMap = shopList
                .stream()
                .collect(Collectors.groupingBy(ShopInfoEntity::getPlatformShopCode,
                        Collectors.toMap(ShopInfoEntity::getDictCountryCode,
                                Function.identity(),
                                // 已授权优先
                                (existing, replacement) -> AuthStatusEnum.ALREADY.getCode().equalsIgnoreCase(replacement.getAuthStatus()) ? replacement : existing
                        )));

        // 仓库信息
        Map<String, WarehouseDTO.ListDTO> warehouseMap = new HashMap<>();
        List<String> warehouseIds = shopList.stream().map(ShopInfoEntity::getWarehouseId).distinct().collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(warehouseIds)) {
            warehouseMap =  wmsWarehouseFeign.listByIds(warehouseIds)
                    .stream().collect(Collectors.toMap(WarehouseDTO.ListDTO::getId, Function.identity()));
        }

        // 矫正时区(报告来源的时间可能不带时区)
        List<CfgTimezoneEntity> timeList = cfgTimezoneService.listAndCache();

        Map<String, WarehouseDTO.ListDTO> finalWarehouseMap = warehouseMap;
        return allList.stream()
                .map(e -> parseDateLocaleShopIdWarehouseId(e, timeList, shopMap, centerMap, finalWarehouseMap))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 检查设置时区和店铺ID
     */
    private static ReportFulfilledShipmentsMongoDTO parseDateLocaleShopIdWarehouseId(ReportFulfilledShipmentsMongoDTO e,
                                                                                     List<CfgTimezoneEntity> timeList,
                                                                                     Map<String, Map<String, ShopInfoEntity>> shopMap,
                                                                                     Map<String, CfgAmzFulfillmentCenterEntity> centerMap,
                                                                                     Map<String, WarehouseDTO.ListDTO> warehouseMap
    ) {
        // 按仓储中心补充仓库
        // 多渠道订单以仓储中心对应国家作为站点
        try {
            CfgAmzFulfillmentCenterEntity centerEntity = centerMap.get(e.getFulfillmentCenterId());

            // Map<国家代号, 店铺>
            Map<String, ShopInfoEntity> curMap = shopMap.get(e.getPlatformShopCode());

            // 设置仓库中心对应仓库
            if (null != centerEntity && !curMap.isEmpty() && org.apache.commons.lang.StringUtils.isNotBlank(centerEntity.getCountry())){
                ShopInfoEntity shopInfo = curMap.get(centerEntity.getCountry());
                // 补充仓库信息
                fillWarehouseInfo(e, warehouseMap, shopInfo);
            }
            if (e.hasMultiChannel()) {
                // 多渠道订单
                parseMultiChannel(e, timeList, curMap, centerEntity);
            } else {
                // B2C订单
                parseB2cOrder(e, timeList, curMap, centerEntity, warehouseMap);
            }
        } catch (Exception ex) {
            log.error("解析亚马逊订单错误：{}", ExceptionUtil.stacktraceToString(ex));
        }
        return e;
    }

    /**
     * 补充信息(B2C销售订单)
     */
    private static void parseB2cOrder(ReportFulfilledShipmentsMongoDTO e, List<CfgTimezoneEntity> timeList, Map<String, ShopInfoEntity> curMap, CfgAmzFulfillmentCenterEntity centerEntity, Map<String, WarehouseDTO.ListDTO> warehouseMap) {
        // 解析后的时区(按销售渠道)
        CfgTimezoneEntity timeZoneEntity = timeList.stream()
                .filter(t -> t.getAndParseCondition().contains(e.getSalesChannel()))
                .findFirst()
                .orElse(null);
        if (null != timeZoneEntity) {
            // 设置所有本地时区
            e.checkAndSetAllDateLocale(timeZoneEntity.getTimeZone());
            if (!curMap.isEmpty() && curMap.containsKey(timeZoneEntity.getCountry())) {
                ShopInfoEntity shopInfo = curMap.get(timeZoneEntity.getCountry());
                e.setShopId(shopInfo.getId());
                // 仓储中心为空按销售渠道对应仓库出库
                if (null == centerEntity){
                    // 补充仓库信息
                    fillWarehouseInfo(e, warehouseMap, shopInfo);
                } else {
                    // 仓储中心配置为空
                    if (org.apache.commons.lang.StringUtils.isBlank(centerEntity.getCountry())){
                        // 补充仓库信息
                        fillWarehouseInfo(e, warehouseMap, shopInfo);
                    }
                }
            }
        }
    }

    /**
     * 补充信息(多渠道销售订单)
     */
    private static void parseMultiChannel(ReportFulfilledShipmentsMongoDTO e, List<CfgTimezoneEntity> timeList, Map<String, ShopInfoEntity> curMap, CfgAmzFulfillmentCenterEntity centerEntity) {
        // 解析后的时区(按仓储中心)
        if (null == centerEntity){
            return;
        }
        CfgTimezoneEntity timeZoneEntity = timeList.stream()
                .filter(t -> t.getCountry().equalsIgnoreCase(centerEntity.getCountry()))
                .findFirst()
                .orElse(null);
        if (null != timeZoneEntity) {
            // 设置所有本地时区
            e.checkAndSetAllDateLocale(timeZoneEntity.getTimeZone());
            if (!curMap.isEmpty()) {
                ShopInfoEntity shopInfo = curMap.get(timeZoneEntity.getCountry());
                e.setShopId(shopInfo.getId());
            }
        }
    }

    /**
     * 填充仓库信息
     */
    private static void fillWarehouseInfo(ReportFulfilledShipmentsMongoDTO e, Map<String, WarehouseDTO.ListDTO> warehouseMap, ShopInfoEntity shopInfo) {
        e.setWarehouseId(shopInfo.getWarehouseId());
        if (!CollectionUtils.isEmpty(warehouseMap) && warehouseMap.containsKey(shopInfo.getWarehouseId())){
            WarehouseDTO.ListDTO warehouseDTO = warehouseMap.get(shopInfo.getWarehouseId());
            e.setWarehouseName(warehouseDTO.getName());
            e.setWarehouseOrgId(warehouseDTO.getOrgId());
            e.setWarehouseOrgName(warehouseDTO.getOrgName());
        }
    }
}
