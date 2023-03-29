package com.erp.server.scm.controller;


import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.UpdateStateDTO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.scm.dto.PurchasePriceDetailDTO;
import com.erp.server.scm.service.PurchasePriceDetailService;
import com.erp.server.scm.service.PurchasePriceHistoryService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

/**
 * 采购价目管理
 *
 * @author admin
 * @since 2023-03-15
 */
@RestController
@RequestMapping("/purchase/price/detail")
public class PurchasePriceDetailController extends BaseController {

    @Resource
    private PurchasePriceDetailService purchasePriceDetailService;

    @Resource
    private PurchasePriceHistoryService purchasePriceHistoryService;


    /**
     * 下载模板
     *
     * @return
     */
    @GetMapping("/downloadTemplate")
    public ApiResult downloadTemplate(HttpServletResponse response) {
        purchasePriceDetailService.downloadTemplate(response);
        return success();
    }


    /**
     * 导入数据
     *
     * @return
     */
    @PostMapping("/importFile")
    public ApiResult<PurchasePriceDetailDTO.ImportDTO> importFile(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
        PurchasePriceDetailDTO.ImportDTO result = purchasePriceDetailService.importFile(excelFile);
        return success(result);
    }


    /**
     * 批量启用或者禁用 采购价目状态
     */
    @PostMapping("/updateDisabled")
    public ApiResult updateDisabled(@RequestBody @Valid UpdateStateDTO.BatchUpdateDTO dto) {
        Boolean result = purchasePriceDetailService.updateDisabled(dto);
        return result == true ? success() : failure();
    }

    /**
     * 获取历史数据
     */
    @PostMapping("/history")
    public ApiResult<List<PurchasePriceDetailDTO.HistoryDTO>> getHistory(@RequestBody @Valid BaseIdDTO dto) {
        List<PurchasePriceDetailDTO.HistoryDTO> historyList = purchasePriceHistoryService.getHistory(dto.getId());
        return success(historyList);
    }


}
