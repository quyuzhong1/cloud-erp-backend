package com.erp.server.tms.service;
import com.erp.model.tms.entity.LogisticsServicePlatformEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.LogisticsServicePlatformDTO;

/**
 * <p>
 * 物流平台服务表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2024-03-04
 */
public interface LogisticsServicePlatformService extends SuperService<LogisticsServicePlatformEntity> {

    /**
    * 新增
    * @author Lambda
    * @date: 2024-03-04
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(LogisticsServicePlatformDTO.AddDTO dto);

    /**
    * 修改
    * @author Lambda
    * @date: 2024-03-04
    * @param dto
    * @return
    */
    Boolean update(LogisticsServicePlatformDTO.UpdateDTO dto);


}
