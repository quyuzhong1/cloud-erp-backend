package com.erp.server.scm.controller.api;


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
import com.erp.server.scm.service.ScmPushMsgService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.scm.dto.ScmPushMsgDTO;

/**
 * 本地推送消息表
 *
 * @author shukai
 * @since 2024-08-29
 */
@Slf4j
@RestController
@LogSystemModule("本地推送消息表")
@RequestMapping("/scmPushMsg")
public class ScmPushMsgController extends BaseController {

    @Resource
    private ScmPushMsgService scmPushMsgService;

    /**
    * 新增
    * @author shukai
    * @date:  2024-08-29
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "本地推送消息表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated ScmPushMsgDTO.AddDTO dto) {
        return success(scmPushMsgService.add(dto));
    }

    /**
    * 修改
    * @author shukai
    * @date:  2024-08-29
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "本地推送消息表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "scm:scmPushMsg:update",
        serviceClass = ScmPushMsgService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated ScmPushMsgDTO.UpdateDTO dto) {
        scmPushMsgService.update(dto);
        return success();
    }



}
