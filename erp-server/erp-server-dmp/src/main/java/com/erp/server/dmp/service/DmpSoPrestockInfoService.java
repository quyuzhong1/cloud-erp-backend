package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpSoPrestockInfoEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpSoPrestockInfoDTO;

/**
 * <p>
 * 销售预入库主表 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-08-09
 */
public interface DmpSoPrestockInfoService extends SuperService<DmpSoPrestockInfoEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-08-09
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpSoPrestockInfoDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-08-09
    * @param dto
    * @return
    */
    Boolean update(DmpSoPrestockInfoDTO.UpdateDTO dto);


}
