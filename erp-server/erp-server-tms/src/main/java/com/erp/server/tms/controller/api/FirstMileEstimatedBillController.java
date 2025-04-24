package com.erp.server.tms.controller.api;

import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.utils.ExcelUtil;
import com.erp.model.tms.dto.FirstMileEstimatedBillDTO;
import com.erp.server.tms.query.FirstMileEstimatedQueryHandler;
import com.erp.server.tms.service.FirstMileEstimatedBillService;
import org.checkerframework.checker.units.qual.A;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * 头程暂估账单
 * @date 2024-08-16
 * @author tanmujin
 */
@RestController
@RequestMapping("/firstMileEstimatedBill")
public class FirstMileEstimatedBillController extends BaseController {

    @Resource
    private FirstMileEstimatedBillService firstMileEstimatedBillService;

    /**
     * 高级查询
     */
    @PostMapping("/paging")
    @WebAdvanceQuery(handler = FirstMileEstimatedQueryHandler.class)
    public ApiResult<PagingVO<FirstMileEstimatedBillDTO.View>> paging(@RequestBody PagingDTO<FirstMileEstimatedBillDTO.PagingParam> dto){
        PagingVO<FirstMileEstimatedBillDTO.View> pagingVO = firstMileEstimatedBillService.paging(dto);
        return ApiResult.success(pagingVO);
    }

    /**
     * 修改状态
     */
    @PostMapping("/updateStatus")
    public ApiResult<List<BatchResultDTO>> updateStatus(@RequestBody FirstMileEstimatedBillDTO.UpdateStatus dto){
        List<BatchResultDTO> list = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO batchResultDTO = firstMileEstimatedBillService.updateStatus(id, dto.getType());
            list.add(batchResultDTO);
        }
        return list.stream().allMatch(BatchResultDTO::getSuccess) ? success(list) : failure(list);
    }

    /**
     * 统计tab数量
     */
    @GetMapping("/tabList")
    public ApiResult<List<FirstMileEstimatedBillDTO.Tab>> tabList(){
        List<FirstMileEstimatedBillDTO.Tab> list = firstMileEstimatedBillService.tabList();
        return ApiResult.success(list);
    }

    /**
     * 导入Excel
     */
    @PostMapping("/importExcel")
    public ApiResult<Object> importExcel(@RequestParam MultipartFile excelFile, HttpServletResponse response){
        firstMileEstimatedBillService.importExcel(excelFile, response);
        return success();
    }

    /**
     * 导出Excel
     */
    @PostMapping("/exportExcel")
    @WebAdvanceQuery(handler = FirstMileEstimatedQueryHandler.class)
    public ApiResult<Object> exportExcel(@RequestBody FirstMileEstimatedBillDTO.ExportParam dto){
        firstMileEstimatedBillService.exportExcel(dto);
        return ApiResult.success();
    }

    /**
     * 下载模板
     */
    @GetMapping("/downloadTemplate")
    public ApiResult<Object> downloadTemplate(HttpServletResponse response){
        String path = "classpath:excel/firstMileEstimatedBillTemplate.xlsx";
        String excelName = "firstMileEstimatedTemplate.xlsx";
        ExcelUtil.downloadTemplate(path, excelName, response);
        return ApiResult.success();
    }
}
