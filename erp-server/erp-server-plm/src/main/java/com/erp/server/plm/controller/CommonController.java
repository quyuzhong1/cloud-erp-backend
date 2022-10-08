package com.erp.server.plm.controller;

import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.common.dto.base.BaseSearchDTO;
import com.erp.common.modules.sys.dto.FindUserDTO;
import com.erp.rpc.sys.feign.SysUserFeign;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Classname plm 公共接口
 * @Description TODO
 * @Date 2022-10-08 14:59
 * @Created by yl
 */
@RestController
@RequestMapping("plm/common")
public class CommonController  extends BaseController {

    @Resource
    private SysUserFeign sysUserFeign;

    @PostMapping("/findUserList")
    public ApiResult findUserList(@RequestBody  BaseSearchDTO dto){
        return sysUserFeign.userList(dto);
    }
}
