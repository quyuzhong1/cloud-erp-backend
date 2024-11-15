package com.erp.server.bi.controller.api;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.bi.dto.BiCountryAnalyzeDTO;
import com.erp.model.bi.dto.BiCountryRegionFilterDTO;
import com.erp.model.bi.dto.BiRegionAnalyzeDTO;
import com.erp.server.bi.service.BiComprehensiveAnalyseService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

/**
 * 国家/区域分析
 *
 * @Author Jim
 * @Date 2023/09/18
 **/
@RestController
@RequestMapping("global")
public class BiCountryAreaAnalyzeController extends BaseController {

    @Resource
    private BiComprehensiveAnalyseService biComprehensiveAnalyseService;


    /**
     * 区域销售额分析
     */
    @PostMapping("/regionSales")
    public ApiResult<List<BiRegionAnalyzeDTO>> getRegionSales(@RequestBody @Valid BiCountryRegionFilterDTO dto) {
        // 设置结束时间+1
        dto.setEndTime(dto.getEndTime(), 1);
        List<BiRegionAnalyzeDTO> vo = biComprehensiveAnalyseService.getSubRegionSales(dto);
        return success(vo);
    }


    /**
     * 国家销售额分析
     */
    @PostMapping("/countrySales")
    public ApiResult<List<BiCountryAnalyzeDTO>> getCountrySales(@RequestBody @Valid BiCountryRegionFilterDTO dto) {
        // 设置结束时间+1
        dto.setEndTime(dto.getEndTime(), 1);
        List<BiCountryAnalyzeDTO> vo = biComprehensiveAnalyseService.getCountrySales(dto);
        return success(vo);
    }

    /**
     * 导出区域/国家销售额
     */
    @PostMapping("/exportCountrySales")
    public ApiResult<Void> exportCountrySales(@RequestBody @Valid BiCountryRegionFilterDTO dto, HttpServletResponse response) {
        boolean result = biComprehensiveAnalyseService.exportCountryExcel(dto, response);
        return result ? success() : failure();
    }

}