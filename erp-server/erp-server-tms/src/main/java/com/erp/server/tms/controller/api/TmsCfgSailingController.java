package com.erp.server.tms.controller.api;


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
import com.erp.server.tms.service.TmsCfgSailingService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.tms.dto.TmsCfgSailingDTO;

/**
 * 截单开船配置
 *
 * @author will
 * @since 2024-03-15
 */
@Slf4j
@RestController
@LogSystemModule("截单开船配置")
@RequestMapping("/tmsCfgSailing")
public class TmsCfgSailingController extends BaseController {

    @Resource
    private TmsCfgSailingService tmsCfgSailingService;

    /**
    * 新增
    * @author will
    * @date:  2024-03-15
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "截单开船配置新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated TmsCfgSailingDTO.AddDTO dto) {
        return success(tmsCfgSailingService.add(dto));
    }

    /**
    * 修改
    * @author will
    * @date:  2024-03-15
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "截单开船配置修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "tms:tmsCfgSailing:update",
        serviceClass = TmsCfgSailingService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated TmsCfgSailingDTO.UpdateDTO dto) {
        tmsCfgSailingService.update(dto);
        return success();
    }



}
