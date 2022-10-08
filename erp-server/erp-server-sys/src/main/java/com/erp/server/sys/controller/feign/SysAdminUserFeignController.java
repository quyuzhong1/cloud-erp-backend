package com.erp.server.sys.controller.feign;

import com.common.core.constant.UserStateConstants;
import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.common.dto.base.BaseSearchDTO;
import com.erp.common.enums.ApiError;
import com.erp.common.modules.sys.dto.*;
import com.erp.server.sys.service.SysUserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

/**
 * @Classname SysAdminUserFeignController
 * @Description TODO
 * @Date 2022-07-08 17:06
 * @Created by yl
 */
@RestController
@RequestMapping("sys/feign/user")
public class SysAdminUserFeignController extends BaseController {

    @Autowired
    private SysUserInfoService sysUserInfoService;


    @PostMapping("/accountLogin")
    public ApiResult<SysUserDTO> accountLogin(@RequestBody AccountLoginDTO dto) {
        SysUserDTO info = sysUserInfoService.accountLogin(dto);
        if (Objects.isNull(info)) {
            return failure(ApiError.ERROR_9012, null);
        }
        if (info.getUserState() == UserStateConstants.USER_DISABLE) {
            return failure(ApiError.ERROR_9016, null);
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
        if (Objects.isNull(info)) {
            return failure(ApiError.ERROR_9012, null);
        }
        if (info.getUserState() == UserStateConstants.USER_DISABLE) {
            return failure(ApiError.ERROR_9016, null);
        }
        return success(info);
    }

    @PostMapping("/findList")
    public ApiResult<List<FindUserDTO>> findList(@RequestBody @Validated BaseSearchDTO dto) {
        List<FindUserDTO> list = sysUserInfoService.getUserList(dto);
        return success(list);
    }


}
