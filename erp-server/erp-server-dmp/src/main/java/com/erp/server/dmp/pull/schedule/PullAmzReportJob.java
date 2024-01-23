package com.erp.server.dmp.pull.schedule;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.business.constant.MongoTableNameContant;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.enums.PlatformDictEnum;
import com.erp.model.dmp.dto.AmazonJobParamDTO;
import com.erp.model.dmp.entity.AmzReportScheduleEntity;
import com.erp.model.dmp.entity.CfgAmzReportTypeEntity;
import com.erp.model.dmp.enums.ReportScheduleCancelStatusEnum;
import com.erp.model.dmp.enums.ReportScheduleSubscribedStatusEnum;
import com.erp.model.dmp.enums.ReportScheduleSubscribedTypeEnum;
import com.erp.model.oms.dto.ShopInfoDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.rpc.dmp.feign.DmpAmazonFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.wms.feign.WmsFbaInventoryFeign;
import com.erp.sdk.oms.amz.spapi.dto.ReportInfoMongoDTO;
import com.erp.sdk.oms.amz.spapi.enums.AmazonMarketplaceEnum;
import com.erp.sdk.oms.amz.spapi.enums.AmazonRequestTypeRateLimiterEnum;
import com.erp.sdk.oms.amz.spapi.handler.AmazonFbaShipmentHandler;
import com.erp.sdk.oms.amz.spapi.handler.AmazonListingHandler;
import com.erp.sdk.oms.amz.spapi.handler.AmazonOrderHandler;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.pull.thread.PlatformDataThread;
import com.erp.server.dmp.service.*;
import com.erp.server.dmp.service.impl.BusinessServiceImpl;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 亚马逊报告相关任务
 */
@Component
@Slf4j
@EnableScheduling
public class PullAmzReportJob {

    @Resource
    private PlatformDataThread platformDataThread;
    @Resource(name = "pullErpOpenApi")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
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
    private AmzReportScheduleService reportScheduleService;
    @Resource
    private WmsFbaInventoryFeign wmsFbaInventoryFeign;
    @Resource
    private DmpAmazonFeign dmpAmazonFeign;
    @Resource
    private DmpPushTaskService dmpPushTaskService;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private MongoTemplate mongoTemplate;
    @Resource
    private CfgSettingService cfgSettingService;
    @Resource
    private CfgAmzReportTypeService cfgAmzReportTypeService;
    @Resource
    private AmzReportTaskService amzReportTaskService;
    @Resource
    private AmzReportScheduleService amzReportScheduleService;

    /**
     * 亚马逊请求报表计划任务
     */
    @XxlJob("amazonReportScheduleJob")
    public ReturnT<String> amazonReportScheduleJob() {
        Integer size = 20;
        String jobParamStr = XxlJobHelper.getJobParam();
        if (StrUtil.isNotBlank(jobParamStr)) {
            JSONObject jobParam = JSON.parseObject(jobParamStr);
            size = jobParam.getInteger("size");
        }
        // 根据报告ID和状态获取reportDocumentId
        XxlJobHelper.log("[亚马逊请求报表计划任务] 任务开始 size={}", size);
        // 根据状态查询未请求的数据
        List<AmzReportScheduleEntity> reportScheduleEntityList = reportScheduleService.findList(
                ReportScheduleSubscribedStatusEnum.WAIT.getCode(),
                ReportScheduleCancelStatusEnum.NONE.getCode(),
                size
        );
        if (CollectionUtil.isEmpty(reportScheduleEntityList)) {
            XxlJobHelper.log("[亚马逊请求报表计划任务] 任务结束,无需要更新的信息");
            return ReturnT.SUCCESS;
        }
        OffsetDateTime currentDateTime = OffsetDateTime.now(ZoneOffset.UTC);

        reportScheduleEntityList.forEach(reportSchedule -> {
            try {
                amzReportHandleService.createReportSchedule(reportSchedule, currentDateTime);
            } catch (Exception e) {
                String errorMsg = JSONUtil.toJsonStr(e);
                XxlJobHelper.log("[亚马逊请求报表计划任务] 创建亚马逊报表计划失败：reportId={}, error={}",
                        reportSchedule.getAmzReportScheduleId(),
                        errorMsg
                );
            }
        });
        XxlJobHelper.log("[亚马逊获取报表计划请求任务] 任务结束");
        return ReturnT.SUCCESS;
    }

