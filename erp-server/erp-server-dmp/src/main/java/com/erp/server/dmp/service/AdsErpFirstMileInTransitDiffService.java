package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.doris.AdsErpFirstMileInTransitDiffEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.AdsErpFirstMileInTransitDiffDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.FbaTransitCalculateReportDTO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
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
    * 分页列表查询
    * @author Jim
    * @date: 2025-11-13
    * @param pagingParamDTO
    * @return PagingVO<AdsErpFirstMileInTransitDiffDTO.ListDTO>>
    */
    PagingVO<AdsErpFirstMileInTransitDiffDTO.ListDTO> paging(PagingDTO<AdsErpFirstMileInTransitDiffDTO.PagingParamDTO> pagingParamDTO);


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
    Boolean exportList(AdsErpFirstMileInTransitDiffDTO.ExportDTO dto, HttpServletResponse response);

    /**
     * 导入期初模板
     */
    void downloadTemplate(HttpServletResponse response);

    /**
     * 期末在途调整
     */
    Boolean adjustTransitQty(AdsErpFirstMileInTransitDiffDTO.AdjustDTO adjustDTO);

    /**
     * 期初导入
     */
    Boolean importFile(MultipartFile excelFile, HttpServletResponse response);
}
