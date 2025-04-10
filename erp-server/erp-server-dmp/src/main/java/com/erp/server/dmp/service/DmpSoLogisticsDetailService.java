package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpSoLogisticsDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpSoLogisticsDetailDTO;

/**
 * <p>
 * 中台物流单明细表 服务类
 * </p>
 *
 * @author shukai
 * @since 2025-04-07
 */
public interface DmpSoLogisticsDetailService extends SuperService<DmpSoLogisticsDetailEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2025-04-07
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpSoLogisticsDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2025-04-07
    * @param dto
    * @return
    */
    Boolean update(DmpSoLogisticsDetailDTO.UpdateDTO dto);


}
