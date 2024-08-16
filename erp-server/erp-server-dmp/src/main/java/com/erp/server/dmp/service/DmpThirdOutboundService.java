package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpThirdOutboundEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpThirdOutboundDTO;

/**
 * <p>
 * 第三方仓出库 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-08-08
 */
public interface DmpThirdOutboundService extends SuperService<DmpThirdOutboundEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-08-08
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpThirdOutboundDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-08-08
    * @param dto
    * @return
    */
    Boolean update(DmpThirdOutboundDTO.UpdateDTO dto);


}
