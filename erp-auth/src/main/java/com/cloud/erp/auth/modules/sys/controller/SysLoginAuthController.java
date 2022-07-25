package com.cloud.erp.auth.modules.sys.controller;

import com.cloud.erp.auth.modules.sys.feign.SysUserFeign;
import com.cloud.erp.auth.modules.web.server.AuthTokenService;
import com.cloud.erp.common.common.ApiResult;
import com.cloud.erp.common.common.BaseController;
import com.cloud.erp.common.constant.TokenConstants;
import com.cloud.erp.common.dto.AccountLoginDTO;
import com.cloud.erp.common.modules.sys.dto.SysLoginIpDTO;
import com.cloud.erp.common.modules.sys.dto.SysUserDTO;
import com.cloud.erp.common.modules.sys.dto.SysUserThirdDTO;
import com.cloud.erp.common.modules.sys.vo.SysLoginUserVO;
import com.cloud.erp.common.utils.IpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * @Classname SysLoginController
 * @Description TODO
 * @Date 2022-07-08 16:08
 * @Created by yl
 */
@RestController
@RequestMapping("auth/user")
public class SysLoginAuthController extends BaseController {

    @Autowired
    private SysUserFeign sysUserFeign;


    @Resource
    private AuthTokenService authTokenService;

    //账号登录
    @RequestMapping("/accountLogin")
    public ApiResult accountLogin(@RequestBody @Validated AccountLoginDTO loginDTO, HttpServletRequest request) {
        ApiResult<SysUserDTO> apiResult = sysUserFeign.accountLogin(loginDTO);
        int code = apiResult.getCode();
        if (code != 200) {
            return failure(code, apiResult.getMsg(), apiResult.getMsg());
        } else {
            SysUserDTO info = apiResult.getData();
            String ip = IpUtils.getIpAddress(request);
            info.setLoginIp(ip);
            SysLoginIpDTO ipDTO = new SysLoginIpDTO();
            ipDTO.setIp(ip);
            ipDTO.setDate(new Date());
            ipDTO.setUid(info.getUid());
            sysUserFeign.setLoginIp(ipDTO);
            //创建token
            String accessToken = authTokenService.createToken(info);
            SysLoginUserVO sysLoginUserVO=new SysLoginUserVO();
            sysLoginUserVO.setAccessToken(accessToken);
            sysLoginUserVO.setMenuList(info.getMenuList());
            sysLoginUserVO.setPermissionList(info.getPermissionList());
            sysLoginUserVO.setUserName(info.getUserName());
            sysLoginUserVO.setHeadIcon(info.getHeadIcon());
            sysLoginUserVO.setBindingPlatform(info.getBindingPlatform());
            sysLoginUserVO.setBindingState(info.getBindingState());
            return success(sysLoginUserVO);
        }

    }


    //扫码登录
    @RequestMapping("/scanCodeLogin")
    public ApiResult scanCodeLogin(@RequestBody @Validated SysUserThirdDTO loginDTO, HttpServletRequest request) {
        ApiResult<SysUserDTO> apiResult = sysUserFeign.scanCodeLogin(loginDTO);
        int code = apiResult.getCode();
        if (code != 200) {
            return failure(code, apiResult.getMsg(), apiResult.getMsg());
        } else {
            SysUserDTO info = apiResult.getData();
            String ip = IpUtils.getIpAddress(request);
            info.setLoginIp(ip);
            SysLoginIpDTO ipDTO = new SysLoginIpDTO();
            ipDTO.setIp(ip);
            ipDTO.setDate(new Date());
            ipDTO.setUid(info.getUid());
            sysUserFeign.setLoginIp(ipDTO);
            //创建token
            String accessToken = authTokenService.createToken(info);
            SysLoginUserVO sysLoginUserVO=new SysLoginUserVO();
            sysLoginUserVO.setAccessToken(accessToken);
            sysLoginUserVO.setMenuList(info.getMenuList());
            sysLoginUserVO.setPermissionList(info.getPermissionList());
            sysLoginUserVO.setUserName(info.getUserName());
            sysLoginUserVO.setHeadIcon(info.getHeadIcon());
            sysLoginUserVO.setBindingPlatform(info.getBindingPlatform());
            sysLoginUserVO.setBindingState(info.getBindingState());
            return success(sysLoginUserVO);
        }
    }




    //退出登录
    @RequestMapping("/logout")
    public ApiResult Logout(HttpServletRequest request) {
        String accountToken = request.getHeader(TokenConstants.AUTHENTICATION);
        authTokenService.removeToken(accountToken);
        return success();
    }


}
