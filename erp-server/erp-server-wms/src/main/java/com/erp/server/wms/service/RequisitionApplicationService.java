package com.erp.server.wms.service;
import com.erp.model.wms.entity.RequisitionApplicationEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.RequisitionApplicationDTO;

/**
 * <p>
 * 要货申请单 服务类
 * </p>
 *
 * @author Luo_wg
 * @since 2023-11-16
 */
public interface RequisitionApplicationService extends SuperService<RequisitionApplicationEntity> {

    /**
    * 新增
    * @author Luo_wg
    * @date: 2023-11-16
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(RequisitionApplicationDTO.AddDTO dto);

    /**
    * 修改
    * @author Luo_wg
    * @date: 2023-11-16
    * @param dto
    * @return
    */
    Boolean update(RequisitionApplicationDTO.UpdateDTO dto);


}
