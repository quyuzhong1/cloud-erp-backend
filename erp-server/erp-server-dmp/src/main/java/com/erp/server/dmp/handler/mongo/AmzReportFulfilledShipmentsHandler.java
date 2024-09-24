package com.erp.server.dmp.handler.mongo;

import cn.hutool.core.util.StrUtil;
import com.common.business.constant.MongoTableNameContant;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.MongoSuperDTO;
import com.common.business.dto.RequestDTO;
import com.common.business.dto.UniqueDto;
import com.common.business.enums.BusinessTypeEnum;
import com.common.business.enums.PlatformCategoryEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.wrapper.FeignQuery;
import com.erp.model.dmp.entity.CfgTimezoneEntity;
import com.erp.model.dmp.entity.DmpMongoHandleTaskEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.entity.CfgAmzFulfillmentCenterEntity;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.wms.feign.WmsAmazonFeign;
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
import org.apache.commons.lang.StringUtils;
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
    private MongoTemplate mongoTemplate;
    @Resource
    private BusinessServiceImpl businessService;
    @Resource
    private DmpMongoHandleTaskService dmpMongoHandleTaskService;
    @Resource
    private AmazonDownloadService amazonDownloadService;


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

//        log.warn("亚马逊物流销售报告处理服务处理：转换前的数据={}", JSONUtil.toJsonStr(allList));
        // 补充数据
        List<ReportFulfilledShipmentsMongoDTO> canHandleList = amazonDownloadService.reportFulfillmentFillData(allList);
//        log.warn("亚马逊物流销售报告处理服务处理：转换后的数据={}", JSONUtil.toJsonStr(allList));

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
        // 按异常数据(无对应销售渠道无法解析到本地时区)分组
        Map<Boolean, List<ReportFulfilledShipmentsMongoDTO>> groupList = existList.stream().collect(Collectors.groupingBy(e ->
                null == e.getPaymentsDateLocale() ||
                        null == e.getShipmentDateLocale() ||
                        null == e.getPurchaseDateLocale()
        ));
        // 异常数据
        List<ReportFulfilledShipmentsMongoDTO> errorList = groupList.get(true);
        // 正常数据
        List<ReportFulfilledShipmentsMongoDTO> canHandleExistList= groupList.get(false);
        if (!CollectionUtils.isEmpty(errorList)) {
            // 不存在保存mongo等待重新触发
            directSaveMongo(existMainList, AmazonHandleStatusEnum.ERROR, DownloadStatusEnum.FINISH);
        }


        // 存在的订单直接触发销售出库单
        if (!CollectionUtils.isEmpty(canHandleExistList)) {
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

}
