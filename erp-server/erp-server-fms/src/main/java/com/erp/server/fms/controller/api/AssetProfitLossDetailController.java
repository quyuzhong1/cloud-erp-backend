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
import com.erp.server.fms.service.AssetProfitLossDetailService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.fms.dto.AssetProfitLossDetailDTO;

/**
 * 盘盈盘亏单明细表
 *
 * @author wuht
 * @since 2025-10-11
 */
@Slf4j
@RestController
@LogSystemModule("盘盈盘亏单明细表")
@RequestMapping("/assetProfitLossDetail")
public class AssetProfitLossDetailController extends BaseController {

    @Resource
    private AssetProfitLossDetailService assetProfitLossDetailService;

    /**
    * 新增
    * @author wuht
    * @date:  2025-10-11
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "盘盈盘亏单明细表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated AssetProfitLossDetailDTO.AddDTO dto) {
        return success(assetProfitLossDetailService.add(dto));
    }

    /**
    * 修改
    * @author wuht
    * @date:  2025-10-11
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "盘盈盘亏单明细表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "fms:assetProfitLossDetail:update",
        serviceClass = AssetProfitLossDetailService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated AssetProfitLossDetailDTO.UpdateDTO dto) {
        assetProfitLossDetailService.update(dto);
        return success();
    }



}
