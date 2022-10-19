package com.erp.server.plm.aspect;

import com.erp.common.annotation.DataPermision;
import com.erp.common.dto.base.ApiResult;
import com.erp.common.dto.base.PermissionsDTO;
import com.erp.common.modules.sys.dto.FindUserDTO;
import com.erp.common.modules.sys.dto.SysUserDTO;
import com.erp.common.modules.sys.dto.UserRequestPermissionsDTO;
import com.erp.common.vo.LoginUser;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.plm.service.CommonService;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据过滤处理
 */
@Aspect
@Component
public class DataPermisionAspect {
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
    @Pointcut("@annotation(com.erp.common.annotation.DataPermision)")
    public void dataScopePointCut(){
    }

    @Before("dataScopePointCut()")
    public void doBefore(JoinPoint point) throws Throwable {
        handleDataScope(point);
    }

    protected void handleDataScope(final JoinPoint joinPoint) {
        // 获得注解
        DataPermision controllerDataScope = getAnnotationLog(joinPoint);
        if (controllerDataScope == null) {
            return;
        }
        LoginUser userInfo = commonService.getUserInfo();
        userInfo.setUid("1545306732634984450");
        //当用户id 不为空的时候
        if (StringUtils.isNotBlank(userInfo.getUid())) {

        }
        dataScopeFilter(joinPoint, userInfo, controllerDataScope.field(),
                controllerDataScope.menuCode());
    }

    /**
     * 是否存在注解，如果存在就获取
     */
    private DataPermision getAnnotationLog(JoinPoint joinPoint) {
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();
        if (method != null){
            return method.getAnnotation(DataPermision.class);
        }
        return null;
    }

    /**
     * 数据范围过滤
     *
     * @param joinPoint 切点
     * @param user 用户
     * @param field 字段
     * @param menuCode 用户别名
     */
    public void dataScopeFilter(JoinPoint joinPoint, LoginUser user, String field, String menuCode) {
        StringBuilder sqlString = new StringBuilder();
        List<UserRequestPermissionsDTO> requestPermissionsList = sysUserFeign.getRequestPermissionsList(user.getUid());
        List<SysUserDTO> depUserList = sysUserFeign.getDepUserList(user.getUid());
        List<String> userList = new ArrayList<>();
       // List<String> depUserList = depUserList1.getData();
        for (SysUserDTO sysUserDTO : depUserList) {
            userList.add(sysUserDTO.getUid());
        }
        System.out.println(userList);
        for (UserRequestPermissionsDTO role : requestPermissionsList) {
            if (DATA_SCOPE_ALL.equals(role.getDataScope())) {
                sqlString = new StringBuilder();
                break;
            } else if (DATA_SCOPE_DEPT.equals(role.getDataScope())) {
                sqlString.append(" AND " + field + " in (" + StringUtils.join(userList, ",") + ")");
            } else if (DATA_SCOPE_SELF.equals(role.getDataScope())) {
                sqlString.append(" AND " + field + " = " + user.getUid() + " ");
            }
        }

        if (StringUtils.isNotBlank(sqlString.toString())) {
            // 拿到方法的参数，要求第一个参数为实体类且继承PermissionsDTO，因为要将拼接的sql保存PermissionsDTO的param属性上
            Object params = joinPoint.getArgs()[0];
            if (params != null && params instanceof PermissionsDTO) {
                PermissionsDTO baseEntity = (PermissionsDTO) params;
                baseEntity.getParam().put(DATA_SCOPE, sqlString);
            }
        }
    }

}
