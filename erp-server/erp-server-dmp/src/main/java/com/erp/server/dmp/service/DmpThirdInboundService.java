package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpThirdInboundEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpThirdInboundDTO;

/**
 * <p>
 * 第三方仓库存 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-08-08
 */
public interface DmpThirdInboundService extends SuperService<DmpThirdInboundEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-08-08
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpThirdInboundDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-08-08
    * @param dto
    * @return
    */
    Boolean update(DmpThirdInboundDTO.UpdateDTO dto);


}
