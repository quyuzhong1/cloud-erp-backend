package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpSoOriginalDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpSoOriginalDetailDTO;

/**
 * <p>
 * 中台原始销售订单明细表 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-11-25
 */
public interface DmpSoOriginalDetailService extends SuperService<DmpSoOriginalDetailEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-11-25
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpSoOriginalDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-11-25
    * @param dto
    * @return
    */
    Boolean update(DmpSoOriginalDetailDTO.UpdateDTO dto);


}
