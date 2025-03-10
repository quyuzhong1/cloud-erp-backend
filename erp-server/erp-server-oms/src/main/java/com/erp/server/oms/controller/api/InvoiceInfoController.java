package com.erp.server.oms.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.vo.PagingVO;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.oms.service.InvoiceInfoService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.oms.dto.InvoiceInfoDTO;

/**
 * 上传记录
 *
 * @author zdy
 * @since 2025-03-07
 */
@Slf4j
@RestController
@LogSystemModule("上传记录")
@RequestMapping("/invoiceInfo")
public class InvoiceInfoController extends BaseController {

    @Resource
    private InvoiceInfoService invoiceInfoService;

    /**
    * 新增
    * @author zdy
    * @date:  2025-03-07
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "上传记录新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated InvoiceInfoDTO.AddDTO dto) {
        return success(invoiceInfoService.add(dto));
    }

    /**
    * 修改
    * @author zdy
    * @date:  2025-03-07
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "上传记录修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "oms:invoiceInfo:update",
        serviceClass = InvoiceInfoService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated InvoiceInfoDTO.UpdateDTO dto) {
        invoiceInfoService.update(dto);
        return success();
    }

    /**
     * 店铺 分页
     *
     * @return
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "oms:invoiceInfo:paging",
            tableAlias = "i"
    )
    @WebAdvanceQuery
    public ApiResult<PagingVO<InvoiceInfoDTO.PagingViewDTO>> queryByPage(@RequestBody @Validated PagingDTO<InvoiceInfoDTO.PagingParamDTO> dto) {
        PagingVO<InvoiceInfoDTO.PagingViewDTO> pagingVO = invoiceInfoService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 批量生成发票
     * @param idsDTO
     * @param response
     */
    @PostMapping("/exportInvoicePdf")
    public void exportSoContractPdf(@RequestBody @Valid BaseIdsDTO.IdsDTO idsDTO, HttpServletResponse response) {
        invoiceInfoService.exportInvoicePdf(idsDTO.getIds(),response);
    }
}
