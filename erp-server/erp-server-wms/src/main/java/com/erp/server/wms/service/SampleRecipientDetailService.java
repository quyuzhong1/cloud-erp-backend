package com.erp.server.wms.service;
import com.erp.model.wms.entity.SampleRecipientDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.SampleRecipientDetailDTO;

/**
 * <p>
 * 样品领用单明细 服务类
 * </p>
 *
 * @author wuhaotian
 * @since 2025-08-21
 */
public interface SampleRecipientDetailService extends SuperService<SampleRecipientDetailEntity> {

    /**
    * 新增
    * @author wuhaotian
    * @date: 2025-08-21
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(SampleRecipientDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author wuhaotian
    * @date: 2025-08-21
    * @param dto
    * @return
    */
    Boolean update(SampleRecipientDetailDTO.UpdateDTO dto);


}
