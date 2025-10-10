package com.erp.server.plm.controller.api;


import com.erp.server.plm.service.MoldInfoService;
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
import com.erp.server.plm.service.MoldRefSkuService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.plm.dto.MoldRefSkuDTO;

/**
 * 模具关联sku
 *
 * @author jack
 * @since 2025-10-10
 */
@Slf4j
@RestController
@LogSystemModule("模具关联sku")
@RequestMapping("/moldRefSku")
public class MoldRefSkuController extends BaseController {

    @Resource
    private MoldRefSkuService moldRefSkuService;

    /**
    * 新增
    * @author jack
    * @date:  2025-10-10
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "模具关联sku新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated MoldRefSkuDTO.AddDTO dto) {
        return success(moldRefSkuService.add(dto));
    }

    /**
    * 修改
    * @author jack
    * @date:  2025-10-10
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "模具关联sku修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "plm:moldRefSku:update",
        serviceClass = MoldRefSkuService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated MoldRefSkuDTO.UpdateDTO dto) {
        moldRefSkuService.update(dto);
        return success();
    }

}
