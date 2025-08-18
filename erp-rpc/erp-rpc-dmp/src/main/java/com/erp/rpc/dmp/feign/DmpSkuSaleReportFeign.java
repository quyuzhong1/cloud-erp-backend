package com.erp.rpc.dmp.feign;


import com.common.business.config.FeignErrorDecoder;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.dmp.dto.DwsDbErpDmpSkuSalesReportFDTO;
import com.erp.model.dmp.dto.ThridUserInfoDTO;
import com.erp.model.dmp.entity.doris.DwsDbErpDmpSkuSalesReportFEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * SKU销量报告
 *
 * @author Jim
 * @since 2025-06-23
 */
@FeignClient(value = "erp-dmp", path = "feign/dmp/dmpSkuSales", contextId = "DmpSkuSaleReportFeign",configuration = {FeignErrorDecoder.class})
public interface DmpSkuSaleReportFeign {

    /**
     * SKU销量报告
     * @author Jim
     * @since 2025-06-23
     */
    @PostMapping("/reportList")
    List<DwsDbErpDmpSkuSalesReportFEntity> reportList(@RequestBody @Validated DwsDbErpDmpSkuSalesReportFDTO.RequestListDTO dto);
}