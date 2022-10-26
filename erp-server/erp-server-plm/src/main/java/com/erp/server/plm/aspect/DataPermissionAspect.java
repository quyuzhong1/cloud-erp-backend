package com.erp.server.plm.aspect;

import com.alibaba.excel.util.CollectionUtils;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import com.common.core.utils.ObjectUtils;
import com.common.core.utils.ReflectUtils;
import com.common.core.utils.StrUtils;
import com.erp.common.annotation.DataPermission;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.common.modules.sys.dto.SysUserDTO;
import com.erp.common.modules.sys.dto.UserRequestPermissionsDTO;
import com.erp.common.vo.LoginUser;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.plm.service.CommonService;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.hibernate.validator.internal.util.StringHelper;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.thymeleaf.spring5.context.SpringContextUtils;

import javax.annotation.Resource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据过滤处理
 */
@Aspect
@Component
public class DataPermissionAspect {
    /**
     * 全部数据权限
     */
    public static final Integer DATA_SCOPE_ALL = 3;

    /**
     * 部门数据权限
     */
    public static final Integer DATA_SCOPE_DEPT = 2;

    /**
     * 仅本人数据权限
     */
    public static final Integer DATA_SCOPE_SELF = 1;

    @Resource
    private CommonService commonService;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private ApplicationContext applicationContext;

    // 配置织入点
    @Pointcut("@annotation(com.erp.common.annotation.DataPermission)")
    public void dataScopePointCut(){
    }

    @Before("dataScopePointCut()")
    public void doBefore(JoinPoint point) throws Throwable {
        handleDataScope(point);
    }

    protected void handleDataScope(final JoinPoint joinPoint) {
        // 获得注解
        DataPermission controllerDataScope = getAnnotationLog(joinPoint);
        if (controllerDataScope == null) {
            return;
        }
        LoginUser userInfo = commonService.getUserInfo();
        //当用户id 不为空的时候
        if (StringUtils.isNotBlank(userInfo.getUid())) {
            dataScopeFilter(joinPoint, userInfo, controllerDataScope);
        }
    }

    /**
     * 是否存在注解，如果存在就获取
     */
    private DataPermission getAnnotationLog(JoinPoint joinPoint) {
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();
        if (method != null){
            return method.getAnnotation(DataPermission.class);
        }
        return null;
    }

    /**
     * 数据范围过滤
     *
     * @param joinPoint 切点
     * @param user 用户
     * @param controllerDataScope 自定义注解参数
     */
    public void dataScopeFilter(JoinPoint joinPoint, LoginUser user, DataPermission controllerDataScope) {
        List<UserRequestPermissionsDTO> requestPermissionsList = sysUserFeign.getRequestPermissionsList(user.getUid());
        List<UserRequestPermissionsDTO> userRequestPermissionsDTOStream = requestPermissionsList.stream().filter(req -> req.getPermissionsCode().equals(controllerDataScope.menuCode())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(userRequestPermissionsDTOStream)) {
            return;
            //throw new ServiceException(ApiError.ERROR_1013);
        }
        List<SysUserDTO> depUserList = sysUserFeign.getDepUserList(user.getUid());
        List<String> userList = new ArrayList<>();
        for (SysUserDTO sysUserDTO : depUserList) {
            userList.add(sysUserDTO.getUid());
        }
        switch (controllerDataScope.operationType()) {
            case "query":
                query(joinPoint, userRequestPermissionsDTOStream, userList, user, controllerDataScope);
                break;
            case "delete":
                delete(joinPoint, userRequestPermissionsDTOStream, userList, user, controllerDataScope);
                break;
            case "update":
                update(joinPoint, userRequestPermissionsDTOStream, userList, user, controllerDataScope);
                break;
            default:
                break;
        }
    }

