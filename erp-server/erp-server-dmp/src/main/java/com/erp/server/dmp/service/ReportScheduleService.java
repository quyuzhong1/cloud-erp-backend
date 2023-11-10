package com.erp.server.dmp.service;

import com.erp.model.dmp.dto.DmpSyncReportScheduleDTO;
import com.erp.model.dmp.entity.ReportScheduleEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.ReportScheduleDTO;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;

import java.util.List;

/**
 * <p>
 * 亚马逊报告计划表 服务类
 * </p>
 *
 * @author Jim
 * @since 2023-11-08
 */
public interface ReportScheduleService extends SuperService<ReportScheduleEntity> {

    /**
     * reportScheduleId是否存在
     *
     * @author Jim
     * @date: 2023-11-08
     */
    boolean existByReportScheduleId(String reportScheduleId);

    /**
     * 通过reportScheduleId查询实体
     *
     * @author Jim
     * @date: 2023-11-08
     */
    ReportScheduleEntity getByReportScheduleId(String reportScheduleId);

    /**
     * 店铺授权添加所有计划任务
     *
     * @author Jim
     * @date: 2023-11-08
     */
    Boolean addReportSchedule(DmpSyncReportScheduleDTO dto);

    /**
     * 店铺取消授权取消所有计划任务
     *
     * @author Jim
     * @date: 2023-11-08
     */
    Boolean cancelReportSchedule(DmpSyncReportScheduleDTO dto);

    /**
     * 检查和请求所有计划任务
     *
     * @author Jim
     * @date: 2023-11-10
     */
    List<ReportScheduleEntity> findList(String subscribedStatus, String cancelStatus, Integer size);
}
