package com.erp.server.wms.service;
import com.erp.model.wms.entity.SubcontractReturnDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.SubcontractReturnDetailDTO;

/**
 * <p>
 * 委外退料明细单 服务类
 * </p>
 *
 * @author zdy
 * @since 2024-09-15
 */
public interface SubcontractReturnDetailService extends SuperService<SubcontractReturnDetailEntity> {

    /**
    * 新增
    * @author zdy
    * @date: 2024-09-15
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(SubcontractReturnDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author zdy
    * @date: 2024-09-15
    * @param dto
    * @return
    */
    Boolean update(SubcontractReturnDetailDTO.UpdateDTO dto);


}
