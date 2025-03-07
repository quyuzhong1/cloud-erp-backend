package com.erp.server.oms.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.CfgVatInvoiceDTO;
import com.erp.server.oms.query.ShopQueryHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.oms.service.InvoiceUploadService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.oms.dto.InvoiceUploadDTO;

/**
 * 上传记录
 *
 * @author zdy
 * @since 2025-03-07
 */
@Slf4j
@RestController
@LogSystemModule("上传记录")
@RequestMapping("/invoiceUpload")
public class InvoiceUploadController extends BaseController {

    @Resource
    private InvoiceUploadService invoiceUploadService;

    /**
    * 新增
    * @author zdy
    * @date:  2025-03-07
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "上传记录新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated InvoiceUploadDTO.AddDTO dto) {
        return success(invoiceUploadService.add(dto));
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
        menuCode = "oms:invoiceUpload:update",
        serviceClass = InvoiceUploadService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated InvoiceUploadDTO.UpdateDTO dto) {
        invoiceUploadService.update(dto);
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
            menuCode = "oms:invoiceUpload:paging",
            tableAlias = "iu"
    )
    @WebAdvanceQuery
    public ApiResult<PagingVO<InvoiceUploadDTO.PagingViewDTO>> queryByPage(@RequestBody @Validated PagingDTO<InvoiceUploadDTO.PagingParamDTO> dto) {
        PagingVO<InvoiceUploadDTO.PagingViewDTO> pagingVO = invoiceUploadService.paging(dto);
        return success(pagingVO);
    }

}
