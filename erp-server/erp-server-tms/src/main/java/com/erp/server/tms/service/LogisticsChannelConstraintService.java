package com.erp.server.tms.service;

import com.common.business.dto.base.BatchResultDTO;
import com.common.business.service.SuperService;
import com.erp.model.tms.dto.LogisticsChannelConstraintDTO;
import com.erp.model.tms.entity.LogisticsChannelConstraintEntity;

import java.util.List;

/**
 * <p>
 * 物流渠道规则约束 服务类
 * </p>
 *
 * @author lrp
 * @since 2024-02-29
 */
public interface LogisticsChannelConstraintService extends SuperService<LogisticsChannelConstraintEntity> {

    /**
    * 新增
    * @author lrp
    * @date: 2024-02-29
    * @param dto
    * @return
    */
    List<BatchResultDTO> addAndUpdate(LogisticsChannelConstraintDTO.AddOrUpdateDTO dto);


    List<LogisticsChannelConstraintDTO.ListDTO> getList(String channelId);
}
