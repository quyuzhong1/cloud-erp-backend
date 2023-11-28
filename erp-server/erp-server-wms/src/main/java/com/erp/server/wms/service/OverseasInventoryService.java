package com.erp.server.wms.service;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.entity.OverseasInventoryEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.OverseasInventoryDTO;

import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 海外仓库存 服务类
 * </p>
 *
 * @author Jim
 * @since 2023-11-16
 */
public interface OverseasInventoryService extends SuperService<OverseasInventoryEntity> {

    /**
    * 新增
    * @author Jim
    * @date: 2023-11-16
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(OverseasInventoryDTO.AddDTO dto);

    /**
    * 修改
    * @author Jim
    * @date: 2023-11-16
    * @param dto
    * @return
    */
    Boolean update(OverseasInventoryDTO.UpdateDTO dto);


    /**
     * 分页列表
     * @author Jim
     * @date: 2023-11-20
     * @param dto
     * @return
     */
    PagingVO<OverseasInventoryDTO.ListDTO> paging(PagingDTO<OverseasInventoryDTO.PagingParamDTO> dto);

    /**
     * 列表合计
     * @author Jim
     * @date: 2023-11-21
     * @param dto
     * @return
     */
    OverseasInventoryDTO.ListTotalDTO queryParamsTotal(OverseasInventoryDTO.PagingParamDTO dto);

    /**
     * 导出海外仓
     * @author Jim
     * @date: 2023-11-21
     * @param dto
     * @return
     */
    Boolean exportExcel(OverseasInventoryDTO.ExportDTO dto, HttpServletResponse response);

    Boolean saveOrUpdateByPlatform(OverseasInventoryEntity entity);

}
