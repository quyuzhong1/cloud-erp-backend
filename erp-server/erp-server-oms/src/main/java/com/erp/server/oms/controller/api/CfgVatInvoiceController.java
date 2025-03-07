package com.erp.server.oms.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.ShopDTO;
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
import com.erp.server.oms.service.CfgVatInvoiceService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.oms.dto.CfgVatInvoiceDTO;

/**
 * VAT发票设置
 *
 * @author zdy
 * @since 2025-03-07
 */
@Slf4j
@RestController
@LogSystemModule("VAT发票设置")
@RequestMapping("/cfgVatInvoice")
public class CfgVatInvoiceController extends BaseController {

    @Resource
    private CfgVatInvoiceService cfgVatInvoiceService;

    /**
    * 新增
    * @author zdy
    * @date:  2025-03-07
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "VAT发票设置新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated CfgVatInvoiceDTO.AddDTO dto) {
        return success(cfgVatInvoiceService.add(dto));
    }

    /**
    * 修改
    * @author zdy
    * @date:  2025-03-07
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "VAT发票设置修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "oms:cfgVatInvoice:update",
        serviceClass = CfgVatInvoiceService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated CfgVatInvoiceDTO.UpdateDTO dto) {
        cfgVatInvoiceService.update(dto);
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
            menuCode = "oms:cfgVatInvoice:paging",
            tableAlias = "cvi"
    )
    @WebAdvanceQuery
    public ApiResult<PagingVO<CfgVatInvoiceDTO.PagingViewDTO>> queryByPage(@RequestBody @Validated PagingDTO<CfgVatInvoiceDTO.PagingParamDTO> dto) {
        PagingVO<CfgVatInvoiceDTO.PagingViewDTO> pagingVO = cfgVatInvoiceService.paging(dto);
        return success(pagingVO);
    }

}
