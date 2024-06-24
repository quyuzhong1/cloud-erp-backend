package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpMongoHandleTaskEntity;
import com.common.business.service.SuperService;
import com.erp.sdk.oms.amz.spapi.dto.ReportFulfilledShipmentsMongoDTO;
import com.erp.server.dmp.enums.DmpMongoHandleTypeEnum;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 处理mongo业务数据任务 服务类
 * </p>
 *
 * @author Jim
 * @since 2024-05-07
 */
public interface DmpMongoHandleTaskService extends SuperService<DmpMongoHandleTaskEntity> {


    /**
     * 查询mongo数据
     * @param lastId 当前已处理的最大
     * @param handleCount 需要处理的记录数量
     * @param mongoTableName Mongo名
     * @param mongoDTOClass 指定的mongo对象Class
     * @param <T> mongo对象
     * @return mongo对象列表
     */
    <T> List<T> findMongoData(String lastId, Integer handleCount, String mongoTableName, Class<T> mongoDTOClass, Boolean queryIsAddOrUpdate);

    /**
     * 更新当前最大已处理ID和记录下次执行时间
     * @param mongoHandleTaskEntity 原任务对象
     * @param maxLastId 当前最大已处理ID
     */
    void updateMaxLastIdAndNextTime(DmpMongoHandleTaskEntity mongoHandleTaskEntity, String maxLastId);

    /**
     * 清理历史记录
     * @param handleTypeEnum 处理的类型
     * @param historyDateTime 指定时间之前的下载记录
     * @param size 每次清理数量
     */
    Long clearHistory(DmpMongoHandleTypeEnum handleTypeEnum, LocalDateTime historyDateTime, Integer size);
}
