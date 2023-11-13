package com.erp.server.dmp.service.mq;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.amazon.sqs.javamessaging.message.SQSTextMessage;
import com.common.business.constant.BusinessCommonConstants;
import com.common.business.constant.MongoTableNameContant;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MapUtil;
import com.erp.model.dmp.entity.ReportScheduleEntity;
import com.erp.sdk.oms.amz.spapi.api.ReportsApi;
import com.erp.sdk.oms.amz.spapi.dto.NotificationSQSEntity;
import com.erp.sdk.oms.amz.spapi.dto.ReportInfoMongoDTO;
import com.erp.sdk.oms.amz.spapi.dto.ReportInventoryCombineMongoDTO;
import com.erp.sdk.oms.amz.spapi.dto.ReportSuperMongoDTO;
import com.erp.sdk.oms.amz.spapi.enums.AmazonEndpointsEnum;
import com.erp.sdk.oms.amz.spapi.enums.AmazonReportRecordTypeEnum;
import com.erp.sdk.oms.amz.spapi.model.reports.Report;
import com.erp.sdk.oms.amz.spapi.model.reports.ReportDocument;
import com.erp.sdk.oms.amz.spapi.utils.AmazonSpApiReportUtils;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.service.ReportHandleService;
import com.erp.server.dmp.service.ReportScheduleService;
import com.erp.server.dmp.service.impl.BusinessServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.jms.Message;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 亚马逊SQS消息监听
 */
@Service
@Slf4j
public class JmsAmazonSqsConsumer {

    @Resource
    private MongoService mongoService;
    @Resource
    private ReportScheduleService reportScheduleService;
    @Resource
    private BusinessServiceImpl businessService;
    @Resource
    private ReportHandleService reportHandleService;

