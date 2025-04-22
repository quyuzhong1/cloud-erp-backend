package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpReturnInstockEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpReturnInstockDTO;

/**
 * <p>
 * 中台退货入库单主表 服务类
 * </p>
 *
 * @author shukai
 * @since 2025-04-09
 */
public interface DmpReturnInstockService extends SuperService<DmpReturnInstockEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2025-04-09
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpReturnInstockDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2025-04-09
    * @param dto
    * @return
    */
    Boolean update(DmpReturnInstockDTO.UpdateDTO dto);


}
