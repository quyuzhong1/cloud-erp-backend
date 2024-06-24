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
import com.erp.server.dmp.service.DmpCfgInputChildService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.dmp.dto.DmpCfgInputChildDTO;

/**
 * 父子任务关系
 *
 * @author shukai
 * @since 2024-06-21
 */
@Slf4j
@RestController
@LogSystemModule("父子任务关系")
@RequestMapping("/dmpCfgInputChild")
public class DmpCfgInputChildController extends BaseController {

    @Resource
    private DmpCfgInputChildService dmpCfgInputChildService;

    /**
    * 新增
    * @author shukai
    * @date:  2024-06-21
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "父子任务关系新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated DmpCfgInputChildDTO.AddDTO dto) {
        return success(dmpCfgInputChildService.add(dto));
    }

    /**
    * 修改
    * @author shukai
    * @date:  2024-06-21
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "父子任务关系修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "dmp:dmpCfgInputChild:update",
        serviceClass = DmpCfgInputChildService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated DmpCfgInputChildDTO.UpdateDTO dto) {
        dmpCfgInputChildService.update(dto);
        return success();
    }



}
