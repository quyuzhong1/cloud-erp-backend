package com.erp.server.dmp.controller.api;


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
import com.erp.server.dmp.service.DmpThirdWarehouseInfoService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.dmp.dto.DmpThirdWarehouseInfoDTO;

/**
 * 第三方仓库
 *
 * @author shukai
 * @since 2024-08-06
 */
@Slf4j
@RestController
@LogSystemModule("第三方仓库")
@RequestMapping("/dmpThirdWarehouseInfo")
public class DmpThirdWarehouseInfoController extends BaseController {

    @Resource
    private DmpThirdWarehouseInfoService dmpThirdWarehouseInfoService;

    /**
    * 新增
    * @author shukai
    * @date:  2024-08-06
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "第三方仓库新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated DmpThirdWarehouseInfoDTO.AddDTO dto) {
        return success(dmpThirdWarehouseInfoService.add(dto));
    }

    /**
    * 修改
    * @author shukai
    * @date:  2024-08-06
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "第三方仓库修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "dmp:dmpThirdWarehouseInfo:update",
        serviceClass = DmpThirdWarehouseInfoService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated DmpThirdWarehouseInfoDTO.UpdateDTO dto) {
        dmpThirdWarehouseInfoService.update(dto);
        return success();
    }



}
