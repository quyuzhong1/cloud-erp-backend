package com.erp.server.dmp.handler.mongo;

import cn.hutool.core.util.StrUtil;
import com.common.business.constant.MongoTableNameContant;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.MongoSuperDTO;
import com.common.business.dto.RequestDTO;
import com.common.business.dto.UniqueDto;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.BusinessTypeEnum;
import com.common.business.enums.PlatformCategoryEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.wrapper.FeignQuery;
import com.erp.model.dmp.entity.CfgTimezoneEntity;
import com.erp.model.dmp.entity.DmpMongoHandleTaskEntity;
import com.erp.model.dmp.enums.CleanStatusEnum;
import com.erp.model.oms.dto.ShopInfoDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.entity.CfgAmzFulfillmentCenterEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.WmsWarehouseFeign;
import com.erp.sdk.oms.amz.spapi.convert.SdkSoOutStockConverter;
import com.erp.sdk.oms.amz.spapi.dto.*;
import com.erp.sdk.oms.amz.spapi.enums.AmazonHandleStatusEnum;
import com.erp.server.dmp.enums.DownloadStatusEnum;
import com.erp.server.dmp.handler.DmpMongoHandler;
import com.erp.server.dmp.service.AmazonDownloadService;
import com.erp.server.dmp.service.CfgTimezoneService;
import com.erp.server.dmp.service.DmpMongoHandleTaskService;
import com.erp.server.dmp.service.impl.BusinessServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 亚马逊物流销售报告处理服务
 *
 * @author Jim
 * @date 2024/03/06
 */
@Slf4j
@Component("amzFulfilledShipmentsMongoHandler")
public class AmzReportFulfilledShipmentsHandler extends DmpMongoHandler {
    @Resource
    private CfgTimezoneService cfgTimezoneService;
    @Resource
    private MongoTemplate mongoTemplate;
    @Resource
    private BusinessServiceImpl businessService;
    @Resource
    private ShopInfoFeign shopInfoFeign;
    @Resource
    private DmpMongoHandleTaskService dmpMongoHandleTaskService;
    @Resource
    private AmazonDownloadService amazonDownloadService;
    @Resource
    private WmsWarehouseFeign wmsWarehouseFeign;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer findAndFillDataOrHandle(DmpMongoHandleTaskEntity mongoHandleTaskEntity, Boolean queryIsAddOrUpdate) {
        // 任务每次处理数量
        Integer handleCount = mongoHandleTaskEntity.getHandleCount();
        // 指定的mongo表
        String mongoTableName = MongoTableNameContant.DATA_REPORT_AMZ_FULFILLED_SHIPMENTS;
        // 查询
        List<ReportFulfilledShipmentsMongoDTO> allList = dmpMongoHandleTaskService.findMongoData(mongoHandleTaskEntity.getLastId(), handleCount, mongoTableName, ReportFulfilledShipmentsMongoDTO.class, queryIsAddOrUpdate);
        if (CollectionUtils.isEmpty(allList)) {
            log.warn("亚马逊物流销售报告处理服务处理结束：处理数据为空:handleType={}", mongoHandleTaskEntity.getHandleType());
            return 0;
        }

        // 记录最大ID和下次执行时间
        String maxLastId = allList.stream().map(ReportSuperMongoDTO::getId).max(String::compareTo).orElse("0");
        dmpMongoHandleTaskService.updateMaxLastIdAndNextTime(mongoHandleTaskEntity, maxLastId);

        // 补充数据
        List<ReportFulfilledShipmentsMongoDTO> canHandleList = fillData(allList);

//        log.debug("亚马逊物流销售报告处理服务处理：转换后的数据={}", JSONUtil.toJsonStr(allList));

        List<String> uniqueIds = canHandleList.stream()
                .map(e -> StrUtil.format("{}_{}", e.getAmazonOrderId(), e.getShopId()))
                .distinct()
                .collect(Collectors.toList());
        // 查询亚马逊订单是否存在
        List<PlatformAmazonOrderDTO> existOrderList = findAllOrderMongoData(uniqueIds);

        if (CollectionUtils.isEmpty(existOrderList)) {
            // 都不存在直接保存mongo等待重新触发
            // 不存在保存mongo等待重新触发
            directSaveMongo(canHandleList, AmazonHandleStatusEnum.WAIT_DOWNLOAD, DownloadStatusEnum.WAIT);
            return allList.size();
        }
        Map<String, PlatformAmazonOrderDTO> existMap = existOrderList.stream()
                .collect(Collectors.toMap(UniqueDto::getUniqueId, Function.identity()));

        List<ReportFulfilledShipmentsMongoDTO> existList = new LinkedList<>();
        List<ReportFulfilledShipmentsMongoDTO> notExistList = new LinkedList<>();
        List<ReportFulfilledShipmentsMongoDTO> existMainList = new LinkedList<>();

        for (ReportFulfilledShipmentsMongoDTO source : allList) {
            String uniqueId = StrUtil.format("{}_{}", source.getAmazonOrderId(), source.getShopId());
            PlatformAmazonOrderDTO dto = existMap.get(uniqueId);
            if (null == dto) {
                notExistList.add(source);
                continue;
            }
            if (0 == dto.getDownloadStatus()) {
                existMainList.add(source);
            } else {
                existList.add(source);
            }
        }

        if (!CollectionUtils.isEmpty(notExistList)) {
            // 不存在保存mongo等待重新触发
            directSaveMongo(notExistList, AmazonHandleStatusEnum.WAIT_DOWNLOAD, DownloadStatusEnum.WAIT);
        }
        if (!CollectionUtils.isEmpty(existMainList)) {
            // 不存在保存mongo等待重新触发
            directSaveMongo(existMainList, AmazonHandleStatusEnum.WAIT_HANDLE, DownloadStatusEnum.FINISH);
        }

        // 存在的订单直接触发销售出库单
        if (!CollectionUtils.isEmpty(existList)) {
            // 去重
            List<ReportFulfilledShipmentsMongoDTO> mqList = new ArrayList<>(
                    existList.stream().collect(Collectors.toMap(
                    MongoSuperDTO::getUniqueId,
                    obj -> obj,
                    (existing, replacement) -> existing
            )).values());

            JobTaskDTO jobTaskDTO = new JobTaskDTO();
            jobTaskDTO.setDictPlatform(PlatformDictEnum.AMAZON.getCode());
            jobTaskDTO.setApiCode(BusinessTypeEnum.SO_OUT_STOCK.getCode());
            jobTaskDTO.setIntervalTime(86400);
            jobTaskDTO.setStatus(3);
            jobTaskDTO.setRetryTimes(0);
            jobTaskDTO.setApiName("亚马逊物流销售");
            jobTaskDTO.setCreateTime(LocalDateTime.now());
            jobTaskDTO.setPlatformCategory(PlatformCategoryEnum.THIRD_SYSTEM.getCode());
            jobTaskDTO.setBillType(BusinessTypeEnum.SO_OUT_STOCK.getCode());
            jobTaskDTO.setOperateType("pull");
            jobTaskDTO.setSourceList(mqList);
            RequestDTO dto = new RequestDTO();
            dto.setJobTaskDTO(jobTaskDTO);
            // 事务处理
            businessService.batchPullProcessBusiness(jobTaskDTO.getPlatformCategory(), jobTaskDTO.getDictPlatform(), jobTaskDTO.getBillType(), jobTaskDTO, dto.getPlatformApiEnum(), 100);
        }
        return allList.size();
    }

