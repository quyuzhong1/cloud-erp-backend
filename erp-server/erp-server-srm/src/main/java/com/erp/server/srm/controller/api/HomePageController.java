package com.erp.server.srm.controller.api;


import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.srm.dto.HomePageDTO;
import com.erp.server.srm.service.HomePageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 首页
 */
@Slf4j
@RestController
@LogSystemModule("首页")
@RequestMapping("/homePage")
public class HomePageController extends BaseController {

    @Resource
    private HomePageService homePageService;

    /**
     * 查询账号信息
     * @return
     */
    @GetMapping("/getAccountInfo")
    public ApiResult<HomePageDTO.AccountInfoDTO> getAccountInfo(){
        return success(homePageService.getAccountInfo());
    }

    /**
     * 查询待办信息
     * @return
     */
    @GetMapping("/getToDoItems")
    public ApiResult<HomePageDTO.ToDoItems> getToDoItems(){
        return success(homePageService.getToDoItems());
    }

    /**
     * 查询统计数据
     * @return
     */
    @GetMapping("/getStatistical")
    public ApiResult<HomePageDTO.Statistical> getStatistical(@RequestParam(value = "year") String year){
        return success(homePageService.getStatistical(year));
    }
}
