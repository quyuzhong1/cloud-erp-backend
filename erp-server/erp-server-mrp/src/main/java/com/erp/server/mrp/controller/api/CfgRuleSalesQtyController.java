package com.erp.server.mrp.controller.api;


import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.mrp.dto.CfgRuleSalesEstimateFileDTO;
import com.erp.model.mrp.dto.CfgRuleSalesQtyDTO;
import com.erp.server.mrp.service.CfgRuleSalesEstimateFileService;
import com.erp.server.mrp.service.CfgRuleSalesQtyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

/**
 * 销量（规则设置）
 *
 * @author will
 * @since 2024-08-23
 */
@Slf4j
@RestController
@LogSystemModule("销量（规则设置）")
@RequestMapping("/cfgRuleSalesQty")
public class CfgRuleSalesQtyController extends BaseController {

    @Resource
    private CfgRuleSalesEstimateFileService cfgRuleSalesEstimateFileService;
    @Resource
    private CfgRuleSalesQtyService cfgRuleSalesQtyService;

    /**
    * 修改
    * @author will
    * @date:  2024-08-23
    * @param updateDTO
    * @return ApiResult
    */
    @PostMapping("/batchUpdate")
    @LogAction(value = LogActionEnum.CUSTOM_BATCH_UPDATE, desc = "销量规则批量修改")
    public ApiResult<String> batchUpdate(@RequestBody @Validated CfgRuleSalesQtyDTO.UpdateDTO updateDTO) {
        cfgRuleSalesQtyService.batchUpdate(updateDTO);
        return success();
    }

    /**
     * 查看详情
     * @author will
     * @date 2024/8/24 9:19
     * @param platform
     * @return ApiResult<ViewDTO>
     */
    @GetMapping("/view")
    @LogViewService
    public ApiResult<CfgRuleSalesQtyDTO.ViewDTO> view(@RequestParam("platform") String platform) {
        return success(cfgRuleSalesQtyService.view(platform));
    }

    /**
     * 补货建议查询销量详情
     * @author will
     * @date 2024/9/6 12:21
     * @param refId
     * @return ApiResult<ViewDetailDTO>
     */
    @GetMapping("/viewDetail")
    @LogViewService
    public ApiResult<CfgRuleSalesQtyDTO.ViewDetailDTO> refView(@RequestParam("refId") String refId) {
        return success(cfgRuleSalesQtyService.viewDetail(refId));
    }

    /**
     * 导入模板
     * @param response 参数
     * @return ApiResult<?>
     */
    @GetMapping("/downloadRuleTemplate")
    public ApiResult<String> downloadRuleTemplate(HttpServletResponse response) {
        cfgRuleSalesEstimateFileService.downloadRuleTemplate(response);
        return success();
    }


    /**
     * 文件列表
     */
    @PostMapping("/filePage")
    public ApiResult<PagingVO<CfgRuleSalesEstimateFileDTO.PagingView>> filePage(@RequestBody @Validated PagingDTO<CfgRuleSalesEstimateFileDTO.PagingParamDTO> params) {
        PagingVO<CfgRuleSalesEstimateFileDTO.PagingView> pagingVO = cfgRuleSalesEstimateFileService.filePage(params);
        return success(pagingVO);
    }


    /**
     * 导入
     */
    @PostMapping("/importFile")
    @LogAction(value = LogActionEnum.IMPORT, desc = "预估日销量导入")
    public ApiResult<String> importFile(@RequestParam(value = "excelFile") MultipartFile excelFile, @RequestParam(value = "platform") String platform, HttpServletResponse response) {
        cfgRuleSalesEstimateFileService.importFile(excelFile, platform, response);
        return success("上传成功");
    }
}
