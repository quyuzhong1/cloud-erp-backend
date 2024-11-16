package com.erp.server.bi.controller.api;

import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
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

 * @date 2022/12/29 16:23
 */
@RestController
@LogSystemModule("模块管理")
@RequestMapping("salesMonitoring")
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
    @LogAction(value = LogActionEnum.CUSTOM_BATCH_INSERT, desc = "批量新增销售监控:监控维度={type}")
    @PostMapping("/batchAdd")
    public ApiResult<Object> batchAdd (@RequestBody @Validated List<BiSalesMonitoringDTO> list) {
        boolean flag = biSalesMonitoringService.batchAdd(list);
        return flag ? success() : failure();
    }

    /**
     * 销售监控-批量修改销售监控
     * @author Will
     * @date: 2022/12/29 16:54
     * @param list
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.CUSTOM_BATCH_UPDATE, desc = "批量修改销售监控：监控维度={type}")
    @PostMapping("/batchUpdate")
    public ApiResult<Void> batchUpdate (@RequestBody @Validated List<BiSalesMonitoringDTO> list) {
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
    @DataPermission(operationType = DataAttributeEnum.LIST,
                    tableField = "charge_id",
                    menuCode = "bi:module:content",
                    tableAlias = "doio")
    public ApiResult<LinkedHashMap<String,Object>> listBiSalesMonitoringView(@RequestBody @Validated BiSalesMonitoringSearchDTO.ParamDTO dto) {
        LinkedHashMap<String,Object> map =  biSalesMonitoringService.listBiSalesMonitoringView(dto);
        return success(map);
    }


}
