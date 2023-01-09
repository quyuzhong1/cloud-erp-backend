package com.erp.server.bi.controller;

import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.model.bi.dto.BiSalesMonitoringDTO;
import com.erp.model.bi.dto.BiSalesMonitoringSearchDTO;
import com.erp.server.bi.service.BiSalesMonitoringService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 销售监控
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/29 16:23
 */
@RestController
@RequestMapping("bi/salesMonitoring")
public class BiSalesMonitoringController extends BaseController {

    @Resource
    private BiSalesMonitoringService biSalesMonitoringService;

    /**
     * 销售监控-批量新增销售监控
     * @author Will
     * @date: 2022/12/29 16:54
     * @param list
     * @return ApiResult
     */
    @PostMapping("/batchAdd")
    public ApiResult batchAdd (@RequestBody @Validated List<BiSalesMonitoringDTO> list) {
        Boolean flag = biSalesMonitoringService.batchAdd(list);
        return flag == true ? success() : failure();
    }

    /**
     * 销售监控-批量新增销售监控
     * @author Will
     * @date: 2022/12/29 16:54
     * @param list
     * @return ApiResult
     */
    @PostMapping("/batchUpdate")
    public ApiResult batchUpdate (@RequestBody @Validated List<BiSalesMonitoringDTO> list) {
        biSalesMonitoringService.batchUpdate(list);
        return success();
    }

    /**
     * 销售监控-查询所有销售监控数据
     * @author Will
     * @date: 2022/12/29 16:58
     * @return ApiResult<List<BiSalesMonitoringDTO>>
     */
    @GetMapping("/list")
    public ApiResult<List<BiSalesMonitoringDTO>> listBiSalesMonitoring() {
        List<BiSalesMonitoringDTO> list =  biSalesMonitoringService.listBiSalesMonitoring();
        return success(list);
    }


    /**
     * 销售监控-分析报表查询
     * @author Will
     * @date: 2022/12/30 12:27
     * @return ApiResult<BiSalesMonitoringViewVO>
     */
    @PostMapping("/view")
    public ApiResult<LinkedHashMap<String,Object>> listBiSalesMonitoringView(@RequestBody @Validated BiSalesMonitoringSearchDTO dto) {
        LinkedHashMap<String,Object> map =  biSalesMonitoringService.listBiSalesMonitoringView(dto);
        return success(map);
    }


}
