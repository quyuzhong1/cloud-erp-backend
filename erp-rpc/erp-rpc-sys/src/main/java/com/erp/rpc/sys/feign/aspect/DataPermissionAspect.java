package com.erp.rpc.sys.feign.aspect;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import com.common.business.annotation.DataPermission;
import com.common.business.dto.UserRequestPermissionsDTO;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.ObjectUtils;
import com.common.core.utils.StrUtils;
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
    public static final String SQL_HEAD = " AND string_to_array(";
    public static final String SQL_LINK = ",',') && string_to_array('";
    public static final String SQL_LAST = "',',')";
    public static final String SQL_HEAD_1 = " AND (string_to_array(";
    public static final String SQL_HEAD_2 = "string_to_array(";
    public static final String SQL_LAST_2 = "',','))";

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private ApplicationContext applicationContext;

    // 配置织入点
    @Pointcut("@annotation(com.common.business.annotation.DataPermission)")
    public void dataScopePointCut() {
    }

    @Before("dataScopePointCut()")
    public void doBefore(JoinPoint point) {
        handleDataScope(point);
    }

    protected void handleDataScope(final JoinPoint joinPoint) {
        // 获得注解
        DataPermission controllerDataScope = getAnnotationLog(joinPoint);
        if (controllerDataScope == null) {
            return;
        }
        LoginUser userInfo = UserContext.getLoginUser();
        if(Objects.isNull(userInfo)){
            userInfo=new LoginUser();
            userInfo.setUid("1549948476757303297");
        }
        if(ObjectUtil.isEmpty(userInfo) || StringUtils.isBlank(userInfo.getUid())){
            throw new ServiceException(ApiError.ERROR_403);
        }
        dataScopeFilter(joinPoint, userInfo, controllerDataScope);
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
            userRequestPermissions = requestPermissionsList
                    .stream()
                    .filter(p -> p.getPermissionsCode().equals(controllerDataScope.menuCode()))
                    .findFirst()
                    .orElseThrow(() -> new ServiceException(ApiError.NO_PERMISSION));
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
        //字段名称
        List<String> tableFieldList = Arrays.asList(tableFields.split(","));
        int tableFieldSize = tableFieldList.size();
        //表别名
        String tableAlias = dataPermission.tableAlias();
        List<String> tableAliasList = Arrays.asList(tableAlias.split(","));
        boolean flag = tableFieldList.size() == tableAliasList.size();

        StringBuilder sqlString = new StringBuilder();
        if (DATA_SCOPE_ALL.equals(userRequestPermissions.getDataScope())) {
            return;
        } else if (DATA_SCOPE_DEPT.equals(userRequestPermissions.getDataScope())) {

            handleScope(userList, user, tableFieldSize, sqlString, tableAliasList, tableFieldList, flag);

        } else if (DATA_SCOPE_SELF.equals(userRequestPermissions.getDataScope())) {
            handleScopeSelf(tableFieldSize, sqlString, tableAliasList, tableFieldList, user.getUid(), flag);
        }
        ObjectUtils.setFieldValue(params[inject.index()], inject.permissionSql(), sqlString.toString());
    }

    private static void handleScopeSelf(int tableFieldSize, StringBuilder sqlString, List<String> tableAliasList, List<String> tableFieldList, String user, boolean flag) {
        if (tableFieldSize == 1) {
            sqlString.append(SQL_HEAD + tableAliasList.get(0) + "." + tableFieldList.get(0) + SQL_LINK + user + SQL_LAST);
        } else {
            sqlString.append(SQL_HEAD_1 + tableAliasList.get(0) + "." + tableFieldList.get(0) + SQL_LINK + user + SQL_LAST);
            if (tableFieldSize > 1) {
                sqlString.append(" OR ");
                for (int i = 1; i < tableFieldSize; i++) {
                    sqlString.append(SQL_HEAD_2 + (flag ? tableAliasList.get(i) : tableAliasList.get(0)) + "." + tableFieldList.get(i) + SQL_LINK + user + SQL_LAST_2);
                }
            }
        }
    }

    private static void handleScope(List<String> userList, LoginUser user, int tableFieldSize, StringBuilder sqlString, List<String> tableAliasList, List<String> tableFieldList, boolean flag) {
        List<String> list = new ArrayList<>();
        for (String s : userList) {
            list.add(s);
        }
        if (CollectionUtils.isNotEmpty(list)) {
            handleScopeSelf(tableFieldSize, sqlString, tableAliasList, tableFieldList, StringUtils.join(list, ","), flag);

        } else {
            handleScopeSelf(tableFieldSize, sqlString, tableAliasList, tableFieldList, user.getUid(), flag);
        }
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

        List<String> inputIdList = new ArrayList<>();
        if (obj instanceof String) {
            inputIdList.add(String.valueOf(obj));
        } else {

            inputIdList = handleOther(dataPermission, obj, inputIdList);
        }

        if (CollectionUtils.isEmpty(inputIdList)) {
            return;
        }
        if (service == null) {
            throw new ServiceException("未找到对应的service类");
        }
        List<String> users = new ArrayList<>();
        List<?> objects = service.listByIds(inputIdList);
        for (Object object : objects) {
            JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(object));
            Object o = jsonObject.get(StrUtils.underlineToCamel(dataPermission.tableField(), true));
            if (o == null) {
                return;
            }
            users.addAll(Arrays.asList(o.toString().split(",")));
        }
        if (DATA_SCOPE_ALL.equals(userRequestPermissions.getDataScope())) {
            return;
        } else if (DATA_SCOPE_DEPT.equals(userRequestPermissions.getDataScope())) {
            long containsUserCount = users.stream().filter(userList::contains).count();
            if (containsUserCount == 0) {
                throw new ServiceException(ApiError.NO_PERMISSION);
            }
        } else if (DATA_SCOPE_SELF.equals(userRequestPermissions.getDataScope()) && !users.contains(user.getUid())) {
            throw new ServiceException(ApiError.NO_PERMISSION);
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


        List<String> inputIdList = new ArrayList<>();
        if (obj instanceof String) {
            inputIdList.add(String.valueOf(obj));
        } else if (obj instanceof List) {
            inputIdList = handlePermissionList(dataPermission, (List<Object>) obj, inputIdList);
        } else {

            inputIdList = handleOther(dataPermission, obj, inputIdList);
        }

        if (CollectionUtils.isEmpty(inputIdList)) {
            return;
        }
        if (service == null) {
            throw new ServiceException("未找到对应的service类");
        }
        List<String> users = new ArrayList<>();
        List<?> objects = service.listByIds(inputIdList);
        for (Object object : objects) {
            if (handleUpdateObject(dataPermission, object, users)) return;
        }

        if (DATA_SCOPE_ALL.equals(userRequestPermissions.getDataScope())) {
            return;
        } else if (DATA_SCOPE_DEPT.equals(userRequestPermissions.getDataScope())) {
            long containsUserCount = users.stream().filter(userList::contains).count();
            if (containsUserCount == 0) {
                throw new ServiceException(ApiError.NO_PERMISSION);
            }
        } else if (DATA_SCOPE_SELF.equals(userRequestPermissions.getDataScope())&&!users.contains(user.getUid())) {
            throw new ServiceException(ApiError.NO_PERMISSION);
        }
    }

    private static boolean handleUpdateObject(DataPermission dataPermission, Object object, List<String> users) {
        JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(object));

        if (StringUtils.isBlank(dataPermission.tableField())) {
            return true;
        }
        String[] tableFields = dataPermission.tableField().split(",");
        for (String tableField : tableFields) {
            Object o = jsonObject.get(StrUtils.underlineToCamel(tableField, true));
            if (o == null) {
                continue;
            }
            users.addAll(Arrays.asList(o.toString().split(",")));
        }
        return false;
    }

    private static List<String> handleOther(DataPermission dataPermission, Object obj, List<String> inputIdList) {
        Map<String, Object> mapParam = JSON.parseObject(JSON.toJSONString(obj), Map.class);
        Object o = null;
        if (StringUtils.isNotBlank(dataPermission.entityName())) {
            Object entity = mapParam.get(dataPermission.entityName());
            o = JSON.parseObject(JSON.toJSONString(entity)).get(dataPermission.keyIdName());
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
        return inputIdList;
    }

    private static List<String> handlePermissionList(DataPermission dataPermission, List<Object> obj, List<String> inputIdList) {
        List<Object> list = obj;
        for (Object object : list) {
            inputIdList = handleOther(dataPermission, object, inputIdList);
        }
        return inputIdList;
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
