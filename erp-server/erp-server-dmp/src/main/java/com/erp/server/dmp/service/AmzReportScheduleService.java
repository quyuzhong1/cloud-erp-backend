package com.erp.server.dmp.service;
import com.erp.model.dmp.dto.AmazonJobParamDTO;
import com.erp.model.dmp.dto.PlatformTaskDTO;
import com.erp.model.dmp.entity.AmzReportScheduleEntity;
import com.common.business.service.SuperService;
import com.erp.model.dmp.entity.CfgAmzReportTypeEntity;
import com.erp.model.oms.entity.ShopInfoEntity;

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
     * 店铺授权添加或启用所有计划任务
     *
     * @author Jim
     * @date: 2023-11-08
     */
    Boolean addOrUpdateReportSchedule(PlatformTaskDTO.DisabledDTO dto);

    /**
     * 店铺取消授权取消所有计划任务
     *
     * @author Jim
     * @date: 2023-11-08
     */
    Boolean cancelReportSchedule(String shopId);

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
    List<AmzReportScheduleEntity> listByParams(String subscribedStatus, String cancelStatus, List<String> subscribedTypeList, List<String> recordTypeList, List<String> shopIds, LocalDateTime minTime);

    /**
     * 更新时间
     *
     * @author Jim
     * @date: 2024-01-20
     */
    void updateNextTime(String mainId);

    /**
     * 根据任务参数查询执行的计划任务
     *
     * @author Jim
     * @date: 2024-01-25
     */
    List<AmzReportScheduleEntity> findActionList(List<ShopInfoEntity> shopInfoEntityList, List<CfgAmzReportTypeEntity> reportTypeConfigList, AmazonJobParamDTO.ReportJobDTO jobParamDTO, List<String> subscribedTypeList);
}
