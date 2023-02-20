package com.cloud.erp.chrome.controller;


import com.cloud.erp.chrome.dto.FindTaskDTO;
import com.cloud.erp.chrome.entity.ScheduleTaskEntity;
import com.cloud.erp.chrome.service.ChromeTaskInfoService;
import com.common.core.controller.vo.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.common.core.controller.BaseController;

import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author yl
 * @since 2022-08-25
 */
@RestController
@RequestMapping("/chromeTask")
@CrossOrigin(origins = "*")
public class ChromeTaskInfoController extends BaseController {

    @Autowired
    private ChromeTaskInfoService chromeTaskInfoService;


    //获取到任务列表

    /**
     * 获取到任务列表
     * @param dto
     * @return
     */
    @PostMapping("/findTaskList")
    public ApiResult getChromeList(@RequestBody @Validated FindTaskDTO dto) {
        List<ScheduleTaskEntity> list = chromeTaskInfoService.getChromeTaskList(dto);
        return success(list);
    }


}

