package com.erp.server.wms.service;
import com.erp.model.wms.entity.SampleAdjustmentDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.SampleAdjustmentDetailDTO;

import java.util.List;

/**
 * <p>
 * 样品调整单明细表 服务类
 * </p>
 *
 * @author wuhaotian
 * @since 2025-11-14
 */
public interface SampleAdjustmentDetailService extends SuperService<SampleAdjustmentDetailEntity> {

    /**
    * 新增
    * @author wuhaotian
    * @date: 2025-11-14
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(SampleAdjustmentDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author wuhaotian
    * @date: 2025-11-14
    * @param dto
    * @return
    */
    Boolean update(SampleAdjustmentDetailDTO.UpdateDTO dto);

    /**
     * 根据主表ID获取明细列表
     * @param mainId 主表ID
     * @return 明细列表
     */
    List<SampleAdjustmentDetailEntity> listByMainId(String mainId);


}
