package com.erp.server.sys.controller.api;


import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.business.dto.base.BaseResultDTO;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.sys.dto.SysEventTrackingDTO;
import com.erp.server.sys.service.SysEventTrackingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

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
    * @param data
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestParam Map<String, Object> data) {
        Object dataObj = data.get("data");
        SysEventTrackingDTO.AddDTO addDTO = JSONUtil.toBean(JSONUtil.toJsonStr(dataObj), SysEventTrackingDTO.AddDTO.class);
        return success(sysEventTrackingService.add(addDTO));
    }
}
