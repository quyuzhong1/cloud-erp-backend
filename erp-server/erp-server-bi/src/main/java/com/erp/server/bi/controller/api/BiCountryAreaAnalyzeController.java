package com.erp.server.bi.controller.api;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.bi.dto.BiCountryAnalyzeDTO;
import com.erp.model.bi.dto.BiCountryRegionFilterDTO;
import com.erp.model.bi.dto.BiRegionAnalyzeDTO;
import com.erp.server.bi.service.BiComprehensiveAnalyseService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
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
        List<BiRegionAnalyzeDTO> vo = biComprehensiveAnalyseService.getRegionSales(dto);
        return success(vo);
    }

    /**
     * 国家销售额分析
     */
    @PostMapping("/countrySales")
    public ApiResult<List<BiCountryAnalyzeDTO>> getCountrySales(@RequestBody @Valid BiCountryRegionFilterDTO dto) {
        List<BiCountryAnalyzeDTO> vo = biComprehensiveAnalyseService.getCountrySales(dto);
        return success(vo);
    }


}