package com.erp.server.oms.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.CustomerDTO;
import lombok.extern.slf4j.Slf4j;
import javax.annotation.Resource;
import javax.validation.Valid;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.oms.service.CfgInvoiceInvalidService;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.CfgInvoiceInvalidDTO;

import java.util.List;

/**
 * 作废发票号
 *
 * @author hcg
 * @since 2025-04-14
 */
@Slf4j
@RestController
@LogSystemModule("作废发票号")
@RequestMapping("/cfgInvoiceInvalid")
public class CfgInvoiceInvalidController extends BaseController {

    @Resource
    private CfgInvoiceInvalidService cfgInvoiceInvalidService;

    /**
    * 新增
    * @author hcg
    * @date:  2025-04-14
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "作废发票号新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated CfgInvoiceInvalidDTO.AddDTO dto) {
        return success(cfgInvoiceInvalidService.add(dto));
    }

    /**
     * 分页查询
     * @author hcg
     * @date:  2025-04-14
     * @param dto
     * @return ApiResult<String>
     */
    @PostMapping("/paging")
    @WebAdvanceQuery
    public PagingVO<CfgInvoiceInvalidDTO.PagingViewDTO> paging(@RequestBody @Validated PagingDTO<CfgInvoiceInvalidDTO.PagingParamDTO> dto) {
        return cfgInvoiceInvalidService.paging(dto);
    }

    /**
     * 导出作废发票号
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出作废发票号")
    @PostMapping("/export")
    public ApiResult<Object> exportCfgInvoiceInvalid(@RequestBody @Valid CustomerDTO.ExportDTO dto) {
        Boolean result = cfgInvoiceInvalidService.export(dto);
        return Boolean.TRUE.equals(result) ? success() : failure();
    }

    /**
     * 公司下拉
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出作废发票号")
    @GetMapping("/getCompanyName")
    public List<CfgInvoiceInvalidDTO.DropDownDTO> getCompanyName() {
        return cfgInvoiceInvalidService.getCompanyName();
    }
}
