package com.erp.server.wms.service;
import com.erp.model.wms.entity.AwdInventoryEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.AwdInventoryDTO;
import com.common.business.vo.PagingVO;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

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
    * 修改
    * @author wtr
    * @date: 2025-12-26
    * @param dto
    * @return
    */
    Boolean update(AwdInventoryDTO.UpdateDTO dto);


    /**
    * 分页列表查询
    * @author wtr
    * @date: 2025-12-26
    * @param pagingParamDTO
    * @return PagingVO<AwdInventoryDTO.ListDTO>>
    */
    PagingVO<AwdInventoryDTO.ListDTO> paging(PagingDTO<AwdInventoryDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author wtr
    * @date: 2025-12-26
    * @param dto
    * @return List<AwdInventoryDTO.TabListDTO>>
    */
    List<AwdInventoryDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author wtr
    * @date: 2025-12-26
    * @param id
    * @return
    */
    AwdInventoryDTO.ViewDTO view(String id);


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
