package com.erp.server.wms.service;
import com.common.business.validator.ValidList;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.SoOutstockDTO;
import com.erp.model.wms.entity.PackageForecastEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.PackageForecastDTO;

import java.util.List;

/**
 * <p>
 * 组包预报表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2024-01-26
 */
public interface PackageForecastService extends SuperService<PackageForecastEntity> {

    /**
    * 新增
    * @author Lambda
    * @date: 2024-01-26
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(PackageForecastDTO.AddDTO dto);

    /**
    * 修改
    * @author Lambda
    * @date: 2024-01-26
    * @param dto
    * @return
    */
    Boolean update(PackageForecastDTO.UpdateDTO dto);


    /**
     *
     * @param
     * @return
     */
    List<PackageForecastDTO.TabListDTO> tabList();

    /**
     * 分页
     * @param dto
     * @return
     */
    PagingVO<PackageForecastDTO.PagingViewDTO> paging(PagingDTO<PackageForecastDTO.PagingParamDTO> dto);

    /**
     * 详情
     * @param id
     * @return
     */
    PackageForecastDTO.ViewDTO view(String id);
}
