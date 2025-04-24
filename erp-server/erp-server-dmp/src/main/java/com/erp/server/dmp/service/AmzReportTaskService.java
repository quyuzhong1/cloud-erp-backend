package com.erp.server.dmp.service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import com.common.business.dto.MongoSuperDTO;
import com.common.business.service.SuperService;
import com.erp.model.dmp.entity.AmzReportScheduleEntity;
import com.erp.model.dmp.entity.AmzReportTaskEntity;
import com.erp.model.dmp.entity.CfgAmzReportTypeEntity;
import com.erp.model.dmp.enums.AmzReportTaskStatusEnum;
import com.erp.model.oms.entity.ShopInfoEntity;

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
    void createTask(String redissonKey, String groupKey, AmzReportScheduleEntity reportSchedule, OffsetDateTime currentDateTime, Map<String, CfgAmzReportTypeEntity> reportTypeMap);

    /**
     * 查询上一次的任务记录
     *
     * @author Jim
     * @date: 2024-01-19
     */
    AmzReportTaskEntity findLastTask(AmzReportScheduleEntity reportSchedule);

    /**
     * 查询任务是否完成或停止
     *
     * @author Jim
     * @date: 2024-01-20
     */
    boolean checkFinishOrStop(String id);

    /**
     * 消费创建报告查询
     *
     * @author Jim
     * @date: 2024-01-20
     */
    void consumerReportCreate(String reportRedissonKey, AmzReportTaskEntity entity);

    /**
     * 消费报告查询
     *
     * @author Jim
     * @date: 2024-01-20
     */
    void consumerReportQuery(String reportRedissonKey, AmzReportTaskEntity entity);


    /**
     * 消费报告下载
     *
     * @author Jim
     * @date: 2024-01-20
     */
    void consumerReportDownload(String reportRedissonKey, AmzReportTaskEntity entity);

    /**
     * 消费报告解析
     *
     * @author Jim
     * @date: 2024-01-20
     */
    void consumerReportParse(String reportRedissonKey, AmzReportTaskEntity entity);

    /**
     * 消费报告直接查询
     *
     * @author Jim
     * @date: 2024-01-25
     */
    void consumerReportDirectQuery(String reportRedissonKey, AmzReportTaskEntity entity);

    /**
     * 查询未完成或终止的任务
     *
     * @author Jim
     * @date: 2024-01-20
     */
    List<AmzReportTaskEntity> findNotFinishOrStop(String shopId, String reportType, String id);

    /**
     * 更新异常信息和重试次数
     *
     * @author Jim
     * @date: 2024-01-20
     */
    void updateErrorMsgAndCount(AmzReportTaskEntity entity, String errorMsg, Integer createdRetryCount, Integer queryRetryCount, Integer downloadRetryCount, Integer parseRetryCount);


    /**
     * 更新状态和时间
     *
     * @author Jim
     * @date: 2024-01-20
     */
    AmzReportTaskEntity updateStatus(String reportId, AmzReportTaskEntity entity, AmzReportTaskStatusEnum statusEnum, LocalDateTime reportCreatedTime, LocalDateTime reportQueryTime, LocalDateTime reportDownloadTime, LocalDateTime reportParseTime, LocalDateTime completedTime, Boolean addCreatedRetryCount);


    /**
     * 处理检查报告任务
     * (3-获取报告阶段)
     *
     * @author Jim
     * @date: 2024-01-25
     */
    void handlerCheckReport(String groupId, List<ShopInfoEntity> shopList, Integer size, OffsetDateTime currentDateTime, List<AmzReportScheduleEntity> scheduleEntityList, Map<String, List<CfgAmzReportTypeEntity>> reportTypeConfigMap, Map<String, CfgAmzReportTypeEntity> reportTypeMap);


    /**
     * 处理报告待获取任务记录
     * (3-获取报告阶段)
     *
     * @author Jim
     * @date: 2024-01-25
     */
    void checkTask(String reportRedissonKey, String groupId, AmzReportScheduleEntity reportSchedule, OffsetDateTime currentDateTime, Map<String, CfgAmzReportTypeEntity> reportTypeMap);

    /**
     * 检查和移除缓存
     *
     * @author Jim
     * @date: 2024-01-29
     */
    void checkAndDelHistory(AmzReportTaskEntity entity);

    /**
     * 亚马逊异常停止任务
     *
     * @author Jim
     * @date: 2024-01-31
     */
    void stopByErrorMsg(AmzReportTaskEntity entity, String msg);

    /**
     * 亚马逊异常检查和停止任务
     *
     * @author Jim
     * @date: 2024-01-31
     */
    boolean checkStopByUnAuthorized(Exception exception, AmzReportTaskEntity entity);

    /**
     * 异常发送预警
     *
     * @author Jim
     * @date: 2024-01-31
     */
    void sendReportWarnMsg(AmzReportTaskEntity entity, String errorMsg);

    /**
     * 检查停止预警
     *
     * @author Jim
     * @date: 2024-01-31
     */
    boolean checkStopAndUpdateTask(AmzReportTaskEntity entity);

    /**
     * 报告重试任务
     *
     * @author Jim
     * @date: 2024-01-31
     */
    void retryTask(AmzReportTaskEntity taskEntity);


    /**
     * 检查重试次数是否停止
     *
     * @author Jim
     * @date: 2024-01-31
     */
    boolean stopRetryCount(Integer retryCount);


    /**
     * 记录处理行数和保存mongo公用事务
     *
     * @author Jim
     * @date: 2024-05-07
     */
    <T extends MongoSuperDTO> AmzReportTaskEntity saveMongoAndUpdateRowIndex(String mongoTableName, List<T> mongoList, AmzReportTaskEntity currentEntity);
}
