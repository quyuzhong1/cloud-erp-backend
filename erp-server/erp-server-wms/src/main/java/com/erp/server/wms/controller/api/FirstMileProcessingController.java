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
import com.erp.model.wms.dto.FirstMileProcessingDTO;
import com.erp.server.wms.query.FirstMileProcessingQueryHandler;
import com.erp.server.wms.service.FirstMileProcessingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 头程虚拟仓订单跟踪
 *
 * @author will
 * @since 2024-12-18
 */
@Slf4j
@RestController
@LogSystemModule("头程虚拟仓订单跟踪")
@RequestMapping("/firstMileProcessing")
public class FirstMileProcessingController extends BaseController {

    @Resource
    private FirstMileProcessingService firstMileProcessingService;

    /**
     * 分页查询
     * @author will
     * @date 2024/12/18 11:45
     * @param dto
     * @return ApiResult<PagingVO<ListDTO>>
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            warehouseTableField = "fmp.warehouse_id",
            menuCode = "wms:firstMileProcessing:paging"
    )
    @WebAdvanceQuery(handler = FirstMileProcessingQueryHandler.class)
    public ApiResult<PagingVO<FirstMileProcessingDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<FirstMileProcessingDTO.PagingParamDTO> dto) {
        return success(firstMileProcessingService.paging(dto));
    }

    /**
     * 导出
     * @author will
     * @date 2024/12/18 12:13
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/exportExcel")
    @LogAction(value = LogActionEnum.EXPORT, desc = "头程虚拟仓列表信息导出")
    public ApiResult exportExcel(@RequestBody FirstMileProcessingDTO.PagingParamDTO dto) {
        Boolean flag = firstMileProcessingService.exportExcel(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 删除订单跟踪数据
     * @author will
     * @date 2024/12/18 12:13
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/deleteFirstMileProcessing")
    @LogAction(value = LogActionEnum.DELETE, desc = "删除订单跟踪数据")
    public ApiResult deleteFirstMileProcessing(@RequestBody FirstMileProcessingDTO.DeleteDTO dto) {
        Boolean flag = firstMileProcessingService.deleteFirstMileProcessing(dto);
        return flag == true ? success() : failure();
    }
}
