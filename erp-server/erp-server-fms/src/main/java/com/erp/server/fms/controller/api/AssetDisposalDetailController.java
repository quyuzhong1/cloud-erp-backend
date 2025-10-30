package com.erp.server.fms.controller.api;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.fms.service.AssetDisposalDetailService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.fms.dto.AssetDisposalDetailDTO;

/**
 * 资产处置单资产明细表
 *
 * @author jack
 * @since 2025-10-29
 */
@Slf4j
@RestController
@LogSystemModule("资产处置单资产明细表")
@RequestMapping("/assetDisposalDetail")
public class AssetDisposalDetailController extends BaseController {

    @Resource
    private AssetDisposalDetailService assetDisposalDetailService;

    /**
    * 新增
    * @author jack
    * @date:  2025-10-29
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "资产处置单资产明细表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated AssetDisposalDetailDTO.AddDTO dto) {
        return success(assetDisposalDetailService.add(dto));
    }

    /**
    * 修改
    * @author jack
    * @date:  2025-10-29
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "资产处置单资产明细表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "fms:assetDisposalDetail:update",
        serviceClass = AssetDisposalDetailService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated AssetDisposalDetailDTO.UpdateDTO dto) {
        assetDisposalDetailService.update(dto);
        return success();
    }



}
