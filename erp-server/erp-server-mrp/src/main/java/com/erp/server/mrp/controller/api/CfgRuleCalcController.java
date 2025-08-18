package com.erp.server.mrp.controller.api;


import com.common.business.dto.base.BatchParamsDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.mrp.dto.CalcSalesInfoFavoriteDTO;
import com.erp.model.mrp.dto.CfgRuleCalcDTO;
import com.erp.server.mrp.service.CfgRuleCalcService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 试算配置
 *
 * @author liaohui
 * @since 2024-11-11
 */
@Slf4j
@RestController
@LogSystemModule("试算配置")
@RequestMapping("/cfgRuleCalc")
public class CfgRuleCalcController extends BaseController {

    @Resource
    private CfgRuleCalcService cfgRuleCalcService;

    /**
     * tab
     * @param dto 参数
     */
    @PostMapping("/tabList")
    public ApiResult<List<CfgRuleCalcDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
        List<CfgRuleCalcDTO.TabListDTO> tabList = cfgRuleCalcService.tabList(dto);
        return success(tabList);
    }

    /**
    * 新增
    * @author liaohui
    * 2024-11-11
    * @param dto 参数
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "新增试算配置")
    public ApiResult<BatchResultDTO> add(@RequestBody @Validated CfgRuleCalcDTO.AddDTO dto) {
        return success(cfgRuleCalcService.add(dto));
    }



    /**
     * 详情
     * @author liaohui
     * 2024-11-11
     * @param id 参数
     * @return ApiResult<String>
     */
    @GetMapping("/view")
    public ApiResult<CfgRuleCalcDTO.ViewDTO> view(@RequestParam String id) {
        return success(cfgRuleCalcService.view(id));
    }

    /**
     * 下载历史销量
     * @param dto 参数
     */
    @LogAction(value = LogActionEnum.DOWNLOAD, desc = "下载历史销量")
    @PostMapping("/downloadHistorySales")
    public ApiResult<String> downloadHistorySales(@RequestBody @Validated CfgRuleCalcDTO.DownloadDTO dto) {
        cfgRuleCalcService.downloadHistorySales(dto);
        return success();
    }

    /**
     * 导入模板
     * @param response 参数
     * @return ApiResult<?>
     */
    @GetMapping("/downloadRuleTemplate")
    public ApiResult<?> downloadRuleTemplate(HttpServletResponse response) {
        cfgRuleCalcService.downloadRuleTemplate(response);
        return success();
    }


    /**
     * 新增关注模板
     * @param dto 参数
     */
    @PostMapping("/addFavorite")
    @LogAction(value = LogActionEnum.INSERT, desc = "新增关注模板 试算模板id={cfgRuleCalcId}")
    public ApiResult<String> addFavorite(@RequestBody @Validated CalcSalesInfoFavoriteDTO.AddDTO dto) {
        cfgRuleCalcService.addFavorite(dto);
        return success();
    }

    /**
     * 取消关注模板
     * @param dto 参数
     */
    @PostMapping("/cancelFavorite")
    @LogAction(value = LogActionEnum.DELETE, desc = "取消关注模板 试算模板id={cfgRuleCalcId}")
    public ApiResult<String> cancelFavorite(@RequestBody @Validated CalcSalesInfoFavoriteDTO.CancelDTO dto) {
        cfgRuleCalcService.cancelFavorite(dto);
        return success();
    }

    /**
     * 根据sku查店铺
     * @param dto 参数
     */
    @PostMapping("/hasSalesShopBySku")
    public ApiResult<List<String>> hasSalesShopBySku(@RequestBody @Validated BatchParamsDTO<String> dto) {
        List<String> shopIds = cfgRuleCalcService.hasSalesShopBySku(dto.getParams());
        return success(shopIds);
    }

    /**
     * 根据sku查店铺
     * @param dto 参数
     */
    @PostMapping("/hasSalesSkuByShop")
    public ApiResult<List<CfgRuleCalcDTO.SkuDTO>> hasSalesSkuByShop(@RequestBody @Validated BatchParamsDTO<String> dto) {
        List<CfgRuleCalcDTO.SkuDTO> dtos = cfgRuleCalcService.hasSalesSkuByShop(dto.getParams());
        return success(dtos);
    }
}
