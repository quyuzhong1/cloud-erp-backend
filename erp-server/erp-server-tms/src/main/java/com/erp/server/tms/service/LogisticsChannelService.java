package com.erp.server.tms.service;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.LogisticsChannelDTO;

import java.util.List;

/**
 * <p>
 * 物流渠道表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-11-02
 */
public interface LogisticsChannelService extends SuperService<LogisticsChannelEntity> {

    /**
    * 新增
    * @author Lambda
    * @date: 2023-11-02
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(LogisticsChannelDTO.AddDTO dto);

    /**
    * 修改
    * @author Lambda
    * @date: 2023-11-02
    * @param dto
    * @return
    */
    Boolean update(LogisticsChannelDTO.UpdateDTO dto);

    /**
     * @description: 物流渠道列表
     * @author Will
     * @date: 2023/11/10 10:06
     * @return List<ListSelectDTO>
     */
    List<LogisticsChannelDTO.ListSelectDTO> listLogisticsChannel();

    /**
     * 根据来源id 获取渠道列表
     * @param sourceId
     * @return
     */
    List<LogisticsChannelDTO.BaseDTO> listBaseBySourceId(String sourceId);
}
