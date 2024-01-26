package com.erp.server.wms.service;
import com.erp.model.wms.entity.PackageForecastDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.PackageForecastDetailDTO;

/**
 * <p>
 * 组包预报详情 服务类
 * </p>
 *
 * @author Lambda
 * @since 2024-01-26
 */
public interface PackageForecastDetailService extends SuperService<PackageForecastDetailEntity> {

    /**
    * 新增
    * @author Lambda
    * @date: 2024-01-26
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(PackageForecastDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author Lambda
    * @date: 2024-01-26
    * @param dto
    * @return
    */
    Boolean update(PackageForecastDetailDTO.UpdateDTO dto);


}
