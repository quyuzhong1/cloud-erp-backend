package com.erp.rpc.tms.feign;

import com.common.business.config.ExportFeignConfig;
import com.common.business.dto.base.BaseDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.*;
import com.erp.model.tms.dto.excel.CfgReconciliationFieldExportExcelDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

@FeignClient(name = "erp-tms", contextId = "importTmsFeign", configuration = ExportFeignConfig.class)
public interface ImportTmsFeign {

    @PostMapping("/feign/import/logisticsBillCost")
    void importLogisticsBillCost(@RequestBody BaseDTO.ImportDTO dto);

    @PostMapping("/feign/import/logisticsLastMileCost")
    void importLogisticsLastMileCost(@RequestBody BaseDTO.ImportDTO dto);
    @PostMapping("/feign/import/importLogisticsTrackInfo")
    void importLogisticsTrackInfo(@RequestBody BaseDTO.ImportDTO dto);
}
