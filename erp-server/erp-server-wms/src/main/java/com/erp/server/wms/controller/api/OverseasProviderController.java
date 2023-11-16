package com.erp.server.wms.controller.api;


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
import com.erp.server.wms.service.OverseasProviderService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.OverseasProviderDTO;

/**
 * 海外物流商
 *
 * @author Luo_wg
 * @since 2023-11-16
 */
@Slf4j
@RestController
@LogSystemModule("海外物流商")
@RequestMapping("/overseasProvider")
public class OverseasProviderController extends BaseController {

    @Resource
    private OverseasProviderService overseasProviderService;

    /**
    * 新增
    * @author Luo_wg
    * @date:  2023-11-16
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "海外物流商新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated OverseasProviderDTO.AddDTO dto) {
        return success(overseasProviderService.add(dto));
    }

    /**
    * 修改
    * @author Luo_wg
    * @date:  2023-11-16
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "海外物流商修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:overseasProvider:update",
        serviceClass = OverseasProviderService.class,
        keyIdName = "id")
    public ApiResult update(@RequestBody @Validated OverseasProviderDTO.UpdateDTO dto) {
        overseasProviderService.update(dto);
        return success();
    }



}
