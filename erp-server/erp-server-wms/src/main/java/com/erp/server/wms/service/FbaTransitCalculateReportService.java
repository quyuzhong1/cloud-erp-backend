package com.erp.server.wms.service;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.scm.dto.PurchaseOrderDTO;
import com.erp.model.wms.dto.excel.FbaTransitExcelDTO;
import com.erp.model.wms.entity.FbaTransitCalculateReportEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.FbaTransitCalculateReportDTO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * FBA在途核算报表 服务类
 * </p>
 *
 * @author zdy
 * @since 2024-12-12
 */
public interface FbaTransitCalculateReportService extends SuperService<FbaTransitCalculateReportEntity> {

    /**
     * 列表查询
     * @param dto
     * @return
     */
    PagingVO<FbaTransitCalculateReportDTO.ListDTO> paging(PagingDTO<FbaTransitCalculateReportDTO.PagingParamDTO> dto);

    /**
     * 自动计算FBA在途报表
     * @param reportMonth
     */
    void autoCalculateFbaShipment(LocalDate reportMonth);

    /**
     * 获取上月存在期末在途数量的列表
     * @param reportMonth
     * @param shipmentCode
     * @param asin
     * @param msku
     * @return
     */
    List<FbaTransitCalculateReportDTO.CalculateDTO> listByTransitAndReportMonth(LocalDate reportMonth, String shipmentCode, String asin, String msku);

    /**
     * 导入模板
     * @param response
     */
    void downloadTemplate(HttpServletResponse response);

    /**
     * 导入数据
     * @param excelFile
     * @param response
     * @return
     */
    Boolean importFile(MultipartFile excelFile, HttpServletResponse response);

    /**
     * 异步生成上月份期末数据
     * @param successList
     */
    void asyncCreateTransitCalculateReport(List<FbaTransitExcelDTO> successList);

    /**
     * 导出列表
     * @param dto
     */
    void exportExcel(FbaTransitCalculateReportDTO.PagingParamDTO dto);

    /**
     *   期末在途调整
     * @param adjustDTO
     * @return
     */
    Boolean adjustTransitQty(FbaTransitCalculateReportDTO.AdjustDTO adjustDTO);
}
