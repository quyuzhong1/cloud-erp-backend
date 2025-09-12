package com.erp.server.wms.controller.api;

import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.wms.dto.SampleLedgerFlowDTO;
import com.erp.server.wms.query.SampleLedgerFlowQueryHandler;
import com.erp.server.wms.query.SampleLedgerQueryHandler;
import com.erp.server.wms.service.SampleLedgerFlowService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

/**
 * 样品台账流水
 *
 * @author wuhaotian
 * @since 2025-08-21
 */
@RestController
@LogSystemModule("样品台账流水")
@RequestMapping("/sampleLedgerFlow")
public class SampleLedgerFlowController extends BaseController {

    @Resource
    private SampleLedgerFlowService sampleLedgerFlowService;

    /**
     * 分页查询
     * @author wuhaotian
     * @date: 2025-08-21
     * @param dto
     * @return
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:sampleLedgerFlow:paging",
            tableAlias = ""
    )
    @WebAdvanceQuery(handler = SampleLedgerFlowQueryHandler.class)
    public ApiResult<PagingVO<SampleLedgerFlowDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<SampleLedgerFlowDTO.PagingParamDTO> dto) {
        return ApiResult.success(sampleLedgerFlowService.paging(dto));
    }

    /**
     * 异步导出
     * @author wuhaotian
     * @date: 2025-08-21
     * @param dto
     * @param response
     * @return
     */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:sampleLedgerFlow:export",
            tableAlias = ""
    )
    @WebAdvanceQuery(handler = SampleLedgerFlowQueryHandler.class)
    @LogAction(value = LogActionEnum.EXPORT, desc = "样品台账流水导出Excel数据")
    public ApiResult<Boolean> exportList(@RequestBody @Validated SampleLedgerFlowDTO.ExportDTO dto, HttpServletResponse response) {
        sampleLedgerFlowService.exportList(dto, response);
        return ApiResult.success(true);
    }
}
