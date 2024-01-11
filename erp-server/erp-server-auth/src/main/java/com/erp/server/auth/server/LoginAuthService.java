package com.erp.server.auth.server;

import com.common.business.enums.UserTypeEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.utils.IpUtils;
import com.erp.model.sys.dto.AccountLoginDTO;
import com.erp.model.sys.dto.SysLoginIpDTO;
import com.erp.model.sys.dto.SysUserDTO;
import com.erp.model.sys.vo.SysLoginUserVO;
import com.erp.rpc.sys.feign.SysUserFeign;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@Component
public class LoginAuthService {

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private AuthTokenService authTokenService;

    public ApiResult<SysLoginUserVO> processLogin(AccountLoginDTO loginDTO, UserTypeEnum userType, HttpServletRequest request) {
        loginDTO.setUserType(userType.code);
        ApiResult<SysUserDTO> apiResult = sysUserFeign.accountLogin(loginDTO);
        int code = apiResult.getCode();
        if (code != 200) {
            return ApiResult.error(code, apiResult.getMsg());
        } else {
            SysUserDTO info = apiResult.getData();
            String ip = IpUtils.getIpAddress(request);
            info.setLoginIp(ip);
            SysLoginIpDTO ipDTO = new SysLoginIpDTO();
            ipDTO.setIp(ip);
            ipDTO.setDate(new Date());
            ipDTO.setUid(info.getUid());
            sysUserFeign.setLoginIp(ipDTO);

            // 创建token
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
            sysLoginUserVO.setNeedChangePwd(info.getNeedChangePwd());

            return ApiResult.success(sysLoginUserVO);
        }
    }
}
