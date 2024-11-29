package com.erp.server.tms.service;
import com.erp.model.tms.entity.LogisticsLargeEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.LogisticsLargeDTO;

/**
 * <p>
 * 物流大表 服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2024-11-29
 */
public interface LogisticsLargeService extends SuperService<LogisticsLargeEntity> {

    /**
    * 新增
    * @author Luo_WG
    * @date: 2024-11-29
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(LogisticsLargeDTO.AddDTO dto);

    /**
    * 修改
    * @author Luo_WG
    * @date: 2024-11-29
    * @param dto
    * @return
    */
    Boolean update(LogisticsLargeDTO.UpdateDTO dto);


}
