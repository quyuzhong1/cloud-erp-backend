package com.erp.server.tms.service;
import com.erp.model.tms.entity.LogisticsTrackEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.LogisticsTrackDTO;

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


}
