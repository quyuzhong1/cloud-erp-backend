package com.erp.server.wms.service;
import com.erp.model.wms.entity.PackageForecastDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.PackageForecastDetailDTO;

import java.util.List;

/**
 * <p>
 * 组包预报详情 服务类
 * </p>
 *
 * @author Lambda
 * @since 2024-01-26
 */
public interface PackageForecastDetailService extends SuperService<PackageForecastDetailEntity> {

    /**
    * 新增
    * @author Lambda
    * @date: 2024-01-26
    * @param mainId
    * @param detailList
    * @return
    */
    void add(String mainId, List<PackageForecastDetailDTO.AddDTO> detailList);

    /**
    * 修改
    * @author Lambda
    * @date: 2024-01-26
    * @param dto
    * @return
    */
    Boolean update(PackageForecastDetailDTO.UpdateDTO dto);

    /**
     * 获取对应详情
     * @param id
     * @return
     */
    List<PackageForecastDetailDTO.ViewDTO> listDetailViewByMainId(String id);
}