    /**
     * 监听接收消息
     * 如果有多个Factory 需要手动指定
     */
    @JmsListener(destination = "erpNotifications", containerFactory = "jmsListenerContainerFactory")
    @Transactional(rollbackFor = Exception.class, transactionManager = "mongoTransactionManager")
    public void consumerListener(Message message) throws Exception {
        SQSTextMessage textMessage = (SQSTextMessage) message;
        log.debug("接收到亚马逊SQS通知:{}", textMessage.getText());
        if (BusinessCommonConstants.hasProfile("dev")){
            // TODO 开发环境暂时过滤
            return ;
        }

        // 处理报告完成队列
        if ("REPORT_PROCESSING_FINISHED".equalsIgnoreCase(new JSONObject(textMessage.getText()).getStr("notificationType"))) {
            NotificationSQSEntity sqsEntity = JSONUtil.toBean(textMessage.getText(), NotificationSQSEntity.class);
            String processingStatus = sqsEntity.getPayload().getReportProcessingFinishedNotification().getProcessingStatus();
            // 非已完成的报表
            if (!"DONE".equalsIgnoreCase(processingStatus)) {
                return;
            }
            // 是否是需要记录的类型
            AmazonReportRecordTypeEnum recordTypeEnum = AmazonReportRecordTypeEnum.getByRecordType(sqsEntity.getPayload().getReportProcessingFinishedNotification().getReportType());
            if (null == recordTypeEnum) {
                return;
            }
            // 根据不同地区区分
            ReportsApi reportsApi = ReportsApi.initApi(AmazonEndpointsEnum.US_EAST_1);
            // 查询当前报告是否是属于系统计划报告
            Report report = reportsApi.getReport(sqsEntity.getPayload().getReportProcessingFinishedNotification().getReportId());

            String reportScheduleId = report.getReportScheduleId();
            if (StringUtils.isBlank(reportScheduleId)) {
                return;
            }
            // 查询报告计划ID是否已存在
            ReportScheduleEntity reportScheduleEntity = reportScheduleService.getByReportScheduleId(reportScheduleId);
//            ReportScheduleEntity reportScheduleEntity = reportScheduleService.getById("1722786406251237379");
            if (null == reportScheduleEntity) {
                // 不存在跳过
                return;
            }
            // 校验报告是否已存在？
            ReportInfoMongoDTO reportMongoDTO = ReportInfoMongoDTO.getReportId(report.getReportId());
            List<ReportInfoMongoDTO> mongoData = mongoService.findMongoData(reportMongoDTO, 0, 0, MongoTableNameContant.THIRD_SYSTEM_AMAZON_REPORT, ReportInfoMongoDTO.class);
            if (!CollectionUtils.isEmpty(mongoData)) {
                // 存在跳过
                return;
            }
            // 保存报告
            ReportInfoMongoDTO reportInfoMongoDTO = new ReportInfoMongoDTO();
            // TODO 转换
            BeanUtils.copyProperties(report, reportInfoMongoDTO);
            // 查询报告当前链接
            ReportDocument reportDocument = reportsApi.getReportDocument(report.getReportDocumentId());
            reportInfoMongoDTO.setReportDocumentUrl(reportDocument.getUrl());
            reportInfoMongoDTO.setReportHandleStatus(0);
            reportInfoMongoDTO.setReportCancelStatus(0);
            reportInfoMongoDTO.setDataStartTime(report.getDataStartTime().toString());
            reportInfoMongoDTO.setDataEndTime(report.getDataEndTime().toString());
            reportInfoMongoDTO.setProcessingStartTime(report.getProcessingStartTime().toString());
            reportInfoMongoDTO.setProcessingEndTime(report.getProcessingEndTime().toString());
            reportInfoMongoDTO.setProcessingStatus(report.getProcessingStatus().getValue());

            List<?> cvsList = AmazonSpApiReportUtils.downloadAndParse(reportDocument.getUrl(), recordTypeEnum.getCvsClass());
            // TODO 转换
            // 填充报告相关信息
            List<? extends ReportSuperMongoDTO> mongoDTOSList = handleData(cvsList, report, recordTypeEnum);

            //判断是否添加库存主表
            this.checkAndSaveMainInventory(report, reportScheduleEntity);

            // 报告保存
            mongoService.saveMongoData(reportInfoMongoDTO, MongoTableNameContant.THIRD_SYSTEM_AMAZON_REPORT);

            // 填充报告来源信息
            mongoService.saveMongoDataMult(mongoDTOSList, recordTypeEnum.getMongoTableName());

            // TODO 扩展
            if (AmazonReportRecordTypeEnum.GET_MERCHANT_LISTINGS_DATA.getRecordType().equalsIgnoreCase(report.getReportType())) {
                reportHandleService.pullBusinessHandler(reportScheduleEntity.getShopId(), report.getReportId(), mongoDTOSList);
            }
        }

        //如果设置的是客户端确认模式(Session.CLIENT_ACKNOWLEDGE)，调用acknowledge()删除sqs消息。
//        message.acknowledge();
    }


