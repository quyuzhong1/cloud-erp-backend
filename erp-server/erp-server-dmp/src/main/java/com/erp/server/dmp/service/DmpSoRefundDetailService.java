package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpSoRefundDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpSoRefundDetailDTO;

/**
 * <p>
 * 中台销售退款单明细表 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-07-12
 */
public interface DmpSoRefundDetailService extends SuperService<DmpSoRefundDetailEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-07-12
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpSoRefundDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-07-12
    * @param dto
    * @return
    */
    Boolean update(DmpSoRefundDetailDTO.UpdateDTO dto);


}
