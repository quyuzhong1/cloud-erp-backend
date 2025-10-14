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
import com.erp.server.fms.service.AssetAcceptPersonService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.fms.dto.AssetAcceptPersonDTO;

/**
 * 资产验收人员关联表
 *
 * @author wuht
 * @since 2025-10-11
 */
@Slf4j
@RestController
@LogSystemModule("资产验收人员关联表")
@RequestMapping("/assetAcceptPerson")
public class AssetAcceptPersonController extends BaseController {

    @Resource
    private AssetAcceptPersonService assetAcceptPersonService;

    /**
    * 新增
    * @author wuht
    * @date:  2025-10-11
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "资产验收人员关联表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated AssetAcceptPersonDTO.AddDTO dto) {
        return success(assetAcceptPersonService.add(dto));
    }

    /**
    * 修改
    * @author wuht
    * @date:  2025-10-11
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "资产验收人员关联表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "fms:assetAcceptPerson:update",
        serviceClass = AssetAcceptPersonService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated AssetAcceptPersonDTO.UpdateDTO dto) {
        assetAcceptPersonService.update(dto);
        return success();
    }



}
