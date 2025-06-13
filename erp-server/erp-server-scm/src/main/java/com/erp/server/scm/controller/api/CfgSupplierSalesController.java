package com.erp.server.scm.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.vo.PagingVO;
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
import com.erp.server.scm.service.CfgSupplierSalesService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.scm.dto.CfgSupplierSalesDTO;

/**
 * 销量设置
 *
 * @author jack
 * @since 2025-06-13
 */
@Slf4j
@RestController
@LogSystemModule("销量设置")
@RequestMapping("/cfgSupplierSales")
public class CfgSupplierSalesController extends BaseController {

    @Resource
    private CfgSupplierSalesService cfgSupplierSalesService;

    /**
    * 新增
    * @author jack
    * @date:  2025-06-13
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "销量设置新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated CfgSupplierSalesDTO.AddDTO dto) {
        return success(cfgSupplierSalesService.add(dto));
    }

    /**
    * 修改
    * @author jack
    * @date:  2025-06-13
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "销量设置修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "scm:cfgSupplierSales:update",
        serviceClass = CfgSupplierSalesService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated CfgSupplierSalesDTO.UpdateDTO dto) {
        cfgSupplierSalesService.update(dto);
        return success();
    }

    /**
     * 列表查询
     * @author jack
     * @date: 2025-06-13
     * @param pagingParamDTO
     * @return ApiResult<PagingVO<CfgSupplierSalesDTO.ListDTO>>
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "scm:cfgSupplierSales:paging",
            tableAlias = "css"
    )
    @WebAdvanceQuery
    public ApiResult<PagingVO<CfgSupplierSalesDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<CfgSupplierSalesDTO.PagingParamDTO> pagingParamDTO) {
        return success(cfgSupplierSalesService.paging(pagingParamDTO));
    }



}