    /**
     * 报告下载-任务1
     * 创建【亚马逊报告】亚马逊-ERP
     */
    @XxlJob("amazonReportJob")
    public ReturnT<String> amazonReportJob() {
        // 执行参数
        String jobParamStr = XxlJobHelper.getJobParam();
        AmazonJobParamDTO.ReportJobDTO jobParamDTO = AmazonJobParamDTO.ReportJobDTO.init(jobParamStr);
        // 根据报告ID和状态获取reportDocumentId
        XxlJobHelper.log("[创建【亚马逊报告】亚马逊-ERP] 任务开始 当前执行参数={}", JSONUtil.toJsonStr(jobParamDTO));
        // 查询报告类型配置
        List<CfgAmzReportTypeEntity> reportTypeConfigList = cfgAmzReportTypeService.findActive();
        if (CollectionUtils.isEmpty(reportTypeConfigList)) {
            XxlJobHelper.log("[创建【亚马逊报告】亚马逊-ERP] amazonReportJob 任务结束,未找到需执行报告类型配置");
            return ReturnT.SUCCESS;
        }
        // 报告类型配置Map
        Map<String, CfgAmzReportTypeEntity> reportTypeMap = reportTypeConfigList.stream().collect(Collectors.toMap(CfgAmzReportTypeEntity::getReportType, Function.identity()));

        // 报告类型根据配置分组为Map
        Map<String, List<CfgAmzReportTypeEntity>> reportTypeConfigMap = reportTypeConfigList.stream().collect(Collectors.groupingBy(CfgAmzReportTypeEntity::getReportGroup));

        // 查询指定或所有已授权店铺
        ShopInfoDTO.ListParamDTO listParamDTO = new ShopInfoDTO.ListParamDTO(AuthStatusEnum.ALREADY.getCode(), PlatformDictEnum.AMAZON.getCode(), jobParamDTO.getShopIdList());
        List<ShopInfoEntity> shopInfoEntityList = shopInfoFeign.listByParams(listParamDTO);
        if (CollectionUtils.isEmpty(shopInfoEntityList)) {
            XxlJobHelper.log("[创建【亚马逊报告】亚马逊-ERP] amazonReportJob 任务结束,未找到需执行的任务记录");
            return ReturnT.SUCCESS;
        }
        List<String> shopIds = shopInfoEntityList.stream().map(ShopInfoEntity::getId).collect(Collectors.toList());
        List<String> recordTypeList = reportTypeConfigList.stream().map(CfgAmzReportTypeEntity::getReportType).distinct().collect(Collectors.toList());
        // (任务参数优选)
        if (!CollectionUtils.isEmpty(jobParamDTO.getRecordTypeList())) {
            recordTypeList = jobParamDTO.getRecordTypeList();
        }
        if (!CollectionUtils.isEmpty(jobParamDTO.getShopIdList())) {
            shopIds = jobParamDTO.getShopIdList();
        }
        LocalDateTime minTime = LocalDateTime.now(ZoneId.systemDefault());
        if (null != jobParamDTO.getIgnoreNextReportCreationTime()) {
            if (jobParamDTO.getIgnoreNextReportCreationTime()) {
                minTime = null;
            }
        }

        // 查询所有待请求的计划任务
        List<AmzReportScheduleEntity> reportScheduleEntityList = amzReportScheduleService.listByParams(
                ReportScheduleSubscribedStatusEnum.ALREADY.getCode(),
                ReportScheduleCancelStatusEnum.NONE.getCode(),
                ReportScheduleSubscribedTypeEnum.MANUAL.getCode(),
                recordTypeList,
                shopIds,
                minTime
        );

        String platform = PlatformDictEnum.AMAZON.getCode();
        // 根据groupId分组店铺
        Map<String, List<ShopInfoEntity>> taskGroupMap = shopInfoEntityList.stream()
                // 平台类型:sellerId:请求的端点区域
                .collect(Collectors.groupingBy(e ->
                        StrUtil.format("{}:{}:{}",
                                platform,
                                e.getPlatformShopCode(),
                                AmazonMarketplaceEnum.getByCountryCode(e.getDictCountryCode()).getEndpointsEnum().name())
                ));
        // 当前时间
        OffsetDateTime currentDateTime = OffsetDateTime.now(ZoneOffset.UTC);

        taskGroupMap.forEach((key, value) -> threadPoolTaskExecutor.execute(() -> {
            // 处理
            amzReportTaskService.handlerCreateReportTask(key,
                    value,
                    jobParamDTO.getSize(),
                    currentDateTime,
                    reportScheduleEntityList,
                    reportTypeConfigMap,
                    reportTypeMap
            );
            log.info("[创建【亚马逊报告】亚马逊-ERP] amazonReportJob 当前线程执行完毕,group={}", key);
            XxlJobHelper.log("[创建【亚马逊报告】亚马逊-ERP] amazonReportJob 当前线程执行完毕");
        }));

        XxlJobHelper.log("[创建【亚马逊报告】亚马逊-ERP] amazonReportJob 所有任务执行完毕");
        return ReturnT.SUCCESS;
    }


