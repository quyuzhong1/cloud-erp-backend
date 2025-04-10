package com.erp.server.oms.controller.api;


import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;
import com.common.core.controller.BaseController;
import com.erp.server.oms.service.CfgInvoiceSettingDetailService;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.CfgInvoiceSettingDetailDTO;

import java.util.List;

/**
 * 发票设置明细
 *
 * @author hcg
 * @since 2025-04-09
 */
@Slf4j
@RestController
@LogSystemModule("发票设置明细")
@RequestMapping("/cfgInvoiceSettingDetail")
public class CfgInvoiceSettingDetailController extends BaseController {

    @Resource
    private CfgInvoiceSettingDetailService cfgInvoiceSettingDetailService;

    /**
     * 发票明细新增或修改
     *
     * @param dto
     * @return ApiResult<String>
     * @author hcg
     * @date: 2025-04-09
     */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "发票设置新增")
    public ApiResult<BaseResultDTO.AddDTO> addOrUpdate(@RequestBody @Validated CfgInvoiceSettingDetailDTO.AddDTO dto) {
        return success(cfgInvoiceSettingDetailService.addOrUpdate(dto));
    }

    /**
     * 发票设置明细详情
     *
     * @param dto
     * @return
     */
    @PostMapping("/view")
    public ApiResult<List<CfgInvoiceSettingDetailDTO.ViewDTO>> view(@RequestBody @Validated CfgInvoiceSettingDetailDTO.ViewParamsDTO dto) {
        return success(cfgInvoiceSettingDetailService.view(dto));
    }

    /**
     * 店铺下拉
     * @description:
     * @author: hcg
     * @date: 2025/4/10 15:01
     * @param:
     * @return:
     **/
    @GetMapping("/listShopSelect")
    public ApiResult<List<CfgInvoiceSettingDetailDTO.ViewShopDTO>> listShopSelect(@RequestParam String dictplatform){
        return success(cfgInvoiceSettingDetailService.listShopSelect(dictplatform));
    }

}