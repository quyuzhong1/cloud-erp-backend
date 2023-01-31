package com.erp.server.auth.controller.api;


import com.common.core.constant.TokenConstants;
import com.common.core.utils.IpUtils;
import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.common.modules.sys.dto.AccountLoginDTO;
import com.erp.common.modules.sys.dto.SysLoginIpDTO;
import com.erp.common.modules.sys.dto.SysUserDTO;
import com.erp.common.modules.sys.dto.SysUserThirdDTO;
import com.erp.common.modules.sys.vo.SysLoginUserVO;
import com.erp.common.vo.LoginUser;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.auth.server.AuthTokenService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Objects;


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
            SysLoginUserVO sysLoginUserVO = new SysLoginUserVO();
            sysLoginUserVO.setAccessToken(accessToken);
            sysLoginUserVO.setOverallMenuList(info.getOverallMenuList());
            sysLoginUserVO.setPermissionList(info.getPermissionList());
            sysLoginUserVO.setUserName(info.getUserName());
            sysLoginUserVO.setLeftMenuList(info.getLeftMenuList());
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
            SysLoginUserVO sysLoginUserVO = new SysLoginUserVO();
            sysLoginUserVO.setAccessToken(accessToken);
            sysLoginUserVO.setOverallMenuList(info.getOverallMenuList());
            sysLoginUserVO.setPermissionList(info.getPermissionList());
            sysLoginUserVO.setUserName(info.getUserName());
            sysLoginUserVO.setLeftMenuList(info.getLeftMenuList());
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


    /**
     * 根据token 获取 用户信息
     *
     * @return com.erp.common.dto.base.ApiResult<com.erp.common.modules.sys.vo.SysLoginUserVO>
     * @author yl
     * @date 2023-01-14 9:21
     */
    @GetMapping("/getUserByToken")
    public ApiResult<SysLoginUserVO> getByToken( HttpServletRequest request) {
        String token= request.getHeader(TokenConstants.AUTHENTICATION);
        if(StringUtils.isBlank(token)){
            throw new ServiceException(ApiError.ERROR_403);
        }
        SysLoginUserVO result = new SysLoginUserVO();
        LoginUser loginUser = authTokenService.getLoginUser(token);
        if (Objects.isNull(loginUser)) {
            throw new ServiceException(ApiError.ERROR_403);
        }
        SysUserDTO sysUser = sysUserFeign.getSysUserById(loginUser.getUid());
        result.setAccessToken(token);
        result.setOverallMenuList(sysUser.getOverallMenuList());
        result.setPermissionList(sysUser.getPermissionList());
        result.setUserName(sysUser.getUserName());
        result.setLeftMenuList(sysUser.getLeftMenuList());
        result.setHeadIcon(sysUser.getHeadIcon());
        result.setBindingPlatform(sysUser.getBindingPlatform());
        result.setBindingState(sysUser.getBindingState());

        return success(result);
    }

}
