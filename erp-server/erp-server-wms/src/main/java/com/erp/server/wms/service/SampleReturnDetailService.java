package com.erp.server.wms.service;
import com.erp.model.wms.entity.SampleReturnDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.SampleReturnDetailDTO;

import java.util.List;

/**
 * <p>
 * 样品归还单明细表 服务类
 * </p>
 *
 * @author jack
 * @since 2025-08-20
 */
public interface SampleReturnDetailService extends SuperService<SampleReturnDetailEntity> {

    /**
    * 新增
    * @author jack
    * @date: 2025-08-20
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(SampleReturnDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author jack
    * @date: 2025-08-20
    * @param dto
    * @return
    */
    Boolean update(SampleReturnDetailDTO.UpdateDTO dto);


    List<SampleReturnDetailEntity> listByMainId(String id);
}
