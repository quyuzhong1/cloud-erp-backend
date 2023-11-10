package com.erp.server.dmp.service;

import com.erp.model.dmp.dto.DmpPullShipmentDTO;
import com.erp.model.dmp.entity.ReportScheduleEntity;
import com.erp.sdk.oms.amz.spapi.dto.ReportFbaInventoryPlanningMongoDTO;
import com.erp.sdk.oms.amz.spapi.dto.ReportFbaMyiAllInventoryMongoDTO;
import com.erp.sdk.oms.amz.spapi.dto.ReportInventoryCombineMongoDTO;
import com.erp.sdk.oms.amz.spapi.dto.ReportReservedMongoDTO;

import java.time.LocalDateTime;
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
public interface ReportHandleService {

    /**
     * 组合报告
     */
    void combineInventory(ReportInventoryCombineMongoDTO combineInventoryDTO, List<ReportFbaMyiAllInventoryMongoDTO> fbaMyiAllInventoryMongoDTOList, List<ReportReservedMongoDTO> reportReservedMongoDTOList, List<ReportFbaInventoryPlanningMongoDTO> planningMongoDTOList);

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
    void createReportSchedule(ReportScheduleEntity reportSchedule, OffsetDateTime roundedDateTime) throws Exception;

    /**
     * 请求创建报告
     *
     * @Author Jim
     * @since 2023-11-10
     **/
    void createReport(ReportScheduleEntity reportSchedule, OffsetDateTime currentDateTime);
}
