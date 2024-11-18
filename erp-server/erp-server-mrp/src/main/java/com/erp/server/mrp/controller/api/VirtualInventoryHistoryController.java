package com.erp.server.mrp.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.mrp.dto.VirtualInventoryHistoryDTO;
import com.erp.server.mrp.handler.VirtualInventoryHistoryHandler;
import com.erp.server.mrp.service.VirtualInventoryHistoryService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;
import com.common.core.controller.BaseController;

import javax.annotation.Resource;

/**
 * <p>
 * 虚拟库存表 前端控制器
 * </p>
 *
 * @author liaohui
 * @since 2024-09-27
 */
@RestController
@RequestMapping("/virtual-inventory")
public class VirtualInventoryHistoryController extends BaseController {
    @Resource
    private VirtualInventoryHistoryService virtualInventoryHistoryService;

    /**
     * 分页查询
     * @param dto 入参
     */
    @PostMapping("/paging")
    @WebAdvanceQuery(handler = VirtualInventoryHistoryHandler.class)
    public ApiResult<PagingVO<VirtualInventoryHistoryDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<VirtualInventoryHistoryDTO.SearchParamDTO> dto) {
        PagingVO<VirtualInventoryHistoryDTO.ListDTO> list = virtualInventoryHistoryService.paging(dto);
        return success(list);
    }


    /**
     * 导出Excel数据
     * @param dto 入参
     */
    @PostMapping("/export")
    public ApiResult<Boolean> exportList(@RequestBody @Validated VirtualInventoryHistoryDTO.SearchParamDTO dto) {
        virtualInventoryHistoryService.exportList(dto);
        return success(true);
    }
}
