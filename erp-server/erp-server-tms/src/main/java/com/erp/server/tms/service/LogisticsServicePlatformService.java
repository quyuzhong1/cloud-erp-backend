package com.erp.server.tms.service;
import com.erp.model.tms.entity.LogisticsServicePlatformEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.LogisticsServicePlatformDTO;

import java.util.List;

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


    /**
     * 根据平台查询
     * @description
     * @param logisticsPlatform
     * @return
     * @date 2024-03-04 16:52
     * @author Lambda
     */
    List<LogisticsServicePlatformEntity> listByPlatform(String logisticsPlatform);

    /**
     * 根据平台获取服务名
     * @param logisticsPlatform
     * @return
     */
    List<LogisticsServicePlatformDTO.ServiceNameDTO> listServiceNameByLogisticsPlatform(String logisticsPlatform);
}
