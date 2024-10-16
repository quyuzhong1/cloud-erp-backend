package com.erp.server.wms.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.PagingDTO;
import com.common.business.validator.ValidList;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.wms.dto.ReportOrderDemandDTO;
import com.erp.server.wms.service.ReportOrderDataService;
import com.erp.server.wms.service.ReportOrderDemandService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 
 * 缺货统计
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

    @Resource
    private ReportOrderDataService reportOrderDataService;

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
    @WebAdvanceQuery
    public ApiResult exportExcel(@RequestBody ReportOrderDemandDTO.PagingParamDTO dto) {
        Boolean flag = reportOrderDemandService.exportExcel(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 单个分货查看
     * @author will
     * @date 2024/9/25 10:48
     * @param dto
     * @return ApiResult<ViewAllocationDTO>
     */
    @PostMapping(value = "/viewAllocation")
    public ApiResult<ReportOrderDemandDTO.ViewVirtualAllocationDTO> viewAllocation(@RequestBody @Validated ReportOrderDemandDTO.ViewVirtualAllocationParamDTO dto) {
        ReportOrderDemandDTO.ViewVirtualAllocationDTO viewVirtualAllocationDTO = reportOrderDemandService.viewAllocation(dto);
        return success(viewVirtualAllocationDTO);
    }

    /**
     * 单个分货保存
     * @author will
     * @date 2024/9/25 11:02
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.UPDATE, desc = "单个分货保存")
    @PostMapping(value = "/addAllocation")
    public ApiResult addAllocation(@RequestBody @Validated ReportOrderDemandDTO.AddVirtualAllocationDTO dto) {
        Boolean flag = reportOrderDemandService.addAllocation(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 批量分货查看
     * @author will
     * @date 2024/9/25 11:23
     * @param list
     * @return ApiResult<BatchViewVirtualAllocationDTO>
     */
    @PostMapping(value = "/batchViewAllocation")
    public ApiResult<List<ReportOrderDemandDTO.BatchViewVirtualAllocationDTO>> batchViewAllocation(@RequestBody @Validated ValidList<ReportOrderDemandDTO.ViewVirtualAllocationParamDTO> list) {
        List<ReportOrderDemandDTO.BatchViewVirtualAllocationDTO> resultList = reportOrderDemandService.batchViewAllocation(list);
        return success(resultList);
    }

    /**
     * 批量分货保存
     * @author will
     * @date 2024/9/25 11:02
     * @param list
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.UPDATE, desc = "批量分货保存")
    @PostMapping(value = "/batchAddAllocation")
    public ApiResult batchAddAllocation(@RequestBody @Validated ValidList<ReportOrderDemandDTO.BatchAddVirtualAllocationDTO> list) {
        Boolean flag = reportOrderDemandService.batchAddAllocation(list);
        return flag == true ? success() : failure();
    }


    /**
     * 更新虚拟仓报表数据
     * @author will
     * @date 2024/10/12 11:59
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.UPDATE, desc = "生成虚拟仓报表")
    @PostMapping(value = "/generateVirtualReport")
    public ApiResult generateVirtualReport() {
        reportOrderDataService.generateVirtualReport("",Boolean.FALSE);
        return success();
    }
}
