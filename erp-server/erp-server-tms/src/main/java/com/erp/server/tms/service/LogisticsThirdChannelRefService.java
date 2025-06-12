package com.erp.server.tms.service;
import com.erp.model.tms.entity.LogisticsThirdChannelRefEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.LogisticsThirdChannelRefDTO;

import java.util.List;

/**
 * <p>
 * 物流-第三方渠道关系表 服务类
 * </p>
 *
 * @author zdy
 * @since 2025-05-29
 */
public interface LogisticsThirdChannelRefService extends SuperService<LogisticsThirdChannelRefEntity> {

    /**
    * 新增
    * @author zdy
    * @date: 2025-05-29
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(LogisticsThirdChannelRefDTO.AddDTO dto);

    /**
    * 修改
    * @author zdy
    * @date: 2025-05-29
    * @param dto
    * @return
    */
    Boolean update(LogisticsThirdChannelRefDTO.UpdateDTO dto);


    List<LogisticsThirdChannelRefEntity> listByChannelIds(List<String> channelIds);
}
