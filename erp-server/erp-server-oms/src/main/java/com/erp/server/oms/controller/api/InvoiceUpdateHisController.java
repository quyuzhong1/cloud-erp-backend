package com.erp.server.oms.controller.api;


import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.InvoiceUpdateHisDTO;
import com.erp.server.oms.service.InvoiceUpdateHisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 发票更新历史
 *
 * @author will
 * @since 2025-04-07
 */
@Slf4j
@RestController
@LogSystemModule("发票更新历史")
@RequestMapping("/invoiceUpdateHis")
public class InvoiceUpdateHisController extends BaseController {

    @Resource
    private InvoiceUpdateHisService invoiceUpdateHisService;

    /**
     * 列表查询（修改记录）
     * @author will
     * @date 2025/4/7 17:19
     * @param dto
     * @return ApiResult<InvoiceUpdateHisDTO.ListDTO>>
     */
    @PostMapping("/list")
    public ApiResult<List<InvoiceUpdateHisDTO.ListDTO>> list(@RequestBody @Validated InvoiceUpdateHisDTO.IdDTO dto) {
        return success(invoiceUpdateHisService.list(dto));
    }

}
