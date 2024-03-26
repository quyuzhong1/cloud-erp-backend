package com.erp.server.tms.service;
import com.erp.model.tms.entity.TmsFirstMileReconciliationDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.TmsFirstMileReconciliationDetailDTO;

/**
 * <p>
 * 头程对账单明细 服务类
 * </p>
 *
 * @author Jim
 * @since 2024-03-25
 */
public interface TmsFirstMileReconciliationDetailService extends SuperService<TmsFirstMileReconciliationDetailEntity> {

    /**
    * 新增
    * @author Jim
    * @date: 2024-03-25
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(TmsFirstMileReconciliationDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author Jim
    * @date: 2024-03-25
    * @param dto
    * @return
    */
    Boolean update(TmsFirstMileReconciliationDetailDTO.UpdateDTO dto);


}