    /**
     * 保存主表
     */
    private void checkAndSaveMainInventory(Report report, ReportScheduleEntity reportScheduleEntity) {
        if (!AmazonReportRecordTypeEnum.getInventoryReportList().contains(report.getReportType())) {
            // 不属于库存报告
            return;
        }

        // TODO 使用redisson锁
        // 根据数据开始时间和结束时间,校验是否已存在？
        ReportInventoryCombineMongoDTO combineInventoryDTO = new ReportInventoryCombineMongoDTO(report.getDataStartTime().toString(), report.getDataEndTime().toString(), report.getMarketplaceIds());
        List<ReportInventoryCombineMongoDTO> mongoData = mongoService.findMongoData(combineInventoryDTO, 0, 0, MongoTableNameContant.REPORT_AMAZON_COMBINE_INVENTORY, ReportInventoryCombineMongoDTO.class);
        if (CollectionUtils.isEmpty(mongoData)) {
            // 新增
            ReportInventoryCombineMongoDTO newCombineInventoryDTO = new ReportInventoryCombineMongoDTO();
            // TODO 转换
            BeanUtils.copyProperties(report, newCombineInventoryDTO);
            newCombineInventoryDTO.setDataStartTime(report.getDataStartTime().toString());
            newCombineInventoryDTO.setDataEndTime(report.getDataEndTime().toString());
            newCombineInventoryDTO.setMarketplaceIds(report.getMarketplaceIds());
            newCombineInventoryDTO.setShopId(reportScheduleEntity.getShopId());
            mongoService.saveMongoData(newCombineInventoryDTO, MongoTableNameContant.REPORT_AMAZON_COMBINE_INVENTORY);
        } else {
            // 修改
            ReportInventoryCombineMongoDTO oldCombineInventoryDTO = mongoData.stream().findFirst().orElseThrow(() -> new ServiceException("ReportAmazonCombineInventory 不存在"));
            if (AmazonReportRecordTypeEnum.GET_FBA_MYI_ALL_INVENTORY_DATA.getRecordType().equalsIgnoreCase(report.getReportType())) {
                String myiAllInventoryReportId = oldCombineInventoryDTO.getMyiAllInventoryReportId();
                if (StringUtils.isNotBlank(myiAllInventoryReportId)) {
                    throw new ServiceException("ReportAmazonCombineInventory 更新异常：myiAllInventoryReportId已存在");
                }
                oldCombineInventoryDTO.setMyiAllInventoryReportId(report.getReportId());
            }
            if (AmazonReportRecordTypeEnum.GET_FBA_INVENTORY_PLANNING_DATA.getRecordType().equalsIgnoreCase(report.getReportType())) {
                String inventoryPlanningReportId = oldCombineInventoryDTO.getInventoryPlanningReportId();
                if (StringUtils.isNotBlank(inventoryPlanningReportId)) {
                    throw new ServiceException("ReportAmazonCombineInventory 更新异常：inventoryPlanningReportId已存在");
                }
                oldCombineInventoryDTO.setInventoryPlanningReportId(report.getReportId());
            }
            if (AmazonReportRecordTypeEnum.GET_RESERVED_INVENTORY_DATA.getRecordType().equalsIgnoreCase(report.getReportType())) {
                String reservedReportId = oldCombineInventoryDTO.getReservedReportId();
                if (StringUtils.isNotBlank(reservedReportId)) {
                    throw new ServiceException("ReportAmazonCombineInventory 更新异常：reservedReportId已存在");
                }
                oldCombineInventoryDTO.setReservedReportId(report.getReportId());
            }
            // 检查是否存在所有报告IDS
            if (StringUtils.isNotBlank(oldCombineInventoryDTO.getMyiAllInventoryReportId()) &&
                    StringUtils.isNotBlank(oldCombineInventoryDTO.getInventoryPlanningReportId()) &&
                    StringUtils.isNotBlank(oldCombineInventoryDTO.getReservedReportId())
            ) {
                oldCombineInventoryDTO.setCombineStatus(1);
            }
            // 修改数据
            MapUtil mapUtil = JSONUtil.toBean(JSONUtil.toJsonStr(oldCombineInventoryDTO), MapUtil.class);
            mongoService.updateMongoData(combineInventoryDTO, mapUtil, MongoTableNameContant.REPORT_AMAZON_COMBINE_INVENTORY, ReportInventoryCombineMongoDTO.class);
        }

    }

    public List<? extends ReportSuperMongoDTO> handleData(List<?> cvsList, Report report, AmazonReportRecordTypeEnum recordTypeEnum) {
        return cvsList.stream().map(o -> {
            try {
                ReportSuperMongoDTO mongoDTO = (ReportSuperMongoDTO) (recordTypeEnum.getMongoDTOClass().newInstance());
                BeanUtils.copyProperties(o, mongoDTO);
                mongoDTO.setDataStartTime(report.getDataStartTime().toString());
                mongoDTO.setDataEndTime(report.getDataEndTime().toString());
                mongoDTO.setMarketplaceIds(report.getMarketplaceIds());
                mongoDTO.setReportId(report.getReportId());
                mongoDTO.setReportScheduleId(report.getReportScheduleId());
                return mongoDTO;
            } catch (Exception e) {
                throw new ServiceException("csv转换mongoDTO失败, error=" + e.getMessage());
            }
        }).collect(Collectors.toList());
    }

}