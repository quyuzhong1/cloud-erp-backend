package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpSoLogisticsEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpSoLogisticsDTO;

/**
 * <p>
 * 中台物流单主表 服务类
 * </p>
 *
 * @author shukai
 * @since 2025-04-07
 */
public interface DmpSoLogisticsService extends SuperService<DmpSoLogisticsEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2025-04-07
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpSoLogisticsDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2025-04-07
    * @param dto
    * @return
    */
    Boolean update(DmpSoLogisticsDTO.UpdateDTO dto);


}
