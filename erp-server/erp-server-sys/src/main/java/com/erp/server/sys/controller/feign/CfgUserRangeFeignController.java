package com.erp.server.sys.controller.feign;

import com.common.core.controller.BaseController;
import com.erp.model.sys.dto.CfgUserRangeDTO;
import com.erp.server.sys.service.CfgUserRangeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户区间配置远程调用
 * @CreateTime: 2023-06-15  14:03
 * @Author: zhangchunlin
 */
@RestController
@RequestMapping("feign/cfgUserRange")
public class CfgUserRangeFeignController extends BaseController {

    @Autowired
    private CfgUserRangeService cfgUserRangeService;

    /**
     * 获取用户区间配置
     * @param type
     * @return
     */
    @GetMapping("/getByType")
    public List<CfgUserRangeDTO.UserRangeDataDTO> getUserRangeByType(@RequestParam(value = "type")String type) {
        return cfgUserRangeService.getUserRanges(type);
    }

}