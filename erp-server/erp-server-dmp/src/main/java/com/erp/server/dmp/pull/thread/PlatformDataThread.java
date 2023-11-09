package com.erp.server.dmp.pull.thread;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.business.constant.MongoTableNameContant;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.RequestDTO;
import com.common.business.enums.PlatformApiEnum;
import com.common.core.utils.MapUtil;
import com.erp.sdk.oms.amz.spapi.dto.ReportInfoMongoDTO;
import com.erp.model.dmp.entity.DmpErrorLogEntity;
import com.erp.sdk.oms.amz.spapi.api.ReportsApi;
import com.erp.sdk.oms.amz.spapi.enums.AmazonMarketplaceEnum;
import com.erp.sdk.oms.amz.spapi.enums.AmazonReportRecordTypeEnum;
import com.erp.sdk.oms.amz.spapi.model.reports.Report;
import com.erp.sdk.oms.amz.spapi.model.reports.ReportDocument;
import com.erp.sdk.oms.amz.spapi.model.reports.ReportList;
import com.erp.sdk.oms.amz.spapi.utils.AmazonSpApiReportUtils;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.service.DmpErrorLogService;
import com.erp.server.dmp.service.PlatformApiTaskService;
import com.erp.server.dmp.service.impl.BusinessServiceImpl;
import com.xxl.job.core.context.XxlJobHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 拉取平台数据线程
 * @author Cloud
 */
@Component
@Slf4j
public class PlatformDataThread {
    @Resource
    private PlatformApiTaskService platformApiTaskService;
    @Resource
    private DmpErrorLogService dmpErrorLogService;
    @Resource
    private RedisTemplate<String, String> template;
    @Resource
    private BusinessServiceImpl businessService;
    @Resource
    private MongoService mongoService;


    @Async("pullErpOpenApi")
    public void pullOrder(JobTaskDTO jobTaskDTO) {
        PlatformApiEnum enumByType = PlatformApiEnum.getEnumByType(jobTaskDTO.getApiCode());
        RequestDTO dto = new RequestDTO();
        dto.setPlatformApiEnum(enumByType);
        dto.setJobTaskDTO(jobTaskDTO);
        try {
            log.info("发起异步调用平台【{}】店铺【{}】任务【{}】", dto.getJobTaskDTO().getDictPlatform(),dto.getJobTaskDTO().getShopName(), dto.getJobTaskDTO().getApiName());
            businessService.pullProcessBusiness(jobTaskDTO.getPlatformCategory(), jobTaskDTO.getDictPlatform(),jobTaskDTO.getBillType(), jobTaskDTO);
            Boolean aBoolean = platformApiTaskService.updateTaskStateById(jobTaskDTO, 0);
            if (!aBoolean) {
                throw new RuntimeException("修改任务下次执行时间失败！");
            }
        } catch (Exception e) {
            log.error(" {}拉取数据错误dto={}", jobTaskDTO.getDictPlatform(), JSONUtil.toJsonStr(dto), e);
            Boolean aBoolean = platformApiTaskService.updateTaskStateById(jobTaskDTO, 1);
            String message = e.getMessage();
            if (!aBoolean) {
                message = "更新任务状态失败";
            }
            DmpErrorLogEntity dmpErrorLogEntity = new DmpErrorLogEntity(jobTaskDTO.getId(), JSONUtil.toJsonStr(dto),message, JSONUtil.toJsonStr(e.getStackTrace()));
            dmpErrorLogService.save(dmpErrorLogEntity);
        }
    }

    /**
     * 执行任务
     * @param taskName
     */
    public void executeTask(String taskName) {
        // 获取请求任务
        String o = template.opsForList().rightPop(taskName);
        if(ObjectUtils.isEmpty(o) || "null".equals(o)) {
            return;
        }
        JobTaskDTO orderJobTask = JSONObject.parseObject(o, JobTaskDTO.class);
        if (orderJobTask == null) {
            return;
        }
        String taskKey = StrUtil.format("{}-{}-{}", orderJobTask.getDictPlatform(), orderJobTask.getShopId(), orderJobTask.getApiCode());
        if(ObjectUtils.isEmpty(template.opsForValue().get(taskKey))) {
            pullOrder(orderJobTask);
            template.opsForValue().set(taskKey,"text",10, TimeUnit.SECONDS);
        }else {
            template.opsForList().leftPush(taskName, JSONObject.toJSONString(orderJobTask));
        }
    }

