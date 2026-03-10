package com.erp.server.tms.controller.feign;

import com.common.business.dto.base.BaseDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.dto.ImportHistoryRecordDTO;
import com.erp.server.tms.service.ImportHistoryRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/feign/importHistoryRecord")
public class ImportHistoryRecordFeignController extends BaseController {

    @Resource
    private ImportHistoryRecordService importHistoryRecordService;

    /**
     * 导入的Excel数据（预处理、导入、导入确认）
     * @author will
     * @date 2026/1/20 18:43
     * @param dto
     * @return ApiResult<Object>
     */
    @PostMapping(value = "/preprocessingImportExcel")
    public ApiResult<List<BatchResultDTO>> preprocessingImportExcel(@RequestBody @Validated ImportHistoryRecordDTO.ImportDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getList().size());
        for (BaseDTO.ImportDTO importDTO : dto.getList()) {
            BatchResultDTO resultDTO;
            try {
                resultDTO = importHistoryRecordService.importFile(new ImportHistoryRecordDTO.ImportSyncDTO(dto,importDTO));
            }catch (Exception e){
                resultDTO = BatchResultDTO.fail(importDTO.getTaskId(), importDTO.getFileUrl(), e.getMessage());
            }
            resultDTOS.add(resultDTO);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }
}
