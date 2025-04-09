package com.erp.server.oms.controller.api;



import com.common.business.dto.base.BaseResultDTO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.oms.dto.CfgInvoiceSettingDetailDTO;
import com.erp.server.oms.service.CfgInvoiceSettingDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.controller.BaseController;
import javax.annotation.Resource;
import java.util.List;

/**
 *
 * nf-e发票设置
 *
 *
 * @author Lambda
 * @since 2025-04-07
 */
@Slf4j
@RestController
@LogSystemModule("发票设置明细详情")
@RequestMapping("/cfgInvoiceSettingDetail")
public class CfgInvoiceSettingDetailController extends BaseController {
    @Resource
    private CfgInvoiceSettingDetailService cfgInvoiceSettingDetailService;

    /**
     * 发票设置明细详情
     * @param key
     * @param names
     * @return
     */
    @PostMapping("/view/{mainId}")
    public ApiResult<List<CfgInvoiceSettingDetailDTO.ViewDTO>> view(@PathVariable("mainId") String mainId,@RequestParam("key") String key
            ,@RequestParam(value = "names", required = false) List<String> names) {
        return success(cfgInvoiceSettingDetailService.view(mainId,key,names));
    }

    /**
     * 发票设置明细新增
     * @param dto
     * @return
     */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "发票设置新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated CfgInvoiceSettingDetailDTO.AddDTO dto) {
        return success(cfgInvoiceSettingDetailService.add(dto));
    }
}
