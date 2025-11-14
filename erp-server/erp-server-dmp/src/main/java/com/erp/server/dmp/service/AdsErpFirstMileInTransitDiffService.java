package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.doris.AdsErpFirstMileInTransitDiffEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.AdsErpFirstMileInTransitDiffDTO;
import com.common.business.vo.PagingVO;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 平台在途报告 服务类
 * </p>
 *
 * @author Jim
 * @since 2025-11-13
 */
public interface AdsErpFirstMileInTransitDiffService extends SuperService<AdsErpFirstMileInTransitDiffEntity> {

    /**
    * 新增
    * @author Jim
    * @date: 2025-11-13
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(AdsErpFirstMileInTransitDiffDTO.AddDTO dto);

    /**
    * 修改
    * @author Jim
    * @date: 2025-11-13
    * @param dto
    * @return
    */
    Boolean update(AdsErpFirstMileInTransitDiffDTO.UpdateDTO dto);


    /**
    * 分页列表查询
    * @author Jim
    * @date: 2025-11-13
    * @param pagingParamDTO
    * @return PagingVO<AdsErpFirstMileInTransitDiffDTO.ListDTO>>
    */
    PagingVO<AdsErpFirstMileInTransitDiffDTO.ListDTO> paging(PagingDTO<AdsErpFirstMileInTransitDiffDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author Jim
    * @date: 2025-11-13
    * @param dto
    * @return List<AdsErpFirstMileInTransitDiffDTO.TabListDTO>>
    */
    List<AdsErpFirstMileInTransitDiffDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author Jim
    * @date: 2025-11-13
    * @param id
    * @return
    */
    AdsErpFirstMileInTransitDiffDTO.ViewDTO view(String id);


    /**
    * 导出Excel
    * @author Jim
    * @date: 2025-11-13
    * @param dto
    * @param response
    * @return
    */
    void exportList(AdsErpFirstMileInTransitDiffDTO.ExportDTO dto, HttpServletResponse response);
}
