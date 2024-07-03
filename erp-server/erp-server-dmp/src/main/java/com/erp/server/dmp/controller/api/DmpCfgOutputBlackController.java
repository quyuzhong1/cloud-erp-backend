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
import com.erp.server.dmp.service.DmpCfgOutputBlackService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.dmp.dto.DmpCfgOutputBlackDTO;

/**
 * 输出黑名单
 *
 * @author shukai
 * @since 2024-07-03
 */
@Slf4j
@RestController
@LogSystemModule("输出黑名单")
@RequestMapping("/dmpCfgOutputBlack")
public class DmpCfgOutputBlackController extends BaseController {

    @Resource
    private DmpCfgOutputBlackService dmpCfgOutputBlackService;

    /**
    * 新增
    * @author shukai
    * @date:  2024-07-03
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "输出黑名单新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated DmpCfgOutputBlackDTO.AddDTO dto) {
        return success(dmpCfgOutputBlackService.add(dto));
    }

    /**
    * 修改
    * @author shukai
    * @date:  2024-07-03
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "输出黑名单修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "dmp:dmpCfgOutputBlack:update",
        serviceClass = DmpCfgOutputBlackService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated DmpCfgOutputBlackDTO.UpdateDTO dto) {
        dmpCfgOutputBlackService.update(dto);
        return success();
    }



}
