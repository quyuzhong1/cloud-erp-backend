package com.erp.server.dmp.handler.report;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.common.business.constant.MongoTableNameContant;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.PlatformSoOutStockDTO;
import com.common.business.dto.RequestDTO;
import com.common.business.enums.*;
import com.common.core.utils.date.DateUtil;
import com.erp.model.dmp.entity.AmzReportInfoEntity;
import com.erp.model.dmp.entity.AmzReportTaskEntity;
import com.erp.model.dmp.entity.CfgTimezoneEntity;
import com.erp.model.dmp.enums.CleanStatusEnum;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.wms.dto.SoOutstockDTO;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.sdk.oms.amz.spapi.convert.SdkSoOutStockConverter;
import com.erp.sdk.oms.amz.spapi.csv.ReportFulfilledShipmentsCsvEntity;
import com.erp.sdk.oms.amz.spapi.dto.PlatformAmazonFulfilledShipmentsDTO;
import com.erp.sdk.oms.amz.spapi.dto.PlatformAmazonOrderDTO;
import com.erp.sdk.oms.amz.spapi.enums.AmazonHandleStatusEnum;
import com.erp.sdk.oms.amz.spapi.enums.AmazonMarketplaceEnum;
import com.erp.sdk.oms.amz.spapi.handler.AmazonFulfilledShipmentsHandler;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.service.CfgTimezoneService;
import com.erp.server.dmp.service.impl.BusinessServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.util.StringUtil;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
@Component("amzReportFulfilledShipmentsHandler")
public class AmzReportFulfilledShipmentsHandler extends AmzReportBusinessHandler {

