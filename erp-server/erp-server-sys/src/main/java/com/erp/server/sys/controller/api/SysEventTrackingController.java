package com.erp.server.sys.controller.api;


import com.common.business.dto.base.BaseResultDTO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.sys.dto.SysEventTrackingDTO;
import com.erp.server.sys.service.SysEventTrackingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 前端埋点事件记录
 *
 * @author Jim
 * @since 2025-02-19
 */
@Slf4j
@RestController
@LogSystemModule("前端埋点事件记录")
@RequestMapping("/sysEventTracking")
public class SysEventTrackingController extends BaseController {

    @Resource
    private SysEventTrackingService sysEventTrackingService;

    /**
    * 新增
    * @author Jim
    * @date:  2025-02-19
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "前端埋点事件记录新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated SysEventTrackingDTO.AddDTO dto) {
        return success(sysEventTrackingService.add(dto));
    }


}
