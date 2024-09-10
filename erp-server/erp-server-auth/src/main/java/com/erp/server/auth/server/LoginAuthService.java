package com.erp.server.auth.server;

import cn.hutool.core.util.StrUtil;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.UserTypeEnum;
import com.common.business.utils.RedisUtil;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.IpUtils;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.sys.dto.AccountLoginDTO;
import com.erp.model.sys.dto.SysLoginIpDTO;
import com.erp.model.sys.dto.SysUserDTO;
import com.erp.model.sys.vo.SysLoginUserVO;
import com.erp.rpc.scm.feign.SupplierFeign;
import com.erp.rpc.sys.feign.SysUserFeign;

import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Objects;

@Component
public class LoginAuthService {

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private AuthTokenService authTokenService;

    @Resource
    private SupplierFeign supplierFeign;

    @Resource
    private RedisUtil redisUtil;

    public ApiResult<SysLoginUserVO> processLogin(AccountLoginDTO loginDTO, HttpServletRequest request) {
        String loginErrorKey = StrUtil.format(RedisCacheConstants.LOGIN_ERROR_KEY, loginDTO.getUserType(), loginDTO.getAccount());

        int loginAttempts = Integer.parseInt(redisUtil.get(loginErrorKey) != null ?redisUtil.get(loginErrorKey).toString() : "0");
        if(loginAttempts >= RedisCacheConstants.MAX_LOGIN_ATTEMPTS){
            return ApiResult.error(ApiError.LOGIN_ERROR);
        }

        ApiResult<SysUserDTO> apiResult = sysUserFeign.accountLogin(loginDTO);
        int code = apiResult.getCode();
        if (code != 200) {
            if(code == ApiError.ERROR_9012.code){
                loginAttempts++;
                redisUtil.set(loginErrorKey, String.valueOf(loginAttempts),RedisCacheConstants.LOCK_DURATION_MINUTES*60);
                if(loginAttempts >= RedisCacheConstants.MAX_LOGIN_ATTEMPTS){
                    return ApiResult.error(ApiError.LOGIN_ERROR);
                }
                throw new ServiceException(ApiError.LOGIN_USER_ERROR, RedisCacheConstants.MAX_LOGIN_ATTEMPTS - loginAttempts);
            }
            return ApiResult.error(code, apiResult.getMsg());
        } else {
            SysUserDTO info = apiResult.getData();
            //SRM校验供应商是否启用
            if(loginDTO.getUserType().equals(UserTypeEnum.SRM.getCode())){
                SupplierEntity supplier = supplierFeign.getSupplierByUid(info.getUid());
                if(Objects.isNull(supplier)){
                    return ApiResult.error(ApiError.ERROR_96001);
                }
                if(supplier.getDisabled()){
                    return ApiResult.error(ApiError.ERROR_LOGIN_DISABLE);
                }
                if(supplier.getSrmDisabled()){
                    return ApiResult.error(ApiError.ERROR_LOGIN_SRM_DISABLE);
                }
            }
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
            sysLoginUserVO.setIsSupper(info.getIsSupper());

            redisUtil.del(loginErrorKey);
            return ApiResult.success(sysLoginUserVO);
        }
    }
}
