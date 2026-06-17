package com.erp.server.tms.controller.feign;

import com.common.business.dto.base.BaseDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.dto.ImportHistoryRecordDTO;
import com.erp.model.tms.dto.LogisticsReconDTO;
import com.erp.server.tms.service.LogisticsReconService;
import com.erp.server.tms.util.LogisticsReconOpenImportConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/feign/importHistoryRecord")
public class ImportHistoryRecordFeignController extends BaseController {

    @Resource
    private LogisticsReconService logisticsReconService;

    /**
     * 开放接口：物流费用 Excel 导入（已切换为物流商对账单导入落库，入参契约保持不变）。
     *
     * @author Will
     * @date 2026/6/12
     * @param dto 原物流费用导入请求（processingType / 对账月份 / 文件列表等）
     * @return 各文件异步导入任务结果
     */
    @PostMapping(value = "/preprocessingImportExcel")
    public ApiResult<List<BatchResultDTO>> preprocessingImportExcel(
            @RequestBody @Validated ImportHistoryRecordDTO.ImportDTO dto) {
        LogisticsReconDTO.ImportBatchDTO reconBatch = LogisticsReconOpenImportConverter.toReconImportBatch(dto);
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getList().size());
        for (BaseDTO.ImportDTO importDTO : dto.getList()) {
            BatchResultDTO resultDTO;
            try {
                BaseResultDTO.AddDTO addDTO =
                        logisticsReconService.importExcel(new LogisticsReconDTO.ImportDTO(reconBatch, importDTO));
                resultDTO = BatchResultDTO.success(addDTO.getId(), addDTO.getCode());
            } catch (Exception e) {
                log.error("[preprocessingImportExcel] 物流商对账单导入任务提交失败 fileName={}",
                        importDTO.getFileName(), e);
                resultDTO = BatchResultDTO.fail(importDTO.getTaskId(), importDTO.getFileName(), e.getMessage());
            }
            resultDTOS.add(resultDTO);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }
}
