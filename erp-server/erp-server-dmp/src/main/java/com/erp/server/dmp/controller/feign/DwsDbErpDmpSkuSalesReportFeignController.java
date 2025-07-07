package com.erp.server.dmp.controller.feign;


import com.erp.model.dmp.entity.doris.DwsDbErpDmpSkuSalesReportFEntity;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogSystemModule;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.dmp.service.DwsDbErpDmpSkuSalesReportFService;
import com.erp.model.dmp.dto.DwsDbErpDmpSkuSalesReportFDTO;

import java.util.List;

/**
 * SKU销量报告
 *
 * @author Jim
 * @since 2025-06-23
 */
@Slf4j
@RestController
@LogSystemModule("SKU销量报告")
@RequestMapping("feign/dmp/dmpSkuSales")
public class DwsDbErpDmpSkuSalesReportFeignController extends BaseController {

    @Resource
    private DwsDbErpDmpSkuSalesReportFService dwsDbErpDmpSkuSalesReportFService;

    /**
    * 列表
    * @author Jim
    * @date:  2025-06-23
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/reportList")
    public List<DwsDbErpDmpSkuSalesReportFEntity> reportList(@RequestBody @Validated DwsDbErpDmpSkuSalesReportFDTO.RequestListDTO dto) {
        return dwsDbErpDmpSkuSalesReportFService.reportList(dto);
    }




}
