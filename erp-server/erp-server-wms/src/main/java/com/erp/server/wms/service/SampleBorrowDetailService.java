package com.erp.server.wms.service;
import com.erp.model.wms.entity.SampleBorrowDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.SampleBorrowDetailDTO;

/**
 * <p>
 * 借用变更单明细表 服务类
 * </p>
 *
 * @author jack
 * @since 2025-08-20
 */
public interface SampleBorrowDetailService extends SuperService<SampleBorrowDetailEntity> {

    /**
    * 新增
    * @author jack
    * @date: 2025-08-20
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(SampleBorrowDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author jack
    * @date: 2025-08-20
    * @param dto
    * @return
    */
    Boolean update(SampleBorrowDetailDTO.UpdateDTO dto);


}
