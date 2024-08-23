package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpSoPrestockDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpSoPrestockDetailDTO;

/**
 * <p>
 * 销售预入库明细表 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-08-09
 */
public interface DmpSoPrestockDetailService extends SuperService<DmpSoPrestockDetailEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-08-09
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpSoPrestockDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-08-09
    * @param dto
    * @return
    */
    Boolean update(DmpSoPrestockDetailDTO.UpdateDTO dto);


}
