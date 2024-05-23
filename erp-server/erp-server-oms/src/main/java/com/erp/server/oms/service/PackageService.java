package com.erp.server.oms.service;

import com.common.business.dto.PlatformOrderDTO;
import com.common.business.dto.PrintWayBillPdfDTO;
import com.common.business.dto.WalmartShipDTO;
import com.common.business.dto.base.*;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.PackageDTO;
import com.erp.model.oms.dto.ReportDTO;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.dto.SoB2cLogisticsDTO;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.enums.SoB2cCategoryTypeEnum;
import com.erp.model.oms.enums.SoB2cInvalidTypeEnum;
import com.erp.model.tms.dto.SettingForecastDTO;
import com.erp.model.tms.dto.TransferDeclareDTO;
import com.erp.model.tms.dto.TransferDeclareDetailDTO;
import com.erp.model.tms.dto.TransferDeclareGenerationSettingDTO;
import com.erp.model.wms.dto.SoOutstockDTO;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * @description
 * @return
 * @date 2024-01-30 11:06
 * @author Lambda
 */
public interface PackageService  {


    /**
     * 合并组包
     * @param dto
     * @return
     */
    List<BatchResultDTO> mergePackage(PackageDTO.MergePackageDTO dto);
}
