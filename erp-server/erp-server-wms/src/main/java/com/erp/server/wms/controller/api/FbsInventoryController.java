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
import com.erp.model.wms.dto.FbsInventoryDTO;
import com.erp.server.wms.query.FbsInventoryQueryHandler;
import com.erp.server.wms.service.FbsInventoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

/**
 * FBS库存
 *
 * @author Cursor
 * @since 2026-05-25
 */
@Slf4j
@RestController
@LogSystemModule("FBS库存")
@RequestMapping("/fbsInventory")
public class FbsInventoryController extends BaseController {

    @Resource
    private FbsInventoryService fbsInventoryService;

    /**
     * 列表查询
     *
     * @param dto 分页查询参数
     * @return 分页数据
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:fbsInventory:paging",
            tableAlias = "fi"
    )
    @WebAdvanceQuery(handler = FbsInventoryQueryHandler.class)
    public ApiResult<PagingVO<FbsInventoryDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<FbsInventoryDTO.PagingParamDTO> dto) {
        return success(fbsInventoryService.paging(dto));
    }

    /**
     * 导出Excel数据
     *
     * @param dto 导出参数
     * @param response response
     * @return 操作结果
     */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:fbsInventory:export",
            tableAlias = "fi"
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出Excel数据")
    public ApiResult<Void> exportList(@RequestBody @Validated FbsInventoryDTO.ExportDTO dto, HttpServletResponse response) {
        fbsInventoryService.exportList(dto, response);
        return success();
    }

    /**
     * 列表汇总数量
     *
     * @param pagingParamDTO 分页查询参数
     * @return 汇总数量
     */
    @PostMapping("/summaryNumber")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:fbsInventory:paging",
            tableAlias = "fi"
    )
    public ApiResult<FbsInventoryDTO.SummaryNumber> summaryNumber(@RequestBody @Validated PagingDTO<FbsInventoryDTO.PagingParamDTO> pagingParamDTO) {
        FbsInventoryDTO.SummaryNumber result = fbsInventoryService.summaryNumber(pagingParamDTO);
        return success(result);
    }
}