    /**
     * 查询报告文档的URL并推送到redis
     */
    @Transactional(rollbackFor = Exception.class)
    public void findUrlAndSend(String tableName, ReportInfoMongoDTO report) throws Exception{
        if (StringUtils.isBlank(report.getReportDocumentUrl()) || 1 == report.getReportDocumentUrlStatus()) {
            return;
        }
        String currentMarketplaceId = report.getMarketplaceIds().stream().findFirst().orElse(null);
        AmazonMarketplaceEnum marketplaceEnum = AmazonMarketplaceEnum.getByMarketplaceId(currentMarketplaceId);
        if (null == marketplaceEnum) {
            XxlJobHelper.log("[亚马逊获取报表文档链接] 参数异常：找不到对应MarketplaceIds， reportId+{}",
                    report.getMarketplaceIds(),
                    report.getReportId());
            return;
        }
        // 当前报告的类型
        AmazonReportRecordTypeEnum recordTypeEnum = AmazonReportRecordTypeEnum.getByRecordType(report.getReportType());

        //查询当前报表ID的文档链接
        ReportsApi reportsApi = ReportsApi.initApi(marketplaceEnum.getEndpointsEnum());
        ReportDocument reportDocument = reportsApi.getReportDocument(report.getReportDocumentId());
        report.setReportDocumentUrl(reportDocument.getUrl());
        report.setReportDocumentUrlStatus(1);
        MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(report), MapUtil.class);
        ReportInfoMongoDTO updateDto = new ReportInfoMongoDTO(report.getReportId());
        mongoService.updateMongoData(updateDto, mapUtil, tableName, ReportInfoMongoDTO.class);

        if(!recordTypeEnum.isDirectSaveMongo()){
            // 添加到缓存
            String key = StrUtil.format(RedisCacheConstants.REDIS_AMAZON_REPORT_DOCUMENT_URL, report.getReportType(), marketplaceEnum.getMarketplaceId());
            template.opsForList().leftPush(key, report.getReportDocumentUrl());
        } else {
            // 下载文档内容
            List<?> downloadList = AmazonSpApiReportUtils.downloadAndParse(reportDocument.getUrl(), recordTypeEnum.getAndCheckMongoDTOClass());
            // 直接保存mongo
            if(CollectionUtil.isNotEmpty(downloadList)){
                mongoService.saveMongoDataMult(downloadList, recordTypeEnum.getMongoTableName());
            }
        }
    }

    /**
     * 更新或保存报表
     */
    @Transactional(rollbackFor = Exception.class)
    public void checkAndSaveMongo(ReportList reportList, String marketplaceId) {
        if (CollectionUtil.isEmpty(reportList)) {
            XxlJobHelper.log("[拉取亚马逊报表任务] 无报告信息：marketplaceId={}", marketplaceId);
            return;
        }
        // 转换
        List<ReportInfoMongoDTO> sourceList = reportList.stream()
                .map(this::initAmazonReportMongoDTO)
                // 只保存已完成的报表
                .filter(e -> Report.ProcessingStatusEnum.DONE.getValue().equalsIgnoreCase(e.getProcessingStatus()))
                .collect(Collectors.toList());
        // 新增报表
        List<ReportInfoMongoDTO> insertList = new ArrayList<>();

        String tableName = MongoTableNameContant.THIRD_SYSTEM_AMAZON_REPORT;
        for (ReportInfoMongoDTO sourceReport : sourceList) {
            ReportInfoMongoDTO reportMongoDTO = ReportInfoMongoDTO.getReportId(sourceReport.getReportId());
            List<ReportInfoMongoDTO> mongoData = mongoService.findMongoData(reportMongoDTO, 0, 0, tableName, ReportInfoMongoDTO.class);
            if (CollectionUtil.isEmpty(mongoData)) {
                insertList.add(sourceReport);
            }
        }
        if (CollectionUtil.isNotEmpty(insertList)) {
            mongoService.saveMongoDataMult(insertList, tableName);
        }
    }

    /**
     * 转换mongo的DTO
     */
    private ReportInfoMongoDTO initAmazonReportMongoDTO(Report report) {
        return new ReportInfoMongoDTO()
                .setMarketplaceIds(report.getMarketplaceIds())
                .setReportId(report.getReportId())
                .setReportType(report.getReportType())
                .setDataStartTime(report.getDataStartTime().withOffsetSameInstant(ZoneOffset.of("+8")).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .setDataEndTime(report.getDataEndTime().withOffsetSameInstant(ZoneOffset.of("+8")).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .setReportScheduleId(StringUtils.isNotBlank(report.getReportScheduleId()) ? report.getReportScheduleId() : "")
                .setCreatedTime(report.getCreatedTime().withOffsetSameInstant(ZoneOffset.of("+8")).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .setProcessingStatus(report.getProcessingStatus().getValue())
                .setProcessingStartTime(report.getProcessingStartTime().withOffsetSameInstant(ZoneOffset.of("+8")).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .setProcessingEndTime(report.getProcessingEndTime().withOffsetSameInstant(ZoneOffset.of("+8")).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .setReportDocumentId(report.getReportDocumentId())
                .setReportDocumentUrl("")
                .setReportDocumentUrlStatus(0)
                .setReportCancelStatus(0)
                ;
    }
}
