package com.erp.server.dmp.service;

import cn.hutool.json.JSONObject;
import com.erp.model.dmp.dto.AmazonCreateReportResultDTO;
import com.erp.model.dmp.dto.DmpPullShipmentDTO;
import com.erp.model.dmp.entity.DmpAmzReportInfoEntity;
import com.erp.model.dmp.entity.AmzReportScheduleEntity;
import com.erp.model.dmp.entity.AmzReportTaskEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.sdk.oms.amz.spapi.api.ReportsApi;
import com.erp.sdk.oms.amz.spapi.client.ApiException;
import com.erp.sdk.oms.amz.spapi.model.reports.Report;
import com.erp.sdk.oms.amz.spapi.model.reports.ReportDocument;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * <p>
 * 亚马逊报告处理 服务类
 * </p>
 *
 * @author Jim
 * @since 2023-11-08
 */
public interface AmzReportHandleService {

    /**
     * 拉取货件
     *
     * @Author Jim
     * @since 2023-10-10
     **/
    Boolean pullShipment(DmpPullShipmentDTO dto);

    /**
     * 请求报告计划
     *
     * @Author Jim
     * @since 2023-11-10
     **/
    void createReportSchedule(AmzReportScheduleEntity reportSchedule, OffsetDateTime roundedDateTime) throws Exception;

    /**
     * 亚马逊报告通知处理
     *
     * @Author Jim
     * @since 2023-11-22
     **/
    void handlerNotifications(JSONObject textMessage) throws Exception;


    /**
     * 查询和创建报告(检查是否有处理中的报告）
     * 预估等待时间(秒)：0=不等待
     *
     * @Author Jim
     * @since 2024-06-18
     **/
    AmazonCreateReportResultDTO checkAndCreateAmzReport(AmzReportTaskEntity taskEntity, String reportGroup);

    /**
     * 查询亚马逊报告
     *
     * @Author Jim
     * @since 2024-01-20
     **/
    Report queryAmzReportInfo(AmzReportTaskEntity taskEntity);

    /**
     * 直接查询亚马逊最新报告
     *
     * @Author Jim
     * @since 2024-01-25
     **/
    Report directQueryAmzReportInfo(AmzReportTaskEntity entity);

    /**
     * 获取报告文档
     *
     * @Author Jim
     * @since 2024-01-20
     **/
    ReportDocument queryAmzReportDocument(String shopId, String reportDocumentId, String taskId, String taskStatus);

    /**
     * 定时任务处理
     * 检查店铺，取消报告，取消任务
     *
     * @Author Jim
     * @since 2023-12-19
     **/
    void checkAndUpdateShop(List<ShopInfoEntity> shopList) throws Exception;

    /**
     * 查询是否有处理中的报告(响应预估处理结束时间:0=无处理中报告)
     *
     * @param reportsApi  报告API
     * @param reportTypes 同组报告类型
     * @param taskId  当前任务ID
     * @param groupId 当前任务分组ID
     * @return 预估等待时间:0=无需等待
     * @Author Jim
     * @since 2024-06-18
     */
    Long queryProcessReportWaitTime(ReportsApi reportsApi, List<String> reportTypes, String taskId, String groupId);
}
