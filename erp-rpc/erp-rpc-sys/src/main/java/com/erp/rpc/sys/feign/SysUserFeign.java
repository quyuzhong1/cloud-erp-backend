package com.erp.rpc.sys.feign;

import com.erp.common.dto.base.ApiResult;
import com.erp.common.dto.base.BaseSearchDTO;
import com.erp.common.modules.sys.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @Classname 系统管理 服务
 * @Description TODO
 * @Date 2022-07-08 16:52
 * @Created by yl
 */
@FeignClient("erp-sys")
public interface SysUserFeign {

    //账号登录
    @PostMapping("sys/feign/user/accountLogin")
    ApiResult<SysUserDTO> accountLogin(@RequestBody AccountLoginDTO loginDTO);

    //设置登录ip账号登录
    @PostMapping("sys/feign/user/setLoginIp")
    ApiResult<SysUserDTO> setLoginIp(@RequestBody SysLoginIpDTO loginDTO);

    //扫码登录
    @PostMapping("sys/feign/user/scanCodeLogin")
    ApiResult<SysUserDTO> scanCodeLogin(@RequestBody SysUserThirdDTO loginDTO);


    //获取用户列表
    @PostMapping("sys/feign/user/findList")
    ApiResult<List<FindUserDTO>> userList(@RequestBody BaseSearchDTO dto);

    //获取用户权限
    @PostMapping("sys/feign/user/getRequestPermissionsList")
    List<UserRequestPermissionsDTO> getRequestPermissionsList(@RequestBody String userId);

    //获取用户列表
    @GetMapping("sys/feign/user/getUserList")
    List<FindUserDTO> getUserList();

    //获取部门的用户
    @GetMapping("sys/feign/user/getDepUserList")
    List<SysUserDTO> getDepUserList(@RequestBody String userId);

    //根据用户id 获取用户角色的id
    @GetMapping("sys/feign/user/getRoleIdList")
    List<String> getRoleIdList(@RequestBody String userId);



}
