package com.erp.server.oms.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.oms.dto.OmsPushMsgDTO;
import com.erp.server.oms.service.OmsPushMsgService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 本地推送消息表
 *
 * @author shukai
 * @since 2024-08-22
 */
@Slf4j
@RestController
@LogSystemModule("本地推送消息表")
@RequestMapping("/omsPushMsg")
public class OmsPushMsgController extends BaseController {

    @Resource
    private OmsPushMsgService omsPushMsgService;

    /**
    * 新增
    * @author shukai
    * @date:  2024-08-22
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "本地推送消息表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated OmsPushMsgDTO.AddDTO dto) {
        return success(omsPushMsgService.add(dto));
    }

    /**
    * 修改
    * @author shukai
    * @date:  2024-08-22
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "本地推送消息表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "oms:omsPushMsg:update",
        serviceClass = OmsPushMsgService.class,
        keyIdName = "id")
    public ApiResult<Object> update(@RequestBody @Validated OmsPushMsgDTO.UpdateDTO dto) {
        omsPushMsgService.update(dto);
        return success();
    }



}
