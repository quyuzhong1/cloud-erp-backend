package com.cloud.erp.auth.modules.sys.feign;

import com.cloud.erp.common.common.ApiResult;
import com.cloud.erp.common.dto.AccountLoginDTO;
import com.cloud.erp.common.modules.sys.dto.SysLoginIpDTO;
import com.cloud.erp.common.modules.sys.dto.SysUserDTO;
import com.cloud.erp.common.modules.sys.dto.SysUserThirdDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @Classname 系统管理 服务
 * @Description TODO
 * @Date 2022-07-08 16:52
 * @Created by yl
 */
@FeignClient("erp-admin")
public interface SysUserFeign {

    //账号登录
    @PostMapping("sys/admin/feign/user/accountLogin")
    ApiResult<SysUserDTO> accountLogin(@RequestBody AccountLoginDTO loginDTO);

    //设置登录ip账号登录
    @PostMapping("sys/admin/feign/user/setLoginIp")
    ApiResult<SysUserDTO> setLoginIp(@RequestBody SysLoginIpDTO loginDTO);

    //扫码登录
    @PostMapping("sys/admin/feign/user/scanCodeLogin")
    ApiResult<SysUserDTO> scanCodeLogin(@RequestBody SysUserThirdDTO loginDTO);
}
