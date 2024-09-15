package com.erp.server.wms.service;
import com.erp.model.wms.entity.SubcontractReturnOrderDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.SubcontractReturnOrderDetailDTO;

/**
 * <p>
 * 委外退料明细单 服务类
 * </p>
 *
 * @author zdy
 * @since 2024-09-15
 */
public interface SubcontractReturnOrderDetailService extends SuperService<SubcontractReturnOrderDetailEntity> {

    /**
    * 新增
    * @author zdy
    * @date: 2024-09-15
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(SubcontractReturnOrderDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author zdy
    * @date: 2024-09-15
    * @param dto
    * @return
    */
    Boolean update(SubcontractReturnOrderDetailDTO.UpdateDTO dto);


}
