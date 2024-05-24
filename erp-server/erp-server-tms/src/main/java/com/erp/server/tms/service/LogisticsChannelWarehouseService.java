package com.erp.server.tms.service;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.SuperService;
import com.erp.model.tms.dto.LogisticsChannelWarehouseDTO;
import com.erp.model.tms.entity.LogisticsChannelWarehouseEntity;

/**
 * <p>
 * 渠道仓库设置表 服务类
 * </p>
 *
 * @author will
 * @since 2024-05-23
 */
public interface LogisticsChannelWarehouseService extends SuperService<LogisticsChannelWarehouseEntity> {

    /**
    * 新增
    * @author will
    * @date: 2024-05-23
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO batchUpdate(String channelId,LogisticsChannelWarehouseDTO.BatchUpdateDTO dto);

    /**
     * @description: 根据渠道id查询
     * @author Will
     * @date: 2024/5/23 10:26
     * @param id
     * @return ViewDTO
     */
    LogisticsChannelWarehouseDTO.ViewDTO getByChannelId(String id);
}
