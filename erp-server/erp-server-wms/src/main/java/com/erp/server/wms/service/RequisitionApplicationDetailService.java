package com.erp.server.wms.service;
import com.erp.model.wms.entity.RequisitionApplicationDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.RequisitionApplicationDetailDTO;

/**
 * <p>
 * 要货申请单明细表 服务类
 * </p>
 *
 * @author Luo_wg
 * @since 2023-11-16
 */
public interface RequisitionApplicationDetailService extends SuperService<RequisitionApplicationDetailEntity> {

    /**
    * 新增
    * @author Luo_wg
    * @date: 2023-11-16
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(RequisitionApplicationDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author Luo_wg
    * @date: 2023-11-16
    * @param dto
    * @return
    */
    Boolean update(RequisitionApplicationDetailDTO.UpdateDTO dto);


}
