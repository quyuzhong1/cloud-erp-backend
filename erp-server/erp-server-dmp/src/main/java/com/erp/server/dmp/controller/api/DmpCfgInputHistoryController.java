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
import com.erp.server.dmp.service.DmpCfgInputHistoryService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.dmp.dto.DmpCfgInputHistoryDTO;

/**
 * 外部系统接口明细补偿
 *
 * @author shukai
 * @since 2024-06-11
 */
@Slf4j
@RestController
@LogSystemModule("外部系统接口明细补偿")
@RequestMapping("/dmpCfgInputHistory")
public class DmpCfgInputHistoryController extends BaseController {

    @Resource
    private DmpCfgInputHistoryService dmpCfgInputHistoryService;

    /**
    * 新增
    * @author shukai
    * @date:  2024-06-11
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "外部系统接口明细补偿新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated DmpCfgInputHistoryDTO.AddDTO dto) {
        return success(dmpCfgInputHistoryService.add(dto));
    }

    /**
    * 修改
    * @author shukai
    * @date:  2024-06-11
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "外部系统接口明细补偿修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "dmp:dmpCfgInputHistory:update",
        serviceClass = DmpCfgInputHistoryService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated DmpCfgInputHistoryDTO.UpdateDTO dto) {
        dmpCfgInputHistoryService.update(dto);
        return success();
    }



}
