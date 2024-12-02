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
import com.erp.server.tms.service.TmsPushMsgService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.tms.dto.TmsPushMsgDTO;

/**
 * 本地推送消息表
 *
 * @author Luo_WG
 * @since 2024-11-18
 */
@Slf4j
@RestController
@LogSystemModule("本地推送消息表")
@RequestMapping("/tmsPushMsg")
public class TmsPushMsgController extends BaseController {

    @Resource
    private TmsPushMsgService tmsPushMsgService;

    /**
    * 新增
    * @author Luo_WG
    * @date:  2024-11-18
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "本地推送消息表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated TmsPushMsgDTO.AddDTO dto) {
        return success(tmsPushMsgService.add(dto));
    }

    /**
    * 修改
    * @author Luo_WG
    * @date:  2024-11-18
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "本地推送消息表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "tms:tmsPushMsg:update",
        serviceClass = TmsPushMsgService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated TmsPushMsgDTO.UpdateDTO dto) {
        tmsPushMsgService.update(dto);
        return success();
    }



}
