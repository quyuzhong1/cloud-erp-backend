package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpSoReturnDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpSoReturnDetailDTO;

/**
 * <p>
 * 中台销售退货订单明细表 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-06-30
 */
public interface DmpSoReturnDetailService extends SuperService<DmpSoReturnDetailEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-06-30
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpSoReturnDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-06-30
    * @param dto
    * @return
    */
    Boolean update(DmpSoReturnDetailDTO.UpdateDTO dto);


}
