package com.erp.server.sys.controller.api;


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
import com.erp.server.sys.service.SysPushMsgService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.sys.dto.SysPushMsgDTO;

/**
 * 本地推送消息表
 *
 * @author shukai
 * @since 2024-08-27
 */
@Slf4j
@RestController
@LogSystemModule("本地推送消息表")
@RequestMapping("/sysPushMsg")
public class SysPushMsgController extends BaseController {

    @Resource
    private SysPushMsgService sysPushMsgService;

    /**
    * 新增
    * @author shukai
    * @date:  2024-08-27
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "本地推送消息表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated SysPushMsgDTO.AddDTO dto) {
        return success(sysPushMsgService.add(dto));
    }

    /**
    * 修改
    * @author shukai
    * @date:  2024-08-27
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "本地推送消息表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "sys:sysPushMsg:update",
        serviceClass = SysPushMsgService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated SysPushMsgDTO.UpdateDTO dto) {
        sysPushMsgService.update(dto);
        return success();
    }



}
