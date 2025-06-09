package com.erp.server.mrp.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.mrp.dto.FbaHistoryInventoryDTO;
import com.erp.model.wms.dto.FbaInventoryDTO;
import com.erp.server.mrp.service.FbaHistoryInventoryService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;
import com.common.core.controller.BaseController;

import javax.annotation.Resource;

/**
 * <p>
 * fba历史库存 前端控制器
 * </p>
 *
 * @author Lambda
 * @since 2024-09-12
 */
@RestController
@RequestMapping("/fba-history-inventory")
public class FbaHistoryInventoryController extends BaseController {

    @Resource
    private FbaHistoryInventoryService fbaHistoryInventoryService;

    /**
     * 分页查询
     * @param dto 入参
     */
    @PostMapping("/paging")
    @WebAdvanceQuery
    public ApiResult<PagingVO<FbaHistoryInventoryDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<FbaHistoryInventoryDTO.PagingParamDTO> dto) {
        PagingVO<FbaHistoryInventoryDTO.ListDTO> list = fbaHistoryInventoryService.paging(dto);
        return success(list);
    }


    /**
     * 导出Excel数据
     * @param dto 入参
     */
    @PostMapping("/export")
    public ApiResult<Boolean> exportList(@RequestBody @Validated FbaHistoryInventoryDTO.ExportDTO dto) {
        fbaHistoryInventoryService.exportList(dto);
        return success(true);
    }

}
