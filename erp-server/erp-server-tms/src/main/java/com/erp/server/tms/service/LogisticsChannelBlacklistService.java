package com.erp.server.tms.service;
import com.erp.model.tms.entity.LogisticsChannelBlacklistEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.LogisticsChannelBlacklistDTO;

/**
 * <p>
 * 渠道黑名单表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-11-02
 */
public interface LogisticsChannelBlacklistService extends SuperService<LogisticsChannelBlacklistEntity> {

    /**
    * 新增
    * @author Lambda
    * @date: 2023-11-02
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(LogisticsChannelBlacklistDTO.AddDTO dto);

    /**
    * 修改
    * @author Lambda
    * @date: 2023-11-02
    * @param dto
    * @return
    */
    Boolean update(LogisticsChannelBlacklistDTO.UpdateDTO dto);


}
