package com.erp.server.auth.controller.api;


import java.util.Date;
import java.util.Objects;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.common.business.constant.TokenConstants;
import com.common.business.vo.LoginUser;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.enums.LogActionEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.IpUtils;
import com.erp.model.sys.dto.AccountLoginDTO;
import com.erp.model.sys.dto.SysLoginIpDTO;
import com.erp.model.sys.dto.SysUserDTO;
import com.erp.model.sys.dto.SysUserThirdDTO;
import com.erp.model.sys.vo.SysLoginUserVO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.auth.server.AuthTokenService;
import com.erp.server.auth.server.LoginAuthService;


/**
 * 用户登录模块
 * @Date 2022-07-08 16:08
 * @Created by yl
 */
@RestController
@LogSystemModule("用户登录模块")
@RequestMapping("user")
public class SysLoginAuthController extends BaseController {

    @Autowired
    private SysUserFeign sysUserFeign;

    @Resource
    private AuthTokenService authTokenService;

    @Resource
    private LoginAuthService loginAuthService;

    /**
     * erp登录
     */
    @LogAction(value = LogActionEnum.LOGIN, desc = "ERP登录")
    @RequestMapping("/accountLogin")
    public ApiResult<SysLoginUserVO> accountLogin(@RequestBody @Validated AccountLoginDTO loginDTO, HttpServletRequest request) {
        return loginAuthService.processLogin(loginDTO, request);
    }

    /**
     * 扫码登录
     */
    @LogAction(value = LogActionEnum.LOGIN, desc = "扫码登录")
    @RequestMapping("/scanCodeLogin")
    public ApiResult scanCodeLogin(@RequestBody @Validated SysUserThirdDTO loginDTO, HttpServletRequest request) {
        ApiResult<SysUserDTO> apiResult = sysUserFeign.scanCodeLogin(loginDTO);
        int code = apiResult.getCode();
        if (code != 200) {
            return failure(code, apiResult.getMsg(), null);
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
            sysLoginUserVO.setUserId(info.getUid());
            sysLoginUserVO.setIsSupper(info.getIsSupper());
            sysLoginUserVO.setDeptId(info.getDeptId());
            sysLoginUserVO.setDeptName(info.getDeptName());
            return success(sysLoginUserVO);
        }
    }


    /**
     * 退出登录
     */
    @LogAction(value = LogActionEnum.LOGOUT, desc = "退出登录")
    @RequestMapping("/logout")
    public ApiResult logout(HttpServletRequest request) {
        String accountToken = request.getHeader(TokenConstants.AUTHENTICATION);
        authTokenService.removeToken(accountToken);
        return success();
    }


    /**
     * 根据token 获取 用户信息
     * @return com.common.core.vo.ApiResult<com.erp.model.sys.vo.SysLoginUserVO>
     * @author yl
     * @date 2023-01-14 9:21
     */
    @GetMapping("/getUserByToken")
    public ApiResult<SysLoginUserVO> getByToken( HttpServletRequest request) {
        String token= request.getHeader(TokenConstants.AUTHENTICATION);
        if(StringUtils.isBlank(token)){
            throw new ServiceException(ApiError.ERROR_403);
        }
        return success(loginAuthService.getByToken(token));
    }

}
