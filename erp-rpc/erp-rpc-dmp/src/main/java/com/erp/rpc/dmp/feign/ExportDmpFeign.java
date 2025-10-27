package com.erp.rpc.dmp.feign;

import com.common.business.config.ExportFeignConfig;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.dto.*;
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

    @PostMapping("/feign/export/exportDmpBasicSystem")
    PagingVO<DmpBasicSystemDTO.ListDTO> exportDmpBasicSystem(@RequestBody @Validated PagingDTO<DmpBasicSystemDTO.ExportDTO> dto);

    @PostMapping("/feign/export/exportDmpCfgEtl")
    PagingVO<DmpCfgEtlDTO.ListDTO> exportDmpCfgEtl(@RequestBody @Validated PagingDTO<DmpCfgEtlDTO.ExportDTO> dto);

    @PostMapping("/feign/export/exportDmpCfgInput")
    PagingVO<DmpCfgInputDTO.ListDTO> exportDmpCfgInput(@RequestBody @Validated PagingDTO<DmpCfgInputDTO.ExportDTO> dto);

    @PostMapping("/feign/export/exportDmpCfgInputDetail")
    PagingVO<DmpCfgInputDetailDTO.ListDTO> exportDmpCfgInputDetail(@RequestBody @Validated PagingDTO<DmpCfgInputDetailDTO.ExportDTO> dto);

    @PostMapping("/feign/export/exportDmpCfgOutput")
    PagingVO<DmpCfgOutputDTO.ListDTO> exportDmpCfgOutput(@RequestBody @Validated PagingDTO<DmpCfgOutputDTO.ExportDTO> dto);

    @PostMapping("/feign/export/exportDmpCfgOutputDetail")
    PagingVO<DmpCfgOutputDetailDTO.ListDTO> exportDmpCfgOutputDetail(@RequestBody @Validated PagingDTO<DmpCfgOutputDetailDTO.ExportDTO> dto);

    @PostMapping("/feign/export/exportDmpEtlTask")
    PagingVO<DmpEtlTaskDTO.ListDTO> exportDmpEtlTask(@RequestBody @Validated PagingDTO<DmpEtlTaskDTO.ExportDTO> dto);

    @PostMapping("/feign/export/exportDmpInputTask")
    PagingVO<DmpInputTaskDTO.ListDTO> exportDmpInputTask(@RequestBody @Validated PagingDTO<DmpInputTaskDTO.ExportDTO> dto);

    @PostMapping("/feign/export/exportDmpOutputTask")
    PagingVO<DmpOutputTaskDTO.ListDTO> exportDmpOutputTask(@RequestBody @Validated PagingDTO<DmpOutputTaskDTO.ExportDTO> dto);
}
