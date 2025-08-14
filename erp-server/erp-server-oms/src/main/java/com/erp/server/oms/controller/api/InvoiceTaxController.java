package com.erp.server.oms.controller.api;


import com.common.business.dto.base.BatchResultDTO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.oms.dto.InvoiceTaxDTO;
import com.erp.server.oms.service.InvoiceTaxService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 发票税务信息
 *
 * @author will
 * @since 2025-04-07
 */
@Slf4j
@RestController
@LogSystemModule("发票税务信息")
@RequestMapping("/invoiceTax")
public class InvoiceTaxController extends BaseController {

    @Resource
    private InvoiceTaxService invoiceTaxService;


    /**
    * 新增或修改
    * @author will
    * @date:  2025-04-07
    * @param list
    * @return ApiResult
    */
    @PostMapping("/addOrUpdate")
    @LogAction(value = LogActionEnum.INSERT, desc = "新增或修改")
    public ApiResult<List<BatchResultDTO>> addOrUpdate(@RequestBody @Validated List<InvoiceTaxDTO.UpdateDTO> list) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(list.size());
        for (InvoiceTaxDTO.UpdateDTO updateDTO :list) {
            BatchResultDTO resultDTO;
            try {
                resultDTO =  invoiceTaxService.addOrUpdate(updateDTO);
            }catch (Exception e){
                log.error("生成发票税务信息失败",e);
                resultDTO = BatchResultDTO.fail(updateDTO.getPlatformSkuNo(), updateDTO.getPlatformSkuNo(), e.getMessage());
            }
            resultDTOS.add(resultDTO);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }


    /**
     * 根据业务id查询详情
     * @author will
     * @date 2025/4/7 16:41
     * @param listingId
     * @return ApiResult<VirtualWarehouseAllocationDTO.ViewDTO>
     */
    @GetMapping("/view")
    @LogViewService
    public ApiResult<InvoiceTaxDTO.ViewDTO> view(@RequestParam(value = "listingId") String listingId) {
        return success(invoiceTaxService.view(listingId));
    }
}
