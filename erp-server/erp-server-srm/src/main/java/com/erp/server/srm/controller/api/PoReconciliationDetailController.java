package com.erp.server.srm.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.vo.PagingVO;
import com.erp.model.srm.dto.PoReconciliationDTO;
import com.erp.model.wms.dto.SubcontractIssueDTO;
import com.erp.server.srm.query.PoReconciliationDetailQueryHandler;
import com.erp.server.srm.query.PoReconciliationQueryHandler;
import com.erp.server.srm.service.PoReconciliationService;
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
import com.erp.server.srm.service.PoReconciliationDetailService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.srm.dto.PoReconciliationDetailDTO;

/**
 * 对账单明细【srm】
 *
 * @author will
 * @since 2024-01-19
 */
@Slf4j
@RestController
@LogSystemModule("采购对账单明细")
@RequestMapping("/poReconciliationDetail")
public class PoReconciliationDetailController extends BaseController {

    @Resource
    private PoReconciliationDetailService poReconciliationDetailService;


    /**
     * 分页查询
     * @author Will
     * @date: 2024/1/20 11:31
     * @param dto
     * @return ApiResult<PagingVO<ListDTO>>
     */
    @PostMapping("/paging")
    @WebAdvanceQuery(handler = PoReconciliationDetailQueryHandler.class)
    public ApiResult<PagingVO<PoReconciliationDetailDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<PoReconciliationDetailDTO.PagingParamDTO> dto) {
        return success(poReconciliationDetailService.paging(dto));
    }


    /**
     * 导出
     * @author Will
     * @date: 2024/1/20 12:03
     * @param dto
     * @param response
     */
    @PostMapping("/export")
    @LogAction(value = LogActionEnum.EXPORT, desc = "采购对账单导出Excel数据")
    @WebAdvanceQuery(handler = PoReconciliationQueryHandler.class)
    public void exportList(@RequestBody @Validated PoReconciliationDetailDTO.PagingParamDTO dto, HttpServletResponse response) {
        poReconciliationDetailService.exportList(dto, response);
    }


}
