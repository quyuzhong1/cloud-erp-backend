package com.erp.server.wms.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.wms.dto.ReportOrderDemandDTO;
import com.erp.server.wms.service.ReportOrderDemandService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 
 *
 * @author will
 * @since 2024-09-23
 */
@Slf4j
@RestController
@LogSystemModule("")
@RequestMapping("/reportOrderDemand")
public class ReportOrderDemandController extends BaseController {

    @Resource
    private ReportOrderDemandService reportOrderDemandService;

    /**
     * 列表查询
     * @author will
     * @date 2024/9/23 17:27
     * @param dto
     * @return ApiResult<PagingVO<ListDTO>>
     */
    @PostMapping("/paging")
    @WebAdvanceQuery
    public ApiResult<PagingVO<ReportOrderDemandDTO.ListDTO>> queryByPage(@RequestBody @Validated PagingDTO<ReportOrderDemandDTO.PagingParamDTO> dto) {
        PagingVO<ReportOrderDemandDTO.ListDTO> pagingVO = reportOrderDemandService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 导出
     * @author will
     * @date 2024/9/23 17:29
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出订单需求报表")
    @PostMapping(value = "/exportExcel")
    public ApiResult exportExcel(@RequestBody ReportOrderDemandDTO.PagingParamDTO dto) {
        Boolean flag = reportOrderDemandService.exportExcel(dto);
        return flag == true ? success() : failure();
    }


}
