package com.erp.server.plm.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.plm.dto.PilotApplicationDetailDTO;
import com.erp.server.plm.service.PilotApplicationDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 试产/量产 明细
 *
 * @author tmj
 * @since 2024-08-27
 */
@Slf4j
@RestController
@LogSystemModule("试产/量产 明细")
@RequestMapping("/pilotApplicationDetail")
public class PilotApplicationDetailController extends BaseController {

    @Resource
    private PilotApplicationDetailService pilotApplicationDetailService;

    /**
    * 新增
    * @author tmj
    * @date:  2024-08-27
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "试产/量产 明细新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated PilotApplicationDetailDTO.AddDTO dto) {
        return success(pilotApplicationDetailService.add(dto));
    }

    /**
    * 修改
    * @author tmj
    * @date:  2024-08-27
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "试产/量产 明细修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "plm:pilotApplicationDetail:update",
        serviceClass = PilotApplicationDetailService.class,
        keyIdName = "id")
    public ApiResult<Object> update(@RequestBody @Validated PilotApplicationDetailDTO.UpdateDTO dto) {
        pilotApplicationDetailService.update(dto);
        return success();
    }



}
