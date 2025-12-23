package com.erp.server.wms.service;
import com.erp.model.wms.entity.AwdOutstockDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.AwdOutstockDetailDTO;
import com.common.business.vo.PagingVO;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author wtr
 * @since 2025-12-22
 */
public interface AwdOutstockDetailService extends SuperService<AwdOutstockDetailEntity> {

    /**
    * 新增
    * @author wtr
    * @date: 2025-12-22
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(AwdOutstockDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author wtr
    * @date: 2025-12-22
    * @param dto
    * @return
    */
    Boolean update(AwdOutstockDetailDTO.UpdateDTO dto);


    /**
    * 分页列表查询
    * @author wtr
    * @date: 2025-12-22
    * @param pagingParamDTO
    * @return PagingVO<AwdOutstockDetailDTO.ListDTO>>
    */
    PagingVO<AwdOutstockDetailDTO.ListDTO> paging(PagingDTO<AwdOutstockDetailDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author wtr
    * @date: 2025-12-22
    * @param dto
    * @return List<AwdOutstockDetailDTO.TabListDTO>>
    */
    List<AwdOutstockDetailDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author wtr
    * @date: 2025-12-22
    * @param id
    * @return
    */
    AwdOutstockDetailDTO.ViewDTO view(String id);


    /**
    * 导出Excel
    * @author wtr
    * @date: 2025-12-22
    * @param dto
    * @param response
    * @return
    */
    void exportList(AwdOutstockDetailDTO.ExportDTO dto, HttpServletResponse response);

    boolean add(List<AwdOutstockDetailDTO.AddDTO> awdDetailList,String mainId);
}
