package com.erp.server.dmp.service;

import cn.hutool.json.JSONObject;
import com.erp.model.dmp.dto.DmpPullShipmentDTO;
import com.erp.model.dmp.entity.AmzReportInfoEntity;
import com.erp.model.dmp.entity.AmzReportScheduleEntity;
import com.erp.model.dmp.entity.AmzReportTaskEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.sdk.oms.amz.spapi.api.ReportsApi;
import com.erp.sdk.oms.amz.spapi.dto.*;
import com.erp.sdk.oms.amz.spapi.enums.AmazonReportRecordTypeEnum;
import com.erp.sdk.oms.amz.spapi.model.reports.Report;
import com.erp.sdk.oms.amz.spapi.model.reports.ReportDocument;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

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
     * 请求创建亚马逊报告
     *
     * @Author Jim
     * @since 2023-11-10
     **/
    String createAmzReport(AmzReportTaskEntity taskEntity);

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
     *
     * 获取报告文档
     *
     * @Author Jim
     * @since 2024-01-20
     **/
    ReportDocument queryAmzReportDocument(AmzReportInfoEntity reportInfoEntity, AmzReportTaskEntity entity);

    /**
     * 定时任务处理
     * 检查店铺，取消报告，取消任务
     *
     * @Author Jim
     * @since 2023-12-19
     **/
    void checkAndUpdateShop(List<ShopInfoEntity> shopList) throws Exception ;
}
