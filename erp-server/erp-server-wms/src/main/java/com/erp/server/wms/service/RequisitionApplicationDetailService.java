package com.erp.server.wms.service;
import com.erp.model.wms.dto.RequisitionApplicationDTO;
import com.erp.model.wms.entity.RequisitionApplicationDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.RequisitionApplicationDetailDTO;

/**
 * <p>
 * 要货申请单明细表 服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
 */
public interface RequisitionApplicationDetailService extends SuperService<RequisitionApplicationDetailEntity> {

    /**
    * 新增
    * @author Luo_WG
    * @date: 2023-11-16
    * @param dto
    * @param mainId
    * @return
    */
    BaseResultDTO.AddDTO add(RequisitionApplicationDTO.AddDTO dto, String mainId);

    /**
    * 修改
    * @author Luo_WG
    * @date: 2023-11-16
    * @param dto
    * @param mainId
    * @return
    */
    Boolean update(RequisitionApplicationDTO.UpdateDTO dto, String mainId);


}
