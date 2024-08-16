package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpThirdRegionEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpThirdRegionDTO;

/**
 * <p>
 * 第三方区域 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-08-07
 */
public interface DmpThirdRegionService extends SuperService<DmpThirdRegionEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-08-07
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpThirdRegionDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-08-07
    * @param dto
    * @return
    */
    Boolean update(DmpThirdRegionDTO.UpdateDTO dto);


}
