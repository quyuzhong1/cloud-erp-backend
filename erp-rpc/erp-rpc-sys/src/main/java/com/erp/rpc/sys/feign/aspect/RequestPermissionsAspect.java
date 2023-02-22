package com.erp.rpc.sys.feign.aspect;

import com.common.core.utils.ObjectUtils;
import com.common.business.annotation.RequestPermissions;
import com.common.business.dto.UserRequestPermissionsDTO;
import com.common.business.interceptor.CommonInterceptor;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.business.vo.LoginUser;
import com.erp.rpc.sys.feign.SysUserFeign;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

/**
 * 实现 RequestPermissions 请求权限的注解
 *
 * @Classname
 * @Description TODO
 * @Date 2022-10-15 10:21
 * @Created by yl
 */
@Aspect
@Component
public class RequestPermissionsAspect {

    @Resource
    private SysUserFeign sysUserFeign;

    @Around("@annotation(com.common.business.annotation.RequestPermissions)")
    public Object aroundAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
        Object obj = null;
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Object[] params = joinPoint.getArgs();
        RequestPermissions inject = method.getAnnotation(RequestPermissions.class);
        //获取到对应的权限code
        String permissionsCode = inject.value();
        //当权限code 不为空的时候
        if (StringUtils.isNotBlank(permissionsCode)) {
            String userId = "";
            String userName = "";
            LoginUser userInfo = CommonInterceptor.threadLocal.get();
            if (Objects.isNull(userInfo)) {
                userInfo = new LoginUser();
                userInfo.setUid(userId);
                userInfo.setUserName(userName);
            }
            //当用户id 不为空的时候
            if (StringUtils.isNotBlank(userId)) {
                List<UserRequestPermissionsDTO> permissionsList = sysUserFeign.getRequestPermissionsList(userId);
                UserRequestPermissionsDTO permissions = permissionsList.stream().
                        filter(r -> permissionsCode.equals(r.getPermissionsCode())).findFirst().orElse(null);
                if (Objects.isNull(permissions)) {
                    throw new ServiceException(ApiError.ERROR_1013);
                } else {
                    if(params.length > 0){
                        ObjectUtils.setFieldValue(params[inject.index()],inject.dataScope(),permissions.getDataScope());
                    }
                }
            }else{
                throw new ServiceException(ApiError.ERROR_403);
            }

        }

        obj = joinPoint.proceed(params);
        return obj;
    }
}