    private List<ReportFulfilledShipmentsMongoDTO> fillData(List<ReportFulfilledShipmentsMongoDTO> allList) {
        // 仓储中心列表
        List<String> centerCodeList = allList.stream()
                .map(ReportFulfilledShipmentsMongoDTO::getFulfillmentCenterId)
                .distinct()
                .collect(Collectors.toList());

        // 查询仓库中心配置
        List<CfgAmzFulfillmentCenterEntity> centerEntityList = amazonDownloadService.feignQueryFulfillmentCenterlist(centerCodeList);
        Map<String, CfgAmzFulfillmentCenterEntity> centerMap = centerEntityList
                .stream()
                .collect(Collectors.toMap(CfgAmzFulfillmentCenterEntity::getCode, Function.identity()));

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
                .collect(Collectors.toList());
    }


    private List<PlatformAmazonOrderDTO> findAllOrderMongoData(List<String> uniqueIds) {
        Query query = new Query();
        query.addCriteria(
                Criteria.where("uniqueId").in(uniqueIds)
        );
        return mongoTemplate.find(query, PlatformAmazonOrderDTO.class, MongoTableNameContant.THIRD_SYSTEM_AMAZON_ORDER);
    }

    private void directSaveMongo(List<ReportFulfilledShipmentsMongoDTO> handleList, AmazonHandleStatusEnum handleStatusEnum, DownloadStatusEnum downloadStatusEnum) {
        List<PlatformAmazonFulfilledShipmentsDTO> sourceList = handleList.stream()
                .map(e -> SdkSoOutStockConverter.INSTANCE.sourceDtoToOutStockDto(e,
                        e.getReportId(),
                        e.getShopId(),
                        StrUtil.format("{}_{}_{}", e.getAmazonOrderId(), e.convertShipmentDate(), e.getShopId()),
                        handleStatusEnum.getCode(),
                        downloadStatusEnum.getCode()
                ))
                .collect(Collectors.toList());
        businessService.handleSaveOrUpdateMongo(sourceList, MongoTableNameContant.THIRD_SYSTEM_AMAZON_SO_OUT_STOCK, PlatformAmazonFulfilledShipmentsDTO.class, new ArrayList<>());
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
        CfgAmzFulfillmentCenterEntity centerEntity = centerMap.get(e.getFulfillmentCenterId());

        // Map<国家代号, 店铺>
        Map<String, ShopInfoEntity> curMap = shopMap.get(e.getPlatformShopCode());

        // 设置仓库中心对应仓库
        if (null != centerEntity && !curMap.isEmpty()){
            ShopInfoEntity shopInfo = curMap.get(centerEntity.getCountry());
            e.setWarehouseId(shopInfo.getWarehouseId());
            if (!CollectionUtils.isEmpty(warehouseMap) && warehouseMap.containsKey(shopInfo.getWarehouseId())){
                WarehouseDTO.ListDTO warehouseDTO = warehouseMap.get(shopInfo.getWarehouseId());
                e.setWarehouseName(warehouseDTO.getName());
                e.setWarehouseOrgId(warehouseDTO.getOrgId());
                e.setWarehouseOrgName(warehouseDTO.getOrgName());
            }
        }
        if (e.hasMultiChannel()) {
            // 多渠道订单
            parseMultiChannel(e, timeList, curMap, centerEntity);
        } else {
            // B2C订单
            parseB2cOrder(e, timeList, curMap, centerEntity);
        }

        return e;
    }

    /**
     * 补充信息(B2C销售订单)
     */
    private static void parseB2cOrder(ReportFulfilledShipmentsMongoDTO e, List<CfgTimezoneEntity> timeList, Map<String, ShopInfoEntity> curMap, CfgAmzFulfillmentCenterEntity centerEntity) {
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


}
