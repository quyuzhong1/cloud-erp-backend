package com.erp.server.wms.service;
import com.erp.model.wms.entity.SubcontractIssueDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.SubcontractIssueDetailDTO;

/**
 * <p>
 * 委外发料明细单 服务类
 * </p>
 *
 * @author will
 * @since 2024-01-08
 */
public interface SubcontractIssueDetailService extends SuperService<SubcontractIssueDetailEntity> {

    /**
    * 新增
    * @author will
    * @date: 2024-01-08
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(SubcontractIssueDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2024-01-08
    * @param dto
    * @return
    */
    Boolean update(SubcontractIssueDetailDTO.UpdateDTO dto);


}
