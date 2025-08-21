package com.erp.server.wms.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.ReportProcessingDTO;
import com.erp.server.wms.service.FirstMileProcessingService;
import com.erp.server.wms.service.SoB2bProcessingService;
import com.erp.server.wms.service.SoB2cProcessingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 虚拟仓冻结订单汇总
 *
 * @author will
 * @since 2024-12-18
 */
@Slf4j
@RestController
@LogSystemModule("虚拟仓冻结订单汇总")
@RequestMapping("/reportProcessing")
public class ReportProcessingController extends BaseController {

    @Resource
    private SoB2cProcessingService soB2cProcessingService;

    @Resource
    private SoB2bProcessingService soB2bProcessingService;

    @Resource
    private FirstMileProcessingService firstMileProcessingService;

    /**
     * b2c分页查询
     * @author will
     * @date 2025/08/20 11:30
     * @param dto
     * @return ApiResult<PagingVO<ListDTO>>
     */
    @PostMapping("/b2cTotalPaging")
    @WebAdvanceQuery
    public ApiResult<PagingVO<ReportProcessingDTO.ListDTO>> b2cTotalPaging(@RequestBody @Validated PagingDTO<ReportProcessingDTO.PagingParamDTO> dto) {
        return success(soB2cProcessingService.b2cTotalPaging(dto));
    }

    /**
     * b2b分页查询
     * @author will
     * @date 2025/08/20 11:30
     * @param dto
     * @return ApiResult<PagingVO<ListDTO>>
     */
    @PostMapping("/b2bTotalPaging")
    @WebAdvanceQuery
    public ApiResult<PagingVO<ReportProcessingDTO.ListDTO>> b2bTotalPaging(@RequestBody @Validated PagingDTO<ReportProcessingDTO.PagingParamDTO> dto) {
        return success(soB2bProcessingService.b2bTotalPaging(dto));
    }

    /**
     * 头程分页查询
     * @author will
     * @date 2025/08/20 11:30
     * @param dto
     * @return ApiResult<PagingVO<ListDTO>>
     */
    @PostMapping("/firstMileTotalPaging")
    @WebAdvanceQuery
    public ApiResult<PagingVO<ReportProcessingDTO.ListDTO>> firstMileTotalPaging(@RequestBody @Validated PagingDTO<ReportProcessingDTO.PagingParamDTO> dto) {
        return success(firstMileProcessingService.firstMileTotalPaging(dto));
    }
}
