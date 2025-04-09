package com.erp.server.oms.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.oms.dto.CfgInvoiceSettingDTO;
import com.erp.server.oms.service.CfgInvoiceSettingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.common.core.controller.BaseController;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * nf-e发票设置
 * @author Lambda
 * @since 2025-04-07
 */
@Slf4j
@RestController
@LogSystemModule("发票设置")
@RequestMapping("/cfgInvoiceSetting")
public class CfgInvoiceSettingController extends BaseController {
    @Resource
    private CfgInvoiceSettingService cfgInvoiceSettingService;

    /**
     * 发票设置分页
     *
     * @return
     */
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
     * 新增
     * @param dto
     * @return
     */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "发票设置新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated CfgInvoiceSettingDTO.AddDTO dto) {
        return success(cfgInvoiceSettingService.add(dto));
    }

    /**
     * 修改
     * @param dto
     * @return
     */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "发票设置修改")
    public ApiResult<?> update(@RequestBody @Validated CfgInvoiceSettingDTO.UpdateDTO dto) {
        cfgInvoiceSettingService.update(dto);
        return success();
    }

    /**
     * 删除
     *
     * @return
     */
    @PostMapping("/delete")
    @LogAction(value = LogActionEnum.UPDATE, desc = "发票设置删除")
    public ApiResult<Boolean> delete(@RequestBody @Valid BaseIdsDTO.IdsDTO idsDTO) {
        return success(cfgInvoiceSettingService.delete(idsDTO.getIds()));
    }

    /**
     * 详情
     * @param id
     * @return
     */
    @PostMapping("/view/{id}")
    public ApiResult<CfgInvoiceSettingDTO.ViewDTO> delete(@PathVariable("id") String id) {
        return success(cfgInvoiceSettingService.view(id));
    }

    /**
     * 启用
     * @param dto
     * @return
     */
    @PostMapping("/updateStatus")
    @LogAction(value = LogActionEnum.UPDATE, desc = "启用或禁用")
    public ApiResult<?> updateStatus(@RequestBody @Validated CfgInvoiceSettingDTO.UpdateDTO dto) {
        cfgInvoiceSettingService.updateStatus(dto);
        return success();
    }
}
