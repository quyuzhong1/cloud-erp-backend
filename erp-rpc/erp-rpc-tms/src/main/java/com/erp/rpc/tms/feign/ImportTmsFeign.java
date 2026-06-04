package com.erp.rpc.tms.feign;

import com.common.business.config.ExportFeignConfig;
import com.common.business.dto.base.BaseDTO;
import com.erp.model.tms.dto.ImportHistoryRecordDTO;
import com.erp.model.tms.dto.LogisticsReconDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "erp-tms", contextId = "importTmsFeign", configuration = ExportFeignConfig.class)
public interface ImportTmsFeign {

    @PostMapping("/feign/import/logisticsBillCost")
    void importLogisticsBillCost(@RequestBody BaseDTO.ImportDTO dto);

    @PostMapping("/feign/import/logisticsLastMileCost")
    void importLogisticsLastMileCost(@RequestBody BaseDTO.ImportDTO dto);
    @PostMapping("/feign/import/importLogisticsTrackInfo")
    void importLogisticsTrackInfo(@RequestBody BaseDTO.ImportDTO dto);
    @PostMapping("/feign/import/importCfgLogisticsCost")
    void importCfgLogisticsCost(@RequestBody BaseDTO.ImportDTO dto);

    @PostMapping("/feign/import/preprocessingImportExcel")
    void preprocessingImportExcel(@RequestBody ImportHistoryRecordDTO.ImportSyncDTO importSyncDTO);

    @PostMapping("/feign/import/importLogisticsThirdChannelRef")
    void importLogisticsThirdChannelRef(@RequestBody ImportHistoryRecordDTO.ImportSyncDTO importSyncDTO);

    @PostMapping("/feign/import/importLogisticsRecon")
    void importLogisticsRecon(@RequestBody LogisticsReconDTO.ImportDTO dto);
}
