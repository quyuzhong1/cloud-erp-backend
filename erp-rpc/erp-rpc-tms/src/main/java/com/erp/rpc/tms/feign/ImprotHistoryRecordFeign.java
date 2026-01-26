package com.erp.rpc.tms.feign;


import com.common.business.config.FeignErrorDecoder;
import com.common.business.dto.base.BatchResultDTO;
import com.erp.model.tms.dto.ImportHistoryRecordDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 *
 */
@FeignClient(name = "erp-tms", contextId = "importHistoryRecord" ,configuration = {FeignErrorDecoder.class})
public interface ImprotHistoryRecordFeign {
    /**
     * 导入的Excel数据（预处理、导入、导入确认）
     * @author will
     * @date 2026/1/20 18:43
     * @param dto
     * @return ApiResult<Object>
     */
    @PostMapping(value = "/feign/importHistoryRecord/preprocessingImportExcel")
    List<BatchResultDTO> preprocessingImportExcel(@RequestBody ImportHistoryRecordDTO.ImportDTO dto);

}
