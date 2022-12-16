package com.erp.common.business.aspect;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import com.common.core.utils.ObjectUtils;
import com.common.core.utils.StrUtils;
import com.erp.common.business.annotation.DataPermission;
import com.erp.common.business.interceptor.CommonInterceptor;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.common.modules.sys.dto.UserRequestPermissionsDTO;
import com.erp.common.vo.LoginUser;
import com.erp.rpc.sys.feign.SysUserFeign;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.*;

/**
 * 数据过滤处理
 */
@Aspect
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
    private SysUserFeign sysUserFeign;

    @Resource
    private ApplicationContext applicationContext;

    // 配置织入点
    @Pointcut("@annotation(com.erp.common.business.annotation.DataPermission)")
    public void dataScopePointCut() {
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
        String userId = "";
        String userName = "";
        LoginUser userInfo = CommonInterceptor.threadLocal.get();
        if (Objects.isNull(userInfo)) {
            userInfo = new LoginUser();
            userInfo.setUid(userId);
            userInfo.setUserName(userName);
        }
        //当用户id 不为空的时候
        if (StringUtils.isNotBlank(userInfo.getUid())) {
            dataScopeFilter(joinPoint, userInfo, controllerDataScope);
        } else {
            throw new ServiceException(ApiError.ERROR_403);
        }
    }

    /**
     * 是否存在注解，如果存在就获取
     */
    private DataPermission getAnnotationLog(JoinPoint joinPoint) {
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();
        if (method != null) {
            return method.getAnnotation(DataPermission.class);
        }
        return null;
    }

    /**
     * 数据范围过滤
     *
     * @param joinPoint           切点
     * @param user                用户
     * @param controllerDataScope 自定义注解参数
     */
    public void dataScopeFilter(JoinPoint joinPoint, LoginUser user, DataPermission controllerDataScope) {
        List<UserRequestPermissionsDTO> requestPermissionsList = sysUserFeign.getRequestPermissionsList(user.getUid());
        UserRequestPermissionsDTO userRequestPermissions = new UserRequestPermissionsDTO();
        List<String> roleIdList = sysUserFeign.getRoleIdList(user.getUid());
        if (roleIdList.contains("1")) {
            userRequestPermissions.setPermissionsCode(controllerDataScope.menuCode());
            userRequestPermissions.setDataScope(DATA_SCOPE_ALL);
        } else {
            userRequestPermissions = requestPermissionsList.stream().filter(p -> p.getPermissionsCode().equals(controllerDataScope.menuCode())).findFirst().orElse(null);
            if (Objects.isNull(userRequestPermissions)) {
                throw new ServiceException(ApiError.ERROR_1013);
            }
        }

        List<String> userList = sysUserFeign.getDepUserList(user.getUid());

        switch (controllerDataScope.operationType()) {
            case LIST:
                query(joinPoint, userRequestPermissions, userList, user, controllerDataScope);
                break;
            case CHECK_BY_PARAM:
                delete(joinPoint, userRequestPermissions, userList, user, controllerDataScope);
                break;
            case CHECK_BY_ID:
                update(joinPoint, userRequestPermissions, userList, user, controllerDataScope);
                break;
            default:
                break;
        }
    }

    /**
     * 查询
     *
     * @param joinPoint              切点信息
     * @param userRequestPermissions 权限列表
     * @param userList               部门用户列表
     * @param user                   用户信息
     * @param dataPermission         自定义注解信息
     * @return java.lang.String
     * @Author Luo_WG
     * @Date 2022/10/21 9:57
     **/
    public void query(JoinPoint joinPoint, UserRequestPermissionsDTO userRequestPermissions, List<String> userList, LoginUser user, DataPermission dataPermission) {
        Object[] params = joinPoint.getArgs();
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        DataPermission inject = method.getAnnotation(DataPermission.class);
        String tableFields = dataPermission.tableField();
        List<String> tableFieldList = Arrays.asList(tableFields.split(","));
        int tableFieldSize = tableFieldList.size();
        StringBuilder sqlString = new StringBuilder();
        if (DATA_SCOPE_ALL.equals(userRequestPermissions.getDataScope())) {
            sqlString = new StringBuilder();
            return;
        } else if (DATA_SCOPE_DEPT.equals(userRequestPermissions.getDataScope())) {
            List<String> list = new ArrayList<>();
            for (String s : userList) {
                list.add(s);
            }
            if (CollectionUtils.isNotEmpty(list)) {
                if (tableFieldSize == 1) {
                    sqlString.append(" AND string_to_array("+ dataPermission.tableAlias() + "."+ tableFieldList.get(0) +",',') && string_to_array('" + StringUtils.join(list,",") + "',',')");
                } else {
                    sqlString.append(" AND (string_to_array("+ dataPermission.tableAlias() + "."+ tableFieldList.get(0) +",',') && string_to_array('" + StringUtils.join(list,",") + "',',')");
                    if (tableFieldSize > 1) {
                        sqlString.append(" OR ");
                        for (int i = 1; i < tableFieldSize; i++) {
                            sqlString.append("string_to_array("+ dataPermission.tableAlias() + "."+ tableFieldList.get(i) +",',') && string_to_array('" + StringUtils.join(list,",") + "',','))");
                        }
                    }
                }

            } else {
                if (tableFieldSize == 1) {
                    sqlString.append(" AND string_to_array("+ dataPermission.tableAlias() + "."+ tableFieldList.get(0) +",',') && string_to_array('" + user.getUid() + "',',')");
                } else {
                    sqlString.append(" AND (string_to_array("+ dataPermission.tableAlias() + "."+ tableFieldList.get(0) +",',') && string_to_array('" + user.getUid() + "',',')");
                    if (tableFieldSize > 1) {
                        sqlString.append(" OR ");
                        for (int i = 1; i < tableFieldSize; i++) {
                            sqlString.append("string_to_array("+ dataPermission.tableAlias() + "."+ tableFieldList.get(i) +",',') && string_to_array('" + user.getUid() + "',','))");
                        }
                    }
                }
            }
            //like any (array['%1582313948525367297%','%1549948476757303297%'])
        } else if (DATA_SCOPE_SELF.equals(userRequestPermissions.getDataScope())) {
            if (tableFieldSize == 1) {
                sqlString.append(" AND string_to_array("+ dataPermission.tableAlias() + "."+ tableFieldList.get(0) +",',') && string_to_array('" + user.getUid() + "',',')");
            } else {
                sqlString.append(" AND (string_to_array("+ dataPermission.tableAlias() + "."+ tableFieldList.get(0) +",',') && string_to_array('" + user.getUid() + "',',')");
                if (tableFieldSize > 1) {
                    sqlString.append(" OR ");
                    for (int i = 1; i < tableFieldSize; i++) {
                        sqlString.append("string_to_array("+ dataPermission.tableAlias() + "."+ tableFieldList.get(i) +",',') && string_to_array('" + user.getUid() + "',','))");
                    }
                }
            }
        }
        ObjectUtils.setFieldValue(params[inject.index()], inject.param(), sqlString.toString());
    }

    /**
     * 删除
     *
     * @param userRequestPermissions 权限列表
     * @param userList               部门用户列表
     * @param user                   用户信息
     * @param dataPermission         自定义注解信息
     * @return java.lang.String
     * @Author Luo_WG
     * @Date 2022/10/21 9:57
     **/
    public void delete(JoinPoint joinPoint, UserRequestPermissionsDTO userRequestPermissions, List<String> userList, LoginUser user, DataPermission dataPermission) {
        Class<? extends IService> serviceClass = dataPermission.serviceClass();
        IService<?> service = getIservice(joinPoint, serviceClass.getName());

        Object obj = joinPoint.getArgs()[0];

        List<Object> objList = new ArrayList<>();
        if (obj instanceof List) {
            objList = (List<Object>) obj;
        } else if (obj instanceof String[]) {
            objList = Arrays.asList((String[]) joinPoint.getArgs()[0]);
        } else if (obj instanceof String) {
            objList = Collections.singletonList(obj);
        } else if (obj instanceof Map) {
            Map mapParam = (Map) obj;
        }

        List<String> inputIdList = new ArrayList<>();
        if (obj instanceof String) {
            inputIdList.add(String.valueOf(obj));
        } else {

            Map<String, Object> mapParam = JSONObject.parseObject(JSONObject.toJSONString(obj), Map.class);
            Object o = null;
            if (StringUtils.isNotBlank(dataPermission.entityName())) {
                Object entity = mapParam.get(dataPermission.entityName());
                o = JSONObject.parseObject(JSONObject.toJSONString(entity)).get(dataPermission.keyIdName());
            } else {
                o = mapParam.get(dataPermission.keyIdName());
            }
            if (o != null) {
                if (o instanceof List) {
                    inputIdList = (List<String>) o;
                } else if (o instanceof String) {
                    inputIdList.add(String.valueOf(o));
                }
            }
        }

        if (CollectionUtils.isEmpty(inputIdList)) {
            return;
        }
        List<String> users = new ArrayList<>();
        List<?> objects = service.listByIds(inputIdList);
        for (Object object : objects) {
            JSONObject jsonObject = JSONObject.parseObject(JSONObject.toJSONString(object));
            Object o = jsonObject.get(StrUtils.underlineToCamel(dataPermission.tableField(), true));
            if (o == null) {
                return;
            }
            users.addAll(Arrays.asList(o.toString().split(",")));
        }
        if (DATA_SCOPE_ALL.equals(userRequestPermissions.getDataScope())) {
            return;
        } else if (DATA_SCOPE_DEPT.equals(userRequestPermissions.getDataScope())) {
            if (!userList.containsAll(users)) {
                throw new ServiceException(ApiError.ERROR_1013);
            }
        } else if (DATA_SCOPE_SELF.equals(userRequestPermissions.getDataScope())) {
            if (!users.contains(user.getUid())) {
                throw new ServiceException(ApiError.ERROR_1013);
            }

        }
    }

    /**
     * 修改
     *
     * @param userRequestPermissions 权限列表
     * @param userList               部门用户列表
     * @param user                   用户信息
     * @param dataPermission         自定义注解信息
     * @return java.lang.String
     * @Author Luo_WG
     * @Date 2022/10/21 9:57
     **/
    public void update(JoinPoint joinPoint, UserRequestPermissionsDTO userRequestPermissions, List<String> userList, LoginUser user, DataPermission dataPermission) {
        Class<? extends IService> serviceClass = dataPermission.serviceClass();
        IService<?> service = getIservice(joinPoint, serviceClass.getName());

        Object obj = joinPoint.getArgs()[0];

        List<Object> objList = new ArrayList<>();
        if (obj instanceof List) {
            objList = (List<Object>) obj;
        } else if (obj instanceof String[]) {
            objList = Arrays.asList((String[]) joinPoint.getArgs()[0]);
        } else if (obj instanceof String) {
            objList = Collections.singletonList(obj);
        } else if (obj instanceof Map) {
            Map mapParam = (Map) obj;
        }

        List<String> inputIdList = new ArrayList<>();
        if (obj instanceof String) {
            inputIdList.add(String.valueOf(obj));
        } else {

            Map<String, Object> mapParam = JSONObject.parseObject(JSONObject.toJSONString(obj), Map.class);
            Object o = null;
            if (StringUtils.isNotBlank(dataPermission.entityName())) {
                Object entity = mapParam.get(dataPermission.entityName());
                o = JSONObject.parseObject(JSONObject.toJSONString(entity)).get(dataPermission.keyIdName());
            } else {
                o = mapParam.get(dataPermission.keyIdName());
            }
            if (o != null) {
                if (o instanceof List) {
                    inputIdList = (List<String>) o;
                } else if (o instanceof String) {
                    inputIdList.add(String.valueOf(o));
                }
            }
        }

        if (CollectionUtils.isEmpty(inputIdList)) {
            return;
        }
        List<String> users = new ArrayList<>();
        List<?> objects = service.listByIds(inputIdList);
        for (Object object : objects) {
            JSONObject jsonObject = JSONObject.parseObject(JSONObject.toJSONString(object));
            Object o = jsonObject.get(StrUtils.underlineToCamel(dataPermission.tableField(), true));
            if (o == null) {
                return;
            }
            users.addAll(Arrays.asList(o.toString().split(",")));
        }

        if (DATA_SCOPE_ALL.equals(userRequestPermissions.getDataScope())) {
            return;
        } else if (DATA_SCOPE_DEPT.equals(userRequestPermissions.getDataScope())) {
            if (!userList.containsAll(users)) {
                throw new ServiceException(ApiError.ERROR_1013);
            }
        } else if (DATA_SCOPE_SELF.equals(userRequestPermissions.getDataScope())) {
            if (!users.contains(user.getUid())) {
                throw new ServiceException(ApiError.ERROR_1013);
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
