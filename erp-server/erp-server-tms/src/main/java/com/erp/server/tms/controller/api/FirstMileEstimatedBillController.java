package com.erp.server.tms.controller.api;

import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.dto.FirstMileEstimatedBillDTO;
import com.erp.server.tms.query.FirstMileEstimatedQueryHandler;
import org.checkerframework.checker.units.qual.A;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/**
 * 头程暂估账单
 * @date 2024-08-16
 * @author tanmujin
 */
@RestController
@RequestMapping("/firstMileEstimatedBill")
public class FirstMileEstimatedBillController {

    /**
     * 高级查询
     */
    @PostMapping("/paging")
    @WebAdvanceQuery(handler = FirstMileEstimatedQueryHandler.class)
    public ApiResult<PagingVO<FirstMileEstimatedBillDTO.View>> paging(@RequestBody PagingDTO<FirstMileEstimatedBillDTO.PagingParam> dto){

        return ApiResult.success();
    }

    /**
     * 修改状态
     */
    @PostMapping("/updateStatus")
    public ApiResult<BatchResultDTO> updateStatus(@RequestBody FirstMileEstimatedBillDTO.UpdateStatus dto){

        return ApiResult.success();
    }

    /**
     * 统计tab数量
     */
    @GetMapping("/tabList")
    public ApiResult<FirstMileEstimatedBillDTO.TabList> tabList(){

        return ApiResult.success();
    }

    /**
     * 导入Excel
     */
    @PostMapping("/importExcel")
    public ApiResult<?> importExcel(@RequestParam MultipartFile excelFile, HttpServletResponse response){

        return ApiResult.success();
    }

    /**
     * 导出Excel
     */
    @PostMapping("/exportExcel")
    public ApiResult<?> exportExcel(@RequestBody FirstMileEstimatedBillDTO.ExportParam dto){

        return ApiResult.success();
    }

    /**
     * 下载模板
     */
    @GetMapping("/downloadTemplate")
    public ApiResult<?> downloadTemplate(){

        return ApiResult.success();
    }
}
