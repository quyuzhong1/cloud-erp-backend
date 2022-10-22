package com.erp.server.plm.aspect;

import com.common.core.utils.ObjectUtils;
import com.erp.common.annotation.DataPermission;
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
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
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

    /**
     * 数据权限过滤关键字
     */
    public static final String DATA_SCOPE = "DataPermision";

    @Resource
    private CommonService commonService;

    @Resource
    private SysUserFeign sysUserFeign;

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
        userInfo.setUid("1545306732634984450");
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

        List<SysUserDTO> depUserList = sysUserFeign.getDepUserList(user.getUid());
        List<String> userList = new ArrayList<>();
        for (SysUserDTO sysUserDTO : depUserList) {
            userList.add(sysUserDTO.getUid());
        }
        Object[] params = joinPoint.getArgs();
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        DataPermission inject = method.getAnnotation(DataPermission.class);

/*        //sql的拼接
        String sqlParam = "";
        if (controllerDataScope.operationType().equals("query")) {
            sqlParam = query(userRequestPermissionsDTOStream, userList, user, controllerDataScope);
        } else if (controllerDataScope.operationType().equals("delete")) {

        } else if (controllerDataScope.operationType().equals("update")) {

        }*/
        String sqlParam = "";
        switch (controllerDataScope.operationType()) {
            case "query":
                sqlParam = query(joinPoint, userRequestPermissionsDTOStream, userList, user, controllerDataScope);
                break;
            case "delete":

                break;
            case "update":

                break;
            default:
                break;
        }
        StringBuilder sqlString = new StringBuilder();
        for (UserRequestPermissionsDTO role : userRequestPermissionsDTOStream) {
            if (DATA_SCOPE_ALL.equals(role.getDataScope())) {
                sqlString = new StringBuilder();
                break;
            } else if (DATA_SCOPE_DEPT.equals(role.getDataScope())) {
                sqlString.append(" AND " + controllerDataScope.tableField() + " IN (" + StringUtils.join(userList, ",") + ")");
            } else if (DATA_SCOPE_SELF.equals(role.getDataScope())) {
                sqlString.append(" AND " + controllerDataScope.tableField() + " = " + user.getUid() + " ");
            }

        }


        //这个是给那个字段赋值
        if (StringUtils.isNotBlank(sqlString.toString())) {
            if(params.length > 0){

            }
        }
    }

    /**
     *
     * @Author Luo_WG
     * @Date 2022/10/21 9:57
     * @param joinPoint 切点信息
     * @param userRequestPermissionsDTOStream 权限列表
     * @param userList 部门用户列表
     * @param user 用户信息
     * @param controllerDataScope 自定义注解信息
     * @return java.lang.String
     **/
    public String query(JoinPoint joinPoint, List<UserRequestPermissionsDTO> userRequestPermissionsDTOStream, List<String> userList, LoginUser user, DataPermission controllerDataScope) {
        Object[] params = joinPoint.getArgs();
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        DataPermission inject = method.getAnnotation(DataPermission.class);

        StringBuilder sqlString = new StringBuilder();
        for (UserRequestPermissionsDTO role : userRequestPermissionsDTOStream) {
            if (DATA_SCOPE_ALL.equals(role.getDataScope())) {
                sqlString = new StringBuilder();
                break;
            } else if (DATA_SCOPE_DEPT.equals(role.getDataScope())) {
                sqlString.append(" AND " + controllerDataScope.tableField() + " in (" + StringUtils.join(userList, ",") + ")");
            } else if (DATA_SCOPE_SELF.equals(role.getDataScope())) {
                sqlString.append(" AND " + controllerDataScope.tableField() + " = " + user.getUid() + " ");
            }
        }
        ObjectUtils.setFieldValue(params[inject.index()], inject.param(), sqlString.toString());
        return sqlString.toString();
    }

    /**
     *
     * @Author Luo_WG
     * @Date 2022/10/21 9:57
     * @param userRequestPermissionsDTOStream 权限列表
     * @param userList 部门用户列表
     * @param user 用户信息
     * @param controllerDataScope 自定义注解信息
     * @return java.lang.String
     **/
    public String delete(List<UserRequestPermissionsDTO> userRequestPermissionsDTOStream, List<String> userList, LoginUser user, DataPermission controllerDataScope) {
        StringBuilder sqlString = new StringBuilder();
        for (UserRequestPermissionsDTO role : userRequestPermissionsDTOStream) {
            if (DATA_SCOPE_ALL.equals(role.getDataScope())) {
                sqlString = new StringBuilder();
                break;
            } else if (DATA_SCOPE_DEPT.equals(role.getDataScope())) {
                sqlString.append(" AND " + controllerDataScope.tableField() + " in (" + StringUtils.join(userList, ",") + ")");
            } else if (DATA_SCOPE_SELF.equals(role.getDataScope())) {
                sqlString.append(" AND " + controllerDataScope.tableField() + " = " + user.getUid() + " ");
            }
        }
        return sqlString.toString();
    }
}