    /**
     * 查询
     * @Author Luo_WG
     * @Date 2022/10/21 9:57
     * @param joinPoint 切点信息
     * @param userRequestPermissionsDTOStream 权限列表
     * @param userList 部门用户列表
     * @param user 用户信息
     * @param dataPermission 自定义注解信息
     * @return java.lang.String
     **/
    public void query(JoinPoint joinPoint, List<UserRequestPermissionsDTO> userRequestPermissionsDTOStream, List<String> userList, LoginUser user, DataPermission dataPermission) {
        Object[] params = joinPoint.getArgs();
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        DataPermission inject = method.getAnnotation(DataPermission.class);

        StringBuilder sqlString = new StringBuilder();
        for (UserRequestPermissionsDTO role : userRequestPermissionsDTOStream) {
            if (DATA_SCOPE_ALL.equals(role.getDataScope())) {
                sqlString = new StringBuilder();
                break;
            } else if (DATA_SCOPE_DEPT.equals(role.getDataScope())) {
                sqlString.append(" AND " + dataPermission.tableAlias() + "." + dataPermission.tableField() + " in (" + StringUtils.join(userList, ",") + ")");
            } else if (DATA_SCOPE_SELF.equals(role.getDataScope())) {
                sqlString.append(" AND " + dataPermission.tableAlias() + "." + dataPermission.tableField() + " = " + user.getUid() + " ");
            }
        }
        ObjectUtils.setFieldValue(params[inject.index()], inject.param(), sqlString.toString());
    }

    /**
     * 删除
     * @Author Luo_WG
     * @Date 2022/10/21 9:57
     * @param userRequestPermissionsDTOStream 权限列表
     * @param userList 部门用户列表
     * @param user 用户信息
     * @param dataPermission 自定义注解信息
     * @return java.lang.String
     **/
    public void delete(JoinPoint joinPoint, List<UserRequestPermissionsDTO> userRequestPermissionsDTOStream, List<String> userList, LoginUser user, DataPermission dataPermission) {
        Class<? extends IService> serviceClass = dataPermission.serviceClass();
        IService<?> service = getIservice(joinPoint, serviceClass.getName());

        List<Object> inputIdList = new ArrayList<>();

        Object[] args = joinPoint.getArgs();
        Object arg = joinPoint.getArgs()[0];
        if (arg instanceof List) {
            inputIdList = (List<Object>) arg;
        } else if (arg instanceof String[]) {
            inputIdList = Arrays.asList(args[0]);
        } else if (arg instanceof String) {
            inputIdList = Collections.singletonList(arg);
        } else if (arg instanceof Map) {
            Map mapParam = (Map) arg;
            try {
                if (mapParam.containsKey(dataPermission.keyIdName())) {
                    Object p = mapParam.get(dataPermission.keyIdName());
                    if(p instanceof String){
                        inputIdList.add(p);
                    } else if (p instanceof List){
                        inputIdList = (List<Object>) mapParam.get(dataPermission.keyIdName());
                    }
                } else {
                    inputIdList = (List<Object>) mapParam.get("ids");
                }
            } catch (Exception e) {
                return;
            }
        } else {
            try {
                Map mapParam = JSONObject.parseObject(JSONObject.toJSONString(arg), Map.class);
                Object o = mapParam.get(dataPermission.keyIdName());
                if(o != null){
                    if(o instanceof List){
                        inputIdList = (List<Object>) o;
                    } else if (o instanceof String){
                        inputIdList.add(o);
                    }
                } else {
                    inputIdList = (List<Object>) mapParam.get("ids");  // key : ids  数组
                }
            } catch (Exception e) {
                return;
            }
        }
        if(CollectionUtils.isEmpty(inputIdList)){
            return;
        }
        Object businessData = service.getById(arg.toString());

        String s = JSONObject.toJSONString(businessData);

        JSONObject jsonObject = JSONObject.parseObject(s);

        String userId = jsonObject.get(StrUtils.underlineToCamel(dataPermission.tableField(), true)).toString();

        for (UserRequestPermissionsDTO role : userRequestPermissionsDTOStream) {
            if (DATA_SCOPE_ALL.equals(role.getDataScope())) {
                break;
            } else if (DATA_SCOPE_DEPT.equals(role.getDataScope())) {
                if (!userList.contains(userId)) {
                    throw new ServiceException(ApiError.ERROR_1013);
                }
            } else if (DATA_SCOPE_SELF.equals(role.getDataScope())) {
                if (!user.equals(userId)) {
                    throw new ServiceException(ApiError.ERROR_1013);
                }
            }
        }
    }

