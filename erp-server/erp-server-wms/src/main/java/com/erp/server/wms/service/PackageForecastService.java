package com.erp.server.wms.service;
import com.common.business.validator.ValidList;
import com.erp.model.wms.entity.PackageForecastEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.PackageForecastDTO;

/**
 * <p>
 * 组包预报表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2024-01-26
 */
public interface PackageForecastService extends SuperService<PackageForecastEntity> {

    /**
    * 新增
    * @author Lambda
    * @date: 2024-01-26
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(PackageForecastDTO.AddDTO dto);

    /**
    * 修改
    * @author Lambda
    * @date: 2024-01-26
    * @param dto
    * @return
    */
    Boolean update(PackageForecastDTO.UpdateDTO dto);


}
