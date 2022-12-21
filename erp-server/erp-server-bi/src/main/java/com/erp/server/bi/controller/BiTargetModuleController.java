package com.erp.server.bi.controller;

import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.model.bi.dto.BiFilterDTO;
import com.erp.model.bi.vo.QuarterMonthSalesVO;
import com.erp.model.bi.vo.QuarterMonthSalesVolumeVO;
import com.erp.model.bi.vo.SalesCompletionInfoVO;
import com.erp.model.bi.vo.TargetAnalysisVO;
import com.erp.server.bi.service.DmpOrderInfoService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * BI目标一级模块
 *
 * @Author Cloud
 * @Date 2022/12/20 10:32
 **/
@RestController
@RequestMapping("bi/target/")
public class BiTargetModuleController extends BaseController {

    @Resource
    private DmpOrderInfoService dmpOrderInfoService;
    /**
     * 季度销售额完成情况
     */
    @PostMapping("/quarter/sales")
    public ApiResult quarterSales(@RequestBody @Validated(BiFilterDTO.SelectTargetModule.class) BiFilterDTO dto) {
        TargetAnalysisVO<QuarterMonthSalesVO> vo = dmpOrderInfoService.sumQuarterSales(dto);
        return success(vo);
    }


    /**
     * 季度销量完成情况
     */
    @PostMapping("/quarter/sales/volume")
    public ApiResult quarterSalesVolume(@RequestBody @Validated(BiFilterDTO.SelectTargetModule.class) BiFilterDTO dto) {
        TargetAnalysisVO<QuarterMonthSalesVolumeVO> vo = dmpOrderInfoService.sumQuarterSalesVolume(dto);
        return success(vo);
    }

    /**
     * 月度销售额完成情况
     */
    @PostMapping("/month/sales")
    public ApiResult monthSales(@RequestBody @Validated(BiFilterDTO.SelectTargetModule.class) BiFilterDTO dto) {
        TargetAnalysisVO<QuarterMonthSalesVO> vo = dmpOrderInfoService.sumMonthSales(dto);
        return success(vo);
    }

    /**
     * 月度销售额完成情况
     */
    @PostMapping("/month/sales/volume")
    public ApiResult monthSalesVolume(@RequestBody @Validated(BiFilterDTO.SelectTargetModule.class) BiFilterDTO dto) {
        TargetAnalysisVO<QuarterMonthSalesVolumeVO> vo = dmpOrderInfoService.sumMonthSalesVolume(dto);
        return success(vo);
    }

    /**
     *
     * 事业部销售额/销量完成情况
     */


    /**
     * 平台销售额/销量完成情况
     */
    @PostMapping("/platform/sales")
    public ApiResult platformSalesCompletion(@RequestBody @Validated(BiFilterDTO.SelectTargetModule.class) BiFilterDTO dto) {
        List<SalesCompletionInfoVO> vo = dmpOrderInfoService.sumPlatformSalesCompletion(dto);
        return success(vo);
    }
    /**
     * 站点销售额/销量完成情况
     */

    /**
     * 店铺销售额/销量完成情况
     */



    /**
     * 品类销售额/销量完成情况
     */
    @PostMapping("/category/sales")
    public ApiResult categorySalesCompletion(@RequestBody @Validated(BiFilterDTO.SelectTargetModule.class) BiFilterDTO dto) {
        List<SalesCompletionInfoVO> vo = dmpOrderInfoService.sumCategorySalesCompletion(dto);
        return success(vo);
    }

    /**
     * 新品销售额/销量完成情况
     */

    /**
     * 老品销售额/销量完成情况
     */


    /**
     * 产品定位销售额/销量完成情况
     */


    /**
     * SKU销售额/销量完成情况
     */

    /**
     * 人员达成
     */

}
