package com.erp.server.wms.service;
import com.erp.model.wms.entity.AwdInventoryEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.AwdInventoryDTO;
import com.common.business.vo.PagingVO;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author wtr
 * @since 2025-12-26
 */
public interface AwdInventoryService extends SuperService<AwdInventoryEntity> {

    /**
    * 新增
    * @author wtr
    * @date: 2025-12-26
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(AwdInventoryDTO.AddDTO dto);

    /**
    * 分页列表查询
    * @author wtr
    * @date: 2025-12-26
    * @param pagingParamDTO
    * @return PagingVO<AwdInventoryDTO.ListDTO>>
    */
    PagingVO<AwdInventoryDTO.ListDTO> paging(PagingDTO<AwdInventoryDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 导出Excel
    * @author wtr
    * @date: 2025-12-26
    * @param dto
    * @param response
    * @return
    */
    void exportList(AwdInventoryDTO.ExportDTO dto, HttpServletResponse response);
}