    /**
     * 修改
     * @Author Luo_WG
     * @Date 2022/10/21 9:57
     * @param userRequestPermissionsDTOStream 权限列表
     * @param userList 部门用户列表
     * @param user 用户信息
     * @param dataPermission 自定义注解信息
     * @return java.lang.String
     **/
    public void update(JoinPoint joinPoint, List<UserRequestPermissionsDTO> userRequestPermissionsDTOStream, List<String> userList, LoginUser user, DataPermission dataPermission) {
        Class<? extends IService> serviceClass = dataPermission.serviceClass();
        IService<?> service = getIservice(joinPoint, serviceClass.getName());

        Object obj = joinPoint.getArgs()[0];

        JSONObject jsonObject = JSONObject.parseObject(JSONObject.toJSONString(obj));

        Object arg = jsonObject.get(dataPermission.entityName());

        List<Object> inputIdList = new ArrayList<>();

        if (arg instanceof List) {
            inputIdList = (List<Object>) arg;
        } else if (arg instanceof Map) {
            Map mapParam = (Map) arg;
            inputIdList.add(mapParam.get(dataPermission.keyIdName()));
        }


        JSONObject entity = JSONObject.parseObject(JSONObject.toJSONString(arg));

        if (StringUtils.isBlank(entity.get(dataPermission.keyIdName()).toString())) {
            return;
        }
        Object businessData = service.getById(entity.get(dataPermission.keyIdName()).toString());
        String j = JSONObject.toJSONString(businessData);
        JSONObject jo = JSONObject.parseObject(j);

        String userId = jo.get(StrUtils.underlineToCamel(dataPermission.tableField(), true)).toString();

        for (UserRequestPermissionsDTO role : userRequestPermissionsDTOStream) {
            if (DATA_SCOPE_ALL.equals(role.getDataScope())) {
                break;
            } else if (DATA_SCOPE_DEPT.equals(role.getDataScope())) {
                if (!userList.contains(userId)) {
                    throw new ServiceException(ApiError.ERROR_1013);
                }
            } else if (DATA_SCOPE_SELF.equals(role.getDataScope())) {
                if (!user.equals(userId)) {
                    throw new ServiceException(ApiError.ERROR_1013);
                }
            }
        }
    }

    /**
     * 获取服务名 获取相关服务
     *
     * @param point
     * @param className
     * @return
     */
    private IService<?> getIservice(JoinPoint point, String className) {
        if (StringUtils.isEmpty(className)) {
            className = point.getTarget().getClass().getSimpleName();
        }

        // 将第一个字母修改成小写
        String serviceName = className.substring(0, 1).toLowerCase() + className.substring(1);

        // 获取实体对象
        try {
            return applicationContext.getBean(serviceName, IService.class);
        } catch (NoSuchBeanDefinitionException e) {
            // 1.通过权限命名取一次服务
            try {
                return (IService<?>) applicationContext.getBean(Class.forName(serviceName));
            } catch (ClassNotFoundException ex1) {
                // 2.找不到可能是远程服务，通过以下方式获取远程服务(获取当前类服务)
                String serviceClassName = point.getTarget().getClass().getName();
                try {
                    return (IService<?>) applicationContext.getBean(Class.forName(serviceClassName));
                } catch (ClassNotFoundException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return null;
    }
}