    @Resource
    private BusinessServiceImpl businessService;
    @Resource
    private SoB2cFeign soB2cFeign;
    @Resource
    private CfgTimezoneService cfgTimezoneService;
    @Resource
    private MongoTemplate mongoTemplate;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void businessHandler(AmzReportTaskEntity taskEntity, AmzReportInfoEntity reportInfo, JSONArray jsonArray) {
        log.debug("亚马逊物流销售报告处理服务处理：jsonArray={}", JSONUtil.toJsonStr(jsonArray));
        if (CollectionUtils.isEmpty(jsonArray)){
            log.warn("亚马逊物流销售报告处理服务处理结束：无配置订单:reportId={}", reportInfo.getReportId());
            return;
        }
        List<ReportFulfilledShipmentsCsvEntity> allList = JSONUtil.toList(jsonArray, ReportFulfilledShipmentsCsvEntity.class);
        // 根据报告市场匹配国家
        String currentMarketplace = Arrays.stream(reportInfo.getMarketplaceIds().split(",")).findFirst().orElse(null);
        if (StringUtils.isNotBlank(currentMarketplace)){
            AmazonMarketplaceEnum marketplaceEnum = AmazonMarketplaceEnum.getByMarketplaceId(currentMarketplace);
            CfgTimezoneEntity timeZoneEntity = cfgTimezoneService.getAndCacheByCountry(marketplaceEnum.getCountryCode());
            if (null != timeZoneEntity){
                allList.forEach(e-> {
                    // 设置所有本地时区
                    e.checkAndSetAllDateLocale(timeZoneEntity.getUtcDiffHour());
                });
            }
        }

        List<String> uniqueIds = allList.stream()
                .map(e -> StrUtil.format("{}_{}", e.getAmazonOrderId(), taskEntity.getShopId()))
                .distinct()
                .collect(Collectors.toList());
        // 查询亚马逊订单是否存在
//        List<SoB2cEntity> existOrderList = soB2cFeign.getByPlatformCode(amazonOrderIds, PlatformDictEnum.AMAZON.getCode(), taskEntity.getShopId());
        List<PlatformAmazonOrderDTO> existOrderList = findAllMongoData(uniqueIds, taskEntity.getShopId());

        if (CollectionUtils.isEmpty(existOrderList)){
            // 都不存在直接保存mongo等待重新触发
            // 不存在保存mongo等待重新触发
            directSaveMongo(taskEntity, allList, AmazonHandleStatusEnum.WAIT_DOWNLOAD);
            return;
        }
        Map<String, PlatformAmazonOrderDTO> existMap = existOrderList.stream()
                .collect(Collectors.toMap(e -> e.getOrder().getAmazonOrderId(), Function.identity()));

        List<ReportFulfilledShipmentsCsvEntity> existList = new LinkedList<>();
        List<ReportFulfilledShipmentsCsvEntity> notExistList = new LinkedList<>();
        List<ReportFulfilledShipmentsCsvEntity> existMainList = new LinkedList<>();

        for (ReportFulfilledShipmentsCsvEntity source : allList) {
            PlatformAmazonOrderDTO dto = existMap.get(source.getAmazonOrderId());
            if (null == dto){
                notExistList.add(source);
                continue;
            }
            if (0 == dto.getDownloadStatus()){
                existMainList.add(source);
            } else {
                existList.add(source);
            }
        }

        if (!CollectionUtils.isEmpty(notExistList)){
            // 不存在保存mongo等待重新触发
            directSaveMongo(taskEntity, notExistList, AmazonHandleStatusEnum.WAIT_DOWNLOAD);
        }
        if (!CollectionUtils.isEmpty(existMainList)){
            // 不存在保存mongo等待重新触发
            directSaveMongo(taskEntity, existMainList, AmazonHandleStatusEnum.WAIT_HANDLE);
        }

        // 存在的订单直接触发销售出库单
        if (!CollectionUtils.isEmpty(existList)){
            JobTaskDTO jobTaskDTO = new JobTaskDTO();
            jobTaskDTO.setShopId(taskEntity.getShopId());
            jobTaskDTO.setShopName(taskEntity.getShopId());
            jobTaskDTO.setDictPlatform(PlatformDictEnum.AMAZON.getCode());
            jobTaskDTO.setApiCode(BusinessTypeEnum.SO_OUT_STOCK.getCode());
            jobTaskDTO.setIntervalTime(86400);
            jobTaskDTO.setStatus(3);
            jobTaskDTO.setRetryTimes(0);
            jobTaskDTO.setApiName("亚马逊物流销售");
            jobTaskDTO.setCreateTime(LocalDateTime.now());
            // 转换时间
            LocalDateTime parseTime = LocalDateTimeUtil.parse(taskEntity.getReqDataEndTime(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            jobTaskDTO.setUpdateTime(DateUtil.utcSamePlus8(parseTime));

            jobTaskDTO.setPlatformApiId(taskEntity.getReportId());
            jobTaskDTO.setPlatformCategory(PlatformCategoryEnum.THIRD_SYSTEM.getCode());
            jobTaskDTO.setBillType(BusinessTypeEnum.SO_OUT_STOCK.getCode());
            jobTaskDTO.setOperateType("pull");
            jobTaskDTO.setSourceList(existList);
            RequestDTO dto = new RequestDTO();
            dto.setJobTaskDTO(jobTaskDTO);
            // 事务处理
            businessService.batchPullProcessBusiness(jobTaskDTO.getPlatformCategory(), jobTaskDTO.getDictPlatform(), jobTaskDTO.getBillType(), jobTaskDTO, dto.getPlatformApiEnum());
        }
    }

    private List<PlatformAmazonOrderDTO> findAllMongoData(List<String> uniqueIds, String shopId) {
        Query query = new Query();
        query.addCriteria(
                Criteria.where("uniqueId").in(uniqueIds)
                        .and("shopId").is(shopId)
        );
        return mongoTemplate.find(query, PlatformAmazonOrderDTO.class, MongoTableNameContant.THIRD_SYSTEM_AMAZON_ORDER);
    }

    private void directSaveMongo(AmzReportTaskEntity taskEntity, List<ReportFulfilledShipmentsCsvEntity> allList, AmazonHandleStatusEnum handleStatusEnum) {
        List<PlatformAmazonFulfilledShipmentsDTO> sourceList = allList.stream()
                .map(e -> SdkSoOutStockConverter.INSTANCE.sourceDtoToOutStockDto(e,
                        taskEntity.getReportId(),
                        taskEntity.getShopId(),
                        StrUtil.format("{}_{}_{}", e.getAmazonOrderId(), e.convertShipmentDate(), taskEntity.getShopId()),
                        handleStatusEnum.getCode(),
                        CleanStatusEnum.NONE.getCode()
                ))
                .collect(Collectors.toList());
        businessService.handleSaveOrUpdateMongo(sourceList, MongoTableNameContant.THIRD_SYSTEM_AMAZON_SO_OUT_STOCK, PlatformAmazonFulfilledShipmentsDTO.class, new ArrayList<>());
    }
}
