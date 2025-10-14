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
import com.erp.server.fms.service.AssetStocktakingDetailService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.fms.dto.AssetStocktakingDetailDTO;

/**
 * 资产盘点明细表
 *
 * @author wuht
 * @since 2025-10-11
 */
@Slf4j
@RestController
@LogSystemModule("资产盘点明细表")
@RequestMapping("/assetStocktakingDetail")
public class AssetStocktakingDetailController extends BaseController {

    @Resource
    private AssetStocktakingDetailService assetStocktakingDetailService;

    /**
    * 新增
    * @author wuht
    * @date:  2025-10-11
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "资产盘点明细表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated AssetStocktakingDetailDTO.AddDTO dto) {
        return success(assetStocktakingDetailService.add(dto));
    }

    /**
    * 修改
    * @author wuht
    * @date:  2025-10-11
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "资产盘点明细表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "fms:assetStocktakingDetail:update",
        serviceClass = AssetStocktakingDetailService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated AssetStocktakingDetailDTO.UpdateDTO dto) {
        assetStocktakingDetailService.update(dto);
        return success();
    }



}
