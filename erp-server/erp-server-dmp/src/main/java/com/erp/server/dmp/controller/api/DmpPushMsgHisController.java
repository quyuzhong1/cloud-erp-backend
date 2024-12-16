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
import com.erp.server.dmp.service.DmpPushMsgHisService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.dmp.dto.DmpPushMsgHisDTO;

/**
 * 本地消息表归档
 *
 * @author shukai
 * @since 2024-12-16
 */
@Slf4j
@RestController
@LogSystemModule("本地消息表归档")
@RequestMapping("/dmpPushMsgHis")
public class DmpPushMsgHisController extends BaseController {

    @Resource
    private DmpPushMsgHisService dmpPushMsgHisService;

    /**
    * 新增
    * @author shukai
    * @date:  2024-12-16
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "本地消息表归档新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated DmpPushMsgHisDTO.AddDTO dto) {
        return success(dmpPushMsgHisService.add(dto));
    }

    /**
    * 修改
    * @author shukai
    * @date:  2024-12-16
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "本地消息表归档修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "dmp:dmpPushMsgHis:update",
        serviceClass = DmpPushMsgHisService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated DmpPushMsgHisDTO.UpdateDTO dto) {
        dmpPushMsgHisService.update(dto);
        return success();
    }



}