    /**
     * 检查最新【亚马逊报告】亚马逊-ERP
     */
    @XxlJob("amazonCheckReportJob")
    public ReturnT<String> amazonCheckReportJob() {
        // 执行参数
        String jobParamStr = XxlJobHelper.getJobParam();
        AmazonJobParamDTO.ReportBaseDTO jobParamDTO = AmazonJobParamDTO.ReportBaseDTO.init(jobParamStr);
        // 根据报告ID和状态获取reportDocumentId
        XxlJobHelper.log("[检查最新【亚马逊报告】亚马逊-ERP]  任务开始 当前执行参数={}", JSONUtil.toJsonStr(jobParamDTO));
        // 查询指定或所有已授权店铺
        ShopInfoDTO.ListParamDTO listParamDTO = new ShopInfoDTO.ListParamDTO(AuthStatusEnum.ALREADY.getCode(), PlatformDictEnum.AMAZON.getCode(), jobParamDTO.getShopIdList());
        List<ShopInfoEntity> shopInfoEntityList = shopInfoFeign.listByParams(listParamDTO);
        if (CollectionUtils.isEmpty(shopInfoEntityList)) {
            XxlJobHelper.log("[检查最新【亚马逊报告】亚马逊-ERP]  任务结束,未找到需执行的任务记录");
            return ReturnT.SUCCESS;
        }
        // 根据groupId分组店铺id
        Map<String, List<ShopInfoEntity>> taskGroupMap = shopInfoEntityList.stream().collect(Collectors.groupingBy(ShopInfoEntity::getPlatformShopCode));

        XxlJobHelper.log("[检查最新【亚马逊报告】亚马逊-ERP]  开始,预计分组线程数量={}", taskGroupMap.size());
        OffsetDateTime currentDateTime = OffsetDateTime.now(ZoneOffset.UTC);

        CompletableFuture<Void> allOf = CompletableFuture.allOf(taskGroupMap.entrySet().stream()
                .map(entry -> CompletableFuture.runAsync(() -> {
                    // 异步任务的逻辑
                    String key = entry.getKey();
                    List<ShopInfoEntity> value = entry.getValue();
                    handlerCheckReport(key, value, jobParamDTO, currentDateTime);
                    log.info("[检查最新【亚马逊报告】亚马逊-ERP]  当前线程执行完毕");
                    XxlJobHelper.log("[检查最新【亚马逊报告】亚马逊-ERP]  当前线程执行完毕");
                })).toArray(CompletableFuture[]::new));

        allOf.thenRun(() -> XxlJobHelper.log("[检查最新【亚马逊报告】亚马逊-ERP] 所有任务执行完毕")).join();
        return ReturnT.SUCCESS;
    }

    private void handlerCheckReport(String sellerId, List<ShopInfoEntity> value, AmazonJobParamDTO.ReportBaseDTO jobParamDTO, OffsetDateTime currentDateTime) {

    }


//    /**
//     * 拉取亚马逊报表任务
//     */
//    @XxlJob("amazonReportDownload")
//    public ReturnT<String> reportDownload() {
//        // 报表处理的开始时间
//        String jobParamStr = XxlJobHelper.getJobParam();
//        Integer size = 10;
//        if (StrUtil.isNotBlank(jobParamStr)) {
//            JSONObject jobParam = JSON.parseObject(jobParamStr);
//            size = jobParam.getInteger("size");
//        }
//        XxlJobHelper.log("[拉取亚马逊报表任务] 任务开始：size={}", size);
//
//        // 根据状态查询未下载数据
//        ReportInfoMongoDTO orderMongoDTO = ReportInfoMongoDTO.getByNotCheckDownload();
//        List<ReportInfoMongoDTO> reportInfoMongoDTOList = mongoService.findMongoData(orderMongoDTO, 1, size, MongoTableNameContant.THIRD_SYSTEM_AMAZON_REPORT, ReportInfoMongoDTO.class);
//
//        if (CollectionUtil.isEmpty(reportInfoMongoDTOList)) {
//            XxlJobHelper.log("[拉取亚马逊报表任务] 任务结束：亚马逊已授权店铺列表为空");
//            return ReturnT.SUCCESS;
//        }
//
//        reportInfoMongoDTOList.forEach(mongoDTO -> {
//            try {
//                amzReportHandleService.checkAndDownload(mongoDTO);
//            } catch (Exception e) {
//                XxlJobHelper.log("[拉取亚马逊报表任务] 拉取亚马逊报表失败：reportId={},msg={}, json={}",
//                        mongoDTO.getReportId(),
//                        ExceptionUtil.stacktraceToString(e, 2000),
//                        JSONUtil.toJsonStr(e)
//                );
//            }
//        });
//        XxlJobHelper.log("[拉取亚马逊报表任务] 任务结束");
//        return ReturnT.SUCCESS;
//    }
}
