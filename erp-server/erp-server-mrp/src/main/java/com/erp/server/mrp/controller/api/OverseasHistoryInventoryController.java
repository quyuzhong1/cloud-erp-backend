package com.erp.server.mrp.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.mrp.dto.FbaHistoryInventoryDTO;
import com.erp.model.mrp.dto.OverseasHistoryInventoryDTO;
import com.erp.server.mrp.handler.OverseasHistoryInventoryHandler;
import com.erp.server.mrp.service.FbaHistoryInventoryService;
import com.erp.server.mrp.service.OverseasHistoryInventoryService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;
import com.common.core.controller.BaseController;

import javax.annotation.Resource;

/**
 * <p>
 * 海外仓库存 前端控制器
 * </p>
 *
 * @author liaohui
 * @since 2024-09-23
 */
@RestController
@RequestMapping("/overseas-history-inventory")
public class OverseasHistoryInventoryController extends BaseController {


    @Resource
    private OverseasHistoryInventoryService overseasHistoryInventoryService;

    /**
     * 分页查询
     * @param dto 入参
     */
    @PostMapping("/paging")
    @WebAdvanceQuery(handler = OverseasHistoryInventoryHandler.class)
    public ApiResult<PagingVO<OverseasHistoryInventoryDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<OverseasHistoryInventoryDTO.PagingParamDTO> dto) {
        PagingVO<OverseasHistoryInventoryDTO.ListDTO> list = overseasHistoryInventoryService.paging(dto);
        return success(list);
    }


    /**
     * 导出Excel数据
     * @param dto 入参
     */
    @PostMapping("/export")
    public ApiResult<Boolean> exportList(@RequestBody @Validated OverseasHistoryInventoryDTO.ExportDTO dto) {
        overseasHistoryInventoryService.exportList(dto);
        return success(true);
    }
}
