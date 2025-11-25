package com.erp.server.plm.service;
import com.erp.model.plm.entity.CfgMoldReturnAlertDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.plm.dto.CfgMoldReturnAlertDetailDTO;

/**
 * <p>
 * 模具返回策略明细 服务类
 * </p>
 *
 * @author jack
 * @since 2025-10-15
 */
public interface CfgMoldReturnAlertDetailService extends SuperService<CfgMoldReturnAlertDetailEntity> {

    /**
    * 新增
    * @author jack
    * @date: 2025-10-15
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(CfgMoldReturnAlertDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author jack
    * @date: 2025-10-15
    * @param dto
    * @return
    */
    Boolean update(CfgMoldReturnAlertDetailDTO.UpdateDTO dto);


}
