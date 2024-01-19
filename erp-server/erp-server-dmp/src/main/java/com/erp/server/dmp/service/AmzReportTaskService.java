package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.AmzReportScheduleEntity;
import com.erp.model.dmp.entity.AmzReportTaskEntity;
import com.common.business.service.SuperService;
import com.erp.model.dmp.entity.CfgAmzReportTypeEntity;
import com.erp.model.oms.entity.ShopInfoEntity;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 亚马逊报告请求记录 服务类
 * </p>
 *
 * @author Jim
 * @since 2024-01-18
 */
public interface AmzReportTaskService extends SuperService<AmzReportTaskEntity> {


    /**
     * 处理创建报告任务
     * (1-任务发起阶段)
     *
     * @author Jim
     * @date: 2024-01-19
     */
    void handlerCreateReportTask(String group, List<ShopInfoEntity> shopList, Integer size, OffsetDateTime currentDateTime, List<AmzReportScheduleEntity> scheduleEntityList, Map<String, List<CfgAmzReportTypeEntity>> reportTypeConfigMap, Map<String, CfgAmzReportTypeEntity> reportTypeMap);

    /**
     * 处理报告待请求任务记录
     * (1-任务发起阶段)
     *
     * @author Jim
     * @date: 2024-01-19
     */
    void createTask(String groupKey, AmzReportScheduleEntity reportSchedule, OffsetDateTime currentDateTime, Map<String, CfgAmzReportTypeEntity> reportTypeMap);

    /**
     * 查询上一次的任务记录
     *
     * @author Jim
     * @date: 2024-01-19
     */
    AmzReportTaskEntity findLastTask(AmzReportScheduleEntity reportSchedule);
}
