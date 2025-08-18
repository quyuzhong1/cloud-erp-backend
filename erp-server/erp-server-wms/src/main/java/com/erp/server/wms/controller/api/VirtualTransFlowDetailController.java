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
import com.erp.model.wms.dto.VirtualTransFlowDetailDTO;
import com.erp.server.wms.service.VirtualTransFlowDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 虚拟仓库存流水明细
 *
 * @author will
 * @since 2024-12-03
 */
@Slf4j
@RestController
@LogSystemModule("虚拟仓库存流水明细")
@RequestMapping("/virtualTransFlowDetail")
public class VirtualTransFlowDetailController extends BaseController {

    @Resource
    private VirtualTransFlowDetailService virtualTransFlowDetailService;


    /**
     * 分页查询
     * @author will
     * @date 2024/12/5 10:54
     * @param dto
     * @return ApiResult<PagingVO<ListDTO>>
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            warehouseTableField = "vm.warehouseId",
            menuCode = "wms:virtualTransFlowDetail:paging"
    )
    @WebAdvanceQuery
    public ApiResult<PagingVO<VirtualTransFlowDetailDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<VirtualTransFlowDetailDTO.SearchParamDTO> dto) {
        return success(virtualTransFlowDetailService.paging(dto));
    }

    /**
     * 导出
     * @author will
     * @date 2024/12/5 11:00
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/exportExcel")
    @WebAdvanceQuery
    @LogAction(value = LogActionEnum.EXPORT, desc = "库龄流水导出")
    public ApiResult exportExcel(@RequestBody VirtualTransFlowDetailDTO.SearchParamDTO dto) {
        Boolean flag = virtualTransFlowDetailService.exportExcel(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 处理历史数据（添加任务）
     * @author will
     * @date 2024/12/17 17:34
     * @param dto 
     * @return ApiResult
     */
    @PostMapping("/handleHisVirtualTransFlowDetail")
    public ApiResult handleHisVirtualTransFlowDetail(@RequestBody VirtualTransFlowDetailDTO.HandleDTO dto) {
        virtualTransFlowDetailService.handleHisVirtualTransFlowDetail(dto);
        return success() ;
    }

}
