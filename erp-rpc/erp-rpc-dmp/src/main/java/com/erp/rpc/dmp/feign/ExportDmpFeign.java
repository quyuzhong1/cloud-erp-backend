package com.erp.rpc.dmp.feign;

import com.common.business.config.ExportFeignConfig;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.dto.AfterSaleDTO;
import com.erp.model.dmp.dto.DmpOutputTaskRecordDTO;
import com.erp.model.dmp.dto.DmpPullTaskDTO;
import com.erp.model.dmp.dto.DmpPushTaskDTO;
import com.erp.model.dmp.dto.excel.DmpAfterSaleExcelDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "erp-dmp", contextId = "exportDmpFeign", configuration = ExportFeignConfig.class)
public interface ExportDmpFeign {

    @PostMapping("/feign/export/pullTaskHistory")
    PagingVO<DmpPullTaskDTO.ListDTO> exportPullTaskHistory(@RequestBody PagingDTO<DmpPullTaskDTO.ParamDTO> dto);
    @PostMapping("/feign/export/pullTask")
    PagingVO<DmpPullTaskDTO.ListDTO> exportPullTask(@RequestBody PagingDTO<DmpPullTaskDTO.ParamDTO> dto);
    @PostMapping("/feign/export/pushTask")
    PagingVO<DmpPushTaskDTO.ListDTO> exportPushTask(@RequestBody PagingDTO<DmpPushTaskDTO.ParamDTO> dto);
    @PostMapping("/feign/export/pushTaskHistory")
    PagingVO<DmpPushTaskDTO.ListDTO> exportPushTaskHistory(@RequestBody PagingDTO<DmpPushTaskDTO.ParamDTO> dto);


    @PostMapping("/feign/export/exportNewDmpPushTask")
    PagingVO<DmpOutputTaskRecordDTO.PagingDTO> exportNewDmpPushTask(@RequestBody PagingDTO<DmpOutputTaskRecordDTO.ExpotParamDTO> dto);

    @PostMapping("/feign/export/exportAfterSale")
    PagingVO<DmpAfterSaleExcelDTO> exportAfterSale(@RequestBody @Validated PagingDTO<AfterSaleDTO.PagingParamDTO> dto);
}
