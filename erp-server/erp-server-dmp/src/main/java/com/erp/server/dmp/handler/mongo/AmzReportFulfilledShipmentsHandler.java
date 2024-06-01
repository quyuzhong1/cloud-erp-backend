package com.erp.server.dmp.handler.mongo;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.constant.MongoTableNameContant;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.RequestDTO;
import com.common.business.enums.BusinessTypeEnum;
import com.common.business.enums.PlatformCategoryEnum;
import com.common.business.enums.PlatformDictEnum;
import com.erp.model.dmp.entity.CfgTimezoneEntity;
import com.erp.model.dmp.entity.DmpMongoHandleTaskEntity;
import com.erp.model.dmp.enums.CleanStatusEnum;
import com.erp.model.oms.dto.ShopInfoDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.sdk.oms.amz.spapi.convert.SdkSoOutStockConverter;
import com.erp.sdk.oms.amz.spapi.dto.*;
import com.erp.sdk.oms.amz.spapi.enums.AmazonHandleStatusEnum;
import com.erp.server.dmp.handler.DmpMongoHandler;
import com.erp.server.dmp.service.CfgTimezoneService;
import com.erp.server.dmp.service.DmpMongoHandleTaskService;
import com.erp.server.dmp.service.impl.BusinessServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer findAndFillDataOrHandle(DmpMongoHandleTaskEntity mongoHandleTaskEntity) {
        // 任务每次处理数量
        Integer handleCount = mongoHandleTaskEntity.getHandleCount();
        // 指定的mongo表
        String mongoTableName = MongoTableNameContant.DATA_REPORT_AMZ_FULFILLED_SHIPMENTS;
        // 查询
        List<ReportFulfilledShipmentsMongoDTO> allList = dmpMongoHandleTaskService.findMongoData(mongoHandleTaskEntity.getLastId(), handleCount, mongoTableName, ReportFulfilledShipmentsMongoDTO.class);
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
            directSaveMongo(canHandleList, AmazonHandleStatusEnum.WAIT_DOWNLOAD);
            return allList.size();
        }
        Map<String, PlatformAmazonOrderDTO> existMap = existOrderList.stream()
                .collect(Collectors.toMap(e -> e.getOrder().getAmazonOrderId(), Function.identity()));

        List<ReportFulfilledShipmentsMongoDTO> existList = new LinkedList<>();
        List<ReportFulfilledShipmentsMongoDTO> notExistList = new LinkedList<>();
        List<ReportFulfilledShipmentsMongoDTO> existMainList = new LinkedList<>();

        for (ReportFulfilledShipmentsMongoDTO source : allList) {
            PlatformAmazonOrderDTO dto = existMap.get(source.getAmazonOrderId());
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
            directSaveMongo(notExistList, AmazonHandleStatusEnum.WAIT_DOWNLOAD);
        }
        if (!CollectionUtils.isEmpty(existMainList)) {
            // 不存在保存mongo等待重新触发
            directSaveMongo(existMainList, AmazonHandleStatusEnum.WAIT_HANDLE);
        }

        // 存在的订单直接触发销售出库单
        if (!CollectionUtils.isEmpty(existList)) {
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
            jobTaskDTO.setSourceList(existList);
            RequestDTO dto = new RequestDTO();
            dto.setJobTaskDTO(jobTaskDTO);
            // 事务处理
            businessService.batchPullProcessBusiness(jobTaskDTO.getPlatformCategory(), jobTaskDTO.getDictPlatform(), jobTaskDTO.getBillType(), jobTaskDTO, dto.getPlatformApiEnum(), 100);
        }
        return allList.size();
    }

    private List<ReportFulfilledShipmentsMongoDTO> fillData(List<ReportFulfilledShipmentsMongoDTO> allList) {
        // 所有店铺信息Map<亚马逊账号， Map<国家代号, 店铺ID>
        Map<String, Map<String, String>> shopMap = shopInfoFeign.listByParams(
                        new ShopInfoDTO.ListParamDTO(AuthStatusEnum.ALREADY.getCode(), PlatformDictEnum.AMAZON.getCode(), null)
                )
                .stream()
                .collect(Collectors.groupingBy(ShopInfoEntity::getPlatformShopCode,
                        Collectors.toMap(ShopInfoEntity::getDictCountryCode, ShopInfoEntity::getId)));

        // 矫正时区(报告来源的时间可能不带时区)
        List<CfgTimezoneEntity> timeList = cfgTimezoneService.listAndCache();

        return allList.stream()
                .map(e -> {
                    CfgTimezoneEntity timeZoneEntity = timeList.stream()
                            .filter(t -> t.getAndParseCondition().contains(e.getSalesChannel()))
                            .findFirst()
                            .orElse(null);
                    if (null == timeZoneEntity) {
                        // 配置找不到
                        return e;
                    }
                    // 设置所有本地时区
                    e.checkAndSetAllDateLocale(timeZoneEntity.getUtcDiffHour());
                    // 记录店铺ID
                    Map<String, String> curMap = shopMap.get(e.getPlatformShopCode());
                    if (!curMap.isEmpty()) {
                        String shopId = curMap.getOrDefault(timeZoneEntity.getCountry(), "");
                        e.setShopId(shopId);
                    }
                    return e;})
                .collect(Collectors.toList());
    }


    private List<PlatformAmazonOrderDTO> findAllOrderMongoData(List<String> uniqueIds) {
        Query query = new Query();
        query.addCriteria(
                Criteria.where("uniqueId").in(uniqueIds)
        );
        return mongoTemplate.find(query, PlatformAmazonOrderDTO.class, MongoTableNameContant.THIRD_SYSTEM_AMAZON_ORDER);
    }

    private void directSaveMongo(List<ReportFulfilledShipmentsMongoDTO> handleList, AmazonHandleStatusEnum handleStatusEnum) {
        List<PlatformAmazonFulfilledShipmentsDTO> sourceList = handleList.stream()
                .map(e -> SdkSoOutStockConverter.INSTANCE.sourceDtoToOutStockDto(e,
                        e.getReportId(),
                        e.getShopId(),
                        StrUtil.format("{}_{}_{}", e.getAmazonOrderId(), e.convertShipmentDate(), e.getShopId()),
                        handleStatusEnum.getCode(),
                        CleanStatusEnum.NONE.getCode()
                ))
                .collect(Collectors.toList());
        businessService.handleSaveOrUpdateMongo(sourceList, MongoTableNameContant.THIRD_SYSTEM_AMAZON_SO_OUT_STOCK, PlatformAmazonFulfilledShipmentsDTO.class, new ArrayList<>());
    }
}
