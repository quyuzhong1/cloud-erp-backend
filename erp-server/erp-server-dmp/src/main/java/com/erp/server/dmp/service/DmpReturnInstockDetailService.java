package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpReturnInstockDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpReturnInstockDetailDTO;

/**
 * <p>
 * 中台退货入库明细表 服务类
 * </p>
 *
 * @author shukai
 * @since 2025-04-09
 */
public interface DmpReturnInstockDetailService extends SuperService<DmpReturnInstockDetailEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2025-04-09
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpReturnInstockDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2025-04-09
    * @param dto
    * @return
    */
    Boolean update(DmpReturnInstockDetailDTO.UpdateDTO dto);


}
