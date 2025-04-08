package com.erp.server.oms.service;

import com.common.business.dto.PlatformOrderDTO;
import com.common.business.dto.PlatformSoOutStockDTO;
import com.common.business.dto.PrintWayBillPdfDTO;
import com.common.business.dto.WalmartShipDTO;
import com.common.business.dto.base.*;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.*;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.SoB2cCategoryTypeEnum;
import com.erp.model.oms.enums.SoB2cInvalidTypeEnum;
import com.erp.model.plm.entity.ProductCustomsEntity;
import com.erp.model.tms.dto.SettingForecastDTO;
import com.erp.model.tms.dto.TransferDeclareDTO;
import com.erp.model.tms.dto.TransferDeclareDetailDTO;
import com.erp.model.tms.dto.TransferDeclareGenerationSettingDTO;
import com.erp.model.wms.dto.ReportOrderDataDTO;
import com.erp.model.wms.dto.SoOutstockDTO;
import com.erp.model.wms.dto.WmsDataCompareTaskDTO;
import com.erp.model.wms.dto.inventory.InventoryQtyDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 报表
 */
public interface ReportManagerService {

    /**
     * 报表管理 销售统计
     * @author yl
     * @date 2023-09-01 11:19
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.oms.dto.ReportDTO.ProductSalesPagingViewDTO>
     */
    PagingVO<ReportDTO.ProductSalesPagingViewDTO> productSalesPaging(PagingDTO<ReportDTO.ProductSalesPagingParamDTO> dto);


    /**
     * 导出 销售统计
     * @author yl
     * @date 2023-09-04 16:39
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean productSalesExport(ReportDTO.ProductSalesPagingParamDTO dto);


    /**
     * 导出销售额
     * @param dto 参数
     */
    PagingVO<ReportDTO.ProductSalesPagingViewDTO> exportSoB2CProductSales(PagingDTO<ReportDTO.ProductSalesPagingParamDTO> dto);

}
