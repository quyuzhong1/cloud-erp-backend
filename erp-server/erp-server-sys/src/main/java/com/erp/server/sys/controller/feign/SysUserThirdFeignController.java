package com.erp.server.sys.controller.feign;

/**
 * @description:
 * @author: hcg
 * @date: 2025/5/19 18:39
 */

import com.common.core.controller.BaseController;
import com.erp.model.sys.entity.SysUserThirdEntity;
import com.erp.server.sys.service.SysUserThirdService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 *@Author: hcg
 *@CreateTime: 2025-05-19
 *@Description:
 *@Version: 1.0
 */
@RestController
@RequestMapping("feign/sysUserThird")
public class SysUserThirdFeignController extends BaseController{
    @Resource
    private SysUserThirdService sysUserThirdService;

    @GetMapping("/findByUserId")
    public SysUserThirdEntity findByUserId(@RequestParam("userId") String userId){
        return sysUserThirdService.findByUserId(userId);
    }
}
