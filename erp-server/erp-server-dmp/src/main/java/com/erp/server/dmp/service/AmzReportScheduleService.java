package com.erp.server.dmp.service;
import com.erp.model.dmp.dto.DmpSyncReportScheduleDTO;
import com.erp.model.dmp.entity.AmzReportScheduleEntity;
import com.common.business.service.SuperService;
import com.erp.model.dmp.enums.ReportScheduleCancelStatusEnum;
import com.erp.model.dmp.enums.ReportScheduleSubscribedStatusEnum;
import com.erp.model.dmp.enums.ReportScheduleSubscribedTypeEnum;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 亚马逊报告计划表 服务类
 * </p>
 *
 * @author Jim
 * @since 2024-01-18
 */
public interface AmzReportScheduleService extends SuperService<AmzReportScheduleEntity> {

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
    AmzReportScheduleEntity getByReportScheduleId(String reportScheduleId);

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
    List<AmzReportScheduleEntity> findList(String subscribedStatus, String cancelStatus, Integer size);


    /**
     * 检查和请求所有计划任务
     *
     * @author Jim
     * @date: 2024-01-19
     */
    List<AmzReportScheduleEntity> listByParams(String subscribedStatus, String cancelStatus, String subscribedType, List<String> recordTypeList, List<String> shopIds, LocalDateTime minTime);

    /**
     * 更新时间
     *
     * @author Jim
     * @date: 2024-01-20
     */
    void updateNextTime(String mainId);
}
