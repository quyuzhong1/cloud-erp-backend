package com.cloud.erp.admin.modules.sys.controller.feign;

import com.cloud.erp.admin.modules.sys.service.SysUserInfoService;
import com.comm.core.constant.UserStateConstants;
import com.erp.common.controller.BaseController;
import com.erp.common.dto.ApiResult;
import com.erp.common.enums.ApiError;
import com.erp.common.modules.sys.dto.AccountLoginDTO;
import com.erp.common.modules.sys.dto.SysLoginIpDTO;
import com.erp.common.modules.sys.dto.SysUserDTO;
import com.erp.common.modules.sys.dto.SysUserThirdDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * @Classname SysAdminUserFeignController
 * @Description TODO
 * @Date 2022-07-08 17:06
 * @Created by yl
 */
@RestController
@RequestMapping("sys/admin/feign/user")
public class SysAdminUserFeignController extends BaseController {

    @Autowired
    private SysUserInfoService sysUserInfoService;


    @PostMapping("/accountLogin")
    public ApiResult<SysUserDTO> accountLogin(@RequestBody AccountLoginDTO dto) {
        SysUserDTO info = sysUserInfoService.accountLogin(dto);
        if(Objects.isNull(info)){
           return failure(ApiError.ERROR_9012,null);
        }
        if(info.getUserState()== UserStateConstants.USER_DISABLE){
            return failure(ApiError.ERROR_9016,null);
        }
        return success(info);
    }


    @PostMapping("/setLoginIp")
    public ApiResult<SysUserDTO> setLoginIp(@RequestBody SysLoginIpDTO dto) {
        sysUserInfoService.setLoginIp(dto);
        return success();
    }


    @PostMapping("/scanCodeLogin")
    public ApiResult<SysUserDTO> scanCodeLogin(@RequestBody SysUserThirdDTO dto) {
        SysUserDTO info = sysUserInfoService.scanCodeLogin(dto);
        if(Objects.isNull(info)){
            return failure(ApiError.ERROR_9012,null);
        }
        if(info.getUserState()== UserStateConstants.USER_DISABLE){
            return failure(ApiError.ERROR_9016,null);
        }
        return success(info);
    }
}
