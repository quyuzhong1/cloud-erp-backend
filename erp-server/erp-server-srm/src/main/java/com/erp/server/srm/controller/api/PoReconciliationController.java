package com.erp.server.srm.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.vo.PagingVO;
import com.erp.model.srm.dto.PoReconciliationDetailDTO;
import com.erp.server.srm.query.PoReconciliationQueryHandler;
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
import com.erp.server.srm.service.PoReconciliationService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.srm.dto.PoReconciliationDTO;

/**
 * 采购对账单
 *
 * @author will
 * @since 2024-01-19
 */
@Slf4j
@RestController
@LogSystemModule("采购对账单")
@RequestMapping("/poReconciliation")
public class PoReconciliationController extends BaseController {

    @Resource
    private PoReconciliationService poReconciliationService;


    /**
     * 分页查询
     * @author Will
     * @date: 2024/1/20 12:11
     * @param dto
     * @return ApiResult<PagingVO<ListDTO>>
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:poReconciliation:paging",
            tableAlias = "pr"
    )
    @WebAdvanceQuery(handler = PoReconciliationQueryHandler.class)
    public ApiResult<PagingVO<PoReconciliationDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<PoReconciliationDTO.PagingParamDTO> dto) {
        return success(poReconciliationService.paging(dto));
    }

    /**
     * 修改
     * @author will
     * @date:  2024-01-19
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "采购对账单修改")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "srm:poReconciliation:update",
            serviceClass = PoReconciliationService.class,
            keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated PoReconciliationDTO.UpdateDTO dto) {
        poReconciliationService.update(dto);
        return success();
    }


    /**
     * 导出
     * @author Will
     * @date: 2024/1/20 12:03
     * @param dto
     * @param response
     */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:poReconciliation:paging",
            tableAlias = "pr"
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "采购对账单导出Excel数据")
    @WebAdvanceQuery(handler = PoReconciliationQueryHandler.class)
    public void exportList(@RequestBody @Validated PoReconciliationDTO.PagingParamDTO dto, HttpServletResponse response) {
        poReconciliationService.exportList(dto, response);
    }


}
