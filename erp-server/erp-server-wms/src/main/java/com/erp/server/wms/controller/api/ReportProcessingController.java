package com.erp.server.wms.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.wms.dto.ReportProcessingDTO;
import com.erp.server.wms.query.VirtualInventoryAgeQueryHandler;
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
     * b2c汇总导出
     * @author will
     * @date 2025/8/21 14:02
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/b2cTotalExportExcel")
    @LogAction(value = LogActionEnum.EXPORT, desc = "b2c冻结订单汇总导出")
    @WebAdvanceQuery(handler = VirtualInventoryAgeQueryHandler.class)
    public ApiResult b2cTotalExportExcel(@RequestBody ReportProcessingDTO.PagingParamDTO dto) {
        Boolean flag = soB2cProcessingService.b2cTotalExportExcel(dto);
        return flag == true ? success() : failure();
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
     * b2c汇总导出
     * @author will
     * @date 2025/8/21 14:02
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/b2bTotalExportExcel")
    @LogAction(value = LogActionEnum.EXPORT, desc = "b2b冻结订单汇总导出")
    @WebAdvanceQuery(handler = VirtualInventoryAgeQueryHandler.class)
    public ApiResult b2bTotalExportExcel(@RequestBody ReportProcessingDTO.PagingParamDTO dto) {
        Boolean flag = soB2bProcessingService.b2bTotalExportExcel(dto);
        return flag == true ? success() : failure();
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


    /**
     * 头程汇总导出
     * @author will
     * @date 2025/8/21 14:02
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/firstMileTotalExportExcel")
    @LogAction(value = LogActionEnum.EXPORT, desc = "头程冻结订单汇总导出")
    @WebAdvanceQuery(handler = VirtualInventoryAgeQueryHandler.class)
    public ApiResult firstMileTotalExportExcel(@RequestBody ReportProcessingDTO.PagingParamDTO dto) {
        Boolean flag = firstMileProcessingService.firstMileTotalExportExcel(dto);
        return flag == true ? success() : failure();
    }
}
