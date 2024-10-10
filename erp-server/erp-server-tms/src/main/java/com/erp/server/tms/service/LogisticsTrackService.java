package com.erp.server.tms.service;
import com.erp.model.tms.dto.LogisticsBillDetailQueryDTO;
import com.erp.model.tms.entity.LogisticsTrackEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.LogisticsTrackDTO;
import com.sdk.tms.track123.dto.PlatformTrackDTO;

import java.util.List;

/**
 * <p>
 * 物流轨迹表 服务类
 * </p>
 *
 * @author zdy
 * @since 2023-11-14
 */
public interface LogisticsTrackService extends SuperService<LogisticsTrackEntity> {

    /**
    * 新增
    * @author zdy
    * @date: 2023-11-14
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(LogisticsTrackDTO.AddDTO dto);

    /**
    * 修改
    * @author zdy
    * @date: 2023-11-14
    * @param dto
    * @return
    */
    Boolean update(LogisticsTrackDTO.UpdateDTO dto);

    /**
     * 根据运输单号查询数据
     * @author yl
     * @date 2023-11-16 15:50
     * @param trackNoList
     * @return 
     */
    List<LogisticsTrackEntity> listByTrackNoList(List<String> trackNoList);

    /**
     * 根据运输单查询数据
     *@parms trackNo
     *@return 
     *@author yl
     *@date 2023-11-16
     */
    LogisticsTrackDTO.ViewDTO listByTrackNo(String trackNo);

    /**
     * 根据跟踪单号进行物理删除
     * @param trackNo
     */
    void deleteByTrackNo(String trackNo);

    /**
     * 同步修改订单状态
     * @param logisticsTrackEntity
     */
    void checkTrackStatus(LogisticsTrackEntity logisticsTrackEntity);

    /**
     * 获取跟踪号最后一条记录
     * @param trackNo
     * @return
     */
    LogisticsTrackEntity getMaxByTrackTime(String trackNo);

    /**
     * 处理mongoDb同步数据
     * @param dto
     */
    void processTrackData(PlatformTrackDTO dto);

    /**
     * 更新3个月前物流单状态为系统完结
     */
    void updateBeforeThreeMonthTrackNo(LogisticsBillDetailQueryDTO query);
}
