package com.erp.server.oms.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.vo.PagingVO;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import javax.validation.Valid;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;
import com.common.core.controller.BaseController;
import com.erp.server.oms.service.CfgInvoiceSettingService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.oms.dto.CfgInvoiceSettingDTO;

import java.util.Map;

/**
 * 发票设置
 * @author hcg
 * @since 2025-04-09
 */
@Slf4j
@RestController
@LogSystemModule("")
@RequestMapping("/cfgInvoiceSetting")
public class CfgInvoiceSettingController extends BaseController {
    @Resource
    private CfgInvoiceSettingService cfgInvoiceSettingService;

    /**
     * 发票设置分页查询
     *
     * @description:
     * @author: hcg
     * @date: 2025/4/9 14:43
     * @param: dto
     * @return: PagingVO<CfgInvoiceSettingDTO.PagingViewDTO>
     **/
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "oms:cfgInvoiceSetting:paging",
            tableAlias = "cis"
    )
    @WebAdvanceQuery
    @PostMapping("/paging")
    public ApiResult<PagingVO<CfgInvoiceSettingDTO.PagingViewDTO>> queryByPage(@RequestBody @Validated PagingDTO<CfgInvoiceSettingDTO.PagingParamDTO> dto) {
        PagingVO<CfgInvoiceSettingDTO.PagingViewDTO> pagingVO = cfgInvoiceSettingService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 发票设置新增
     *
     * @author: hcg
     * @date: 2025/4/9 14:43
     * @param: dto
     * @return: BaseResultDTO.AddDTO
     **/
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "发票设置新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated CfgInvoiceSettingDTO.AddDTO dto) {
        return success(cfgInvoiceSettingService.add(dto));
    }

    /**
     * 发票设置修改
     *
     * @description:
     * @author: hcg
     * @date: 2025/4/9 14:42
     * @param: dto
     * @return:
     **/
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "发票设置修改")
    public ApiResult<?> update(@RequestBody @Validated CfgInvoiceSettingDTO.UpdateDTO dto) {
        cfgInvoiceSettingService.update(dto);
        return success();
    }

    /**
     * 发票设置删除
     *
     * @author: hcg
     * @date: 2025/4/9 14:42
     * @param: idsDTO
     * @return: Boolean
     **/
    @PostMapping("/delete")
    @LogAction(value = LogActionEnum.UPDATE, desc = "发票设置删除")
    public ApiResult<Boolean> delete(@RequestBody @Valid BaseIdsDTO.IdsDTO idsDTO) {
        return success(cfgInvoiceSettingService.delete(idsDTO.getIds()));
    }

    /**
     * 发票设置详情
     *
     * @description:
     * @author: hcg
     * @date: 2025/4/9 14:40
     * @param: BaseIdDTO
     * @return: CfgInvoiceSettingDTO.ViewDTO
     **/
    @GetMapping("/view")
    public ApiResult<CfgInvoiceSettingDTO.ViewDTO> view(@RequestParam(value = "settingId") String settingId) {
        return success(cfgInvoiceSettingService.view(settingId));
    }

    /**
     * 发票设置启用或禁用
     *
     * @description:
     * @author: hcg
     * @date: 2025/4/9 14:41
     * @param: dto
     * @return:
     **/
    @PostMapping("/updateStatus")
    @LogAction(value = LogActionEnum.UPDATE, desc = "启用或禁用")
    public ApiResult<?> updateStatus(@RequestBody @Validated CfgInvoiceSettingDTO.UpdateStatusDTO dto) {
        cfgInvoiceSettingService.updateStatus(dto);
        return success();
    }
    /**
     * 序列号修改
     *
     * @description:
     * @author: zdy
     * @date: 2025/5/23 14:41
     * @param: dto
     * @return:
     **/
    @PostMapping("/updateSerialNo")
    @LogAction(value = LogActionEnum.UPDATE, desc = "序列号修改")
    public ApiResult<?> updateSerialNo(@RequestBody @Validated CfgInvoiceSettingDTO.UpdateSerialDTO dto) {
        cfgInvoiceSettingService.updateSerialNo(dto);
        return success();
    }

    /**
     * 初始化公司列表
     * 从第三方系统获取公司列表并初始化到cfg_invoice_setting表
     *
     * @author: system
     * @date: 2025/01/XX
     * @return: 初始化结果信息
     **/
    @PostMapping("/initCompanyList")
    @LogAction(value = LogActionEnum.UPDATE, desc = "初始化公司列表")
    public ApiResult<Map<String, Object>> initCompanyList() {
        Map<String, Object> result = cfgInvoiceSettingService.initCompanyList();
        return success(result);
    }

}
