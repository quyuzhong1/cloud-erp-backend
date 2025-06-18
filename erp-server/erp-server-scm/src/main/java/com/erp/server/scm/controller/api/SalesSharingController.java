package com.erp.server.scm.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.dto.CfgSupplierSalesDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.scm.service.SalesSharingService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.scm.dto.SalesSharingDTO;

/**
 * 销量共享表
 *
 * @author jack
 * @since 2025-06-18
 */
@Slf4j
@RestController
@LogSystemModule("销量共享表")
@RequestMapping("/salesSharing")
public class SalesSharingController extends BaseController {

    @Resource
    private SalesSharingService salesSharingService;


    /**
     * 列表查询
     * @author jack
     * @date: 2025-06-13
     * @param pagingParamDTO
     * @return ApiResult<PagingVO<SalesSharingDTO.ListDTO>>
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "scm:salesSharing:paging",
            tableAlias = "ss"
    )
    @WebAdvanceQuery
    public ApiResult<PagingVO<SalesSharingDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<SalesSharingDTO.PagingParamDTO> pagingParamDTO) {
        return success(salesSharingService.paging(pagingParamDTO));
    }

    /**
     * 导出Excel数据
     * @author jack
     * @date:  2025-06-13
     * @param pagingParamDTO
     * @param response
     * @return
     */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "scm:salesSharing:export",
            tableAlias = "ss"
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出Excel数据")
    @WebAdvanceQuery
    public ApiResult<Object> exportList(@RequestBody @Validated SalesSharingDTO.PagingParamDTO pagingParamDTO, HttpServletResponse response) {
        salesSharingService.exportList(pagingParamDTO, response);
        return success();
    }


}
