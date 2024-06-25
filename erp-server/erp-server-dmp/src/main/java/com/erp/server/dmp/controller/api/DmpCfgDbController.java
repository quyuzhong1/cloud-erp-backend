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
import com.erp.server.dmp.service.DmpCfgDbService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.dmp.dto.DmpCfgDbDTO;

/**
 * 输入输出db信息
 *
 * @author shukai
 * @since 2024-06-11
 */
@Slf4j
@RestController
@LogSystemModule("输入输出db信息")
@RequestMapping("/dmpCfgDb")
public class DmpCfgDbController extends BaseController {

    @Resource
    private DmpCfgDbService dmpCfgDbService;

    /**
    * 新增
    * @author shukai
    * @date:  2024-06-11
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "输入输出db信息新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated DmpCfgDbDTO.AddDTO dto) {
        return success(dmpCfgDbService.add(dto));
    }

    /**
    * 修改
    * @author shukai
    * @date:  2024-06-11
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "输入输出db信息修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "dmp:dmpCfgDb:update",
        serviceClass = DmpCfgDbService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated DmpCfgDbDTO.UpdateDTO dto) {
        dmpCfgDbService.update(dto);
        return success();
    }



}
