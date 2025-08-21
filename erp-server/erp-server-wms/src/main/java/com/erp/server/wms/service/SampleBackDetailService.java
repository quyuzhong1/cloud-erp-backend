package com.erp.server.wms.service;
import com.erp.model.wms.entity.SampleBackDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.SampleBackDetailDTO;

/**
 * <p>
 * 样品退回详情 服务类
 * </p>
 *
 * @author wuhaotian
 * @since 2025-08-21
 */
public interface SampleBackDetailService extends SuperService<SampleBackDetailEntity> {

    /**
    * 新增
    * @author wuhaotian
    * @date: 2025-08-21
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(SampleBackDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author wuhaotian
    * @date: 2025-08-21
    * @param dto
    * @return
    */
    Boolean update(SampleBackDetailDTO.UpdateDTO dto);


}
