package com.erp.server.wms.service;
import com.erp.model.wms.entity.WmsDataComparePlanEntity;
import com.common.business.service.SuperService;

import java.util.List;

import com.common.business.dto.base.*;
import com.erp.model.wms.dto.WmsDataComparePlanDTO;

/**
 * <p>
 * 数据对比映射方案 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-03-20
 */
public interface WmsDataComparePlanService extends SuperService<WmsDataComparePlanEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-03-20
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(WmsDataComparePlanDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-03-20
    * @param dto
    * @return
    */
    Boolean update(WmsDataComparePlanDTO.UpdateDTO dto);

    List<WmsDataComparePlanDTO.ViewDTO> get(WmsDataComparePlanDTO.CommonDTO dto);
}
