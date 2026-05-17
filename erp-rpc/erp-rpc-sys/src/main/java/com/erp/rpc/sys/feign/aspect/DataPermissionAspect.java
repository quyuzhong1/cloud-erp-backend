package com.erp.rpc.sys.feign.aspect;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import com.common.business.annotation.DataPermission;
import com.common.business.dto.UserRequestPermissionsDTO;
import com.common.business.enums.DynamicDataSourceTypeEnum;
import com.common.business.threadlocal.DynamicDataSourceThreadLocal;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.ObjectUtils;
import com.common.core.utils.SqlUtils;
import com.common.core.utils.StrUtils;
import com.erp.model.sys.dto.DataPermissionContextDTO;
import com.erp.model.sys.dto.SysUserDTO;
import com.erp.rpc.sys.feign.dataperm.FeignDataPermissionContextResolver;
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
import java.util.stream.Collectors;

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

    /**
     * 数据权限上下文解析器：一次聚合 Feign + 60s 本地 TTL 缓存，替代历史 5 次散打 Feign。
     *
     * <p>性能：单接口对 sys 的 Feign 调用由 <b>5 次</b> 降为 <b>≤1 次</b>，
     * 命中本地 cache 时 <b>0 次</b>。500 QPS × 10 Pod 场景下 sys QPS 降至原来 1% 以内。</p>
     */
    @Resource
    private FeignDataPermissionContextResolver dataPermissionContextResolver;

    @Resource
    private ApplicationContext applicationContext;

    // 配置织入点
    @Pointcut("@annotation(com.common.business.annotation.DataPermission)")
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
        LoginUser userInfo = UserContext.getLoginUser();
        if(Objects.isNull(userInfo)){
            userInfo=new LoginUser();
            userInfo.setUid("1549948476757303297");
        }
        if(ObjectUtil.isEmpty(userInfo) || StringUtils.isBlank(userInfo.getUid())){
            throw new ServiceException(ApiError.HTTP_UNAUTHORIZED);
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
        // 一次聚合 Feign 拿全 5 类数据（命中本地 60s cache 时 0 Feign），替代历史 5 次散打 Feign
        DataPermissionContextDTO ctx = dataPermissionContextResolver.resolve(user.getUid());
        List<UserRequestPermissionsDTO> requestPermissionsList = ctx.getPermissionsList();
        List<String> roleIdList = ctx.getRoleIdList();
        List<String> userList = ctx.getDepUserList();
        List<SysUserDTO.ShopDTO> shopUserList = ctx.getShopUserList();
        List<SysUserDTO.WarehouseDTO> warehouseUserList = ctx.getWarehouseUserList();

        UserRequestPermissionsDTO userRequestPermissions = new UserRequestPermissionsDTO();
        if (roleIdList != null && roleIdList.contains("1")) {
            userRequestPermissions.setPermissionsCode(controllerDataScope.menuCode());
            userRequestPermissions.setDataScope(DATA_SCOPE_ALL);
        } else {
            userRequestPermissions = requestPermissionsList
                    .stream()
                    .filter(p -> p.getPermissionsCode().equals(controllerDataScope.menuCode()))
                    .findFirst()
                    .orElseThrow(() -> new ServiceException(ApiError.HTTP_FORBIDDEN));
        }

        switch (controllerDataScope.operationType()) {
            case LIST:
                query(joinPoint, userRequestPermissions, userList, user, controllerDataScope,shopUserList,warehouseUserList);
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
     * @param shopUserList
     * @param warehouseUserList
     * @return java.lang.String
     * @Author Luo_WG
     * @Date 2022/10/21 9:57
     **/
    public void query(JoinPoint joinPoint, UserRequestPermissionsDTO userRequestPermissions, List<String> userList, LoginUser user, DataPermission dataPermission,
                      List<SysUserDTO.ShopDTO> shopUserList, List<SysUserDTO.WarehouseDTO> warehouseUserList) {
        Object[] params = joinPoint.getArgs();
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        DataPermission inject = method.getAnnotation(DataPermission.class);
        String tableFields = dataPermission.tableField();
        StringBuilder sqlString = new StringBuilder();
        DynamicDataSourceTypeEnum dynamicDataSourceTypeEnum = DynamicDataSourceThreadLocal.get();
        boolean isDoris = DynamicDataSourceTypeEnum.isDoris(dynamicDataSourceTypeEnum);
        if (CharSequenceUtil.isNotBlank(tableFields)) {
            //字段名称
            List<String> tableFieldList = Arrays.asList(tableFields.split(","));
            int tableFieldSize = tableFieldList.size();
            //表别名
            String tableAlias = dataPermission.tableAlias();
            List<String> tableAliasList = Arrays.asList(tableAlias.split(","));
            boolean flag = tableFieldList.size() == tableAliasList.size();
            if (DATA_SCOPE_ALL.equals(userRequestPermissions.getDataScope())) {
                sqlString.append(" AND  1=1");
            } else if (DATA_SCOPE_DEPT.equals(userRequestPermissions.getDataScope())) {
                if (CollectionUtils.isNotEmpty(userList)) {
                	if(isDoris) {
                		if (tableFieldSize == 1) {
                            sqlString.append(" AND ");
                            SqlUtils.appendPermissionSql(sqlString, tableAliasList.get(0) + "." + tableFieldList.get(0), userList);
                        } else {
                        	sqlString.append(" AND (");
                        	SqlUtils.appendPermissionSql(sqlString, tableAliasList.get(0) + "." + tableFieldList.get(0), userList);
                            if (tableFieldSize > 1) {
                                sqlString.append(" OR ");
                                for (int i = 1; i < tableFieldSize; i++) {
                                	SqlUtils.appendPermissionSql(sqlString, (flag ? tableAliasList.get(i) : tableAliasList.get(0)) + "." + tableFieldList.get(i), userList);
                                }
                            }
                            sqlString.append(")");
                        }
                	}else {
                		if (tableFieldSize == 1) {
                            sqlString.append(" AND string_to_array(").append(tableAliasList.get(0)).append(".").append(tableFieldList.get(0)).append(",',') && string_to_array('").append(StringUtils.join(userList, ",")).append("',',')");
                        } else {
                            sqlString.append(" AND (string_to_array(").append(tableAliasList.get(0)).append(".").append(tableFieldList.get(0)).append(",',') && string_to_array('").append(StringUtils.join(userList, ",")).append("',',')");
                            if (tableFieldSize > 1) {
                                sqlString.append(" OR ");
                                for (int i = 1; i < tableFieldSize; i++) {
                                    sqlString.append("string_to_array(").append(flag ? tableAliasList.get(i) : tableAliasList.get(0)).append(".").append(tableFieldList.get(i)).append(",',') && string_to_array('").append(StringUtils.join(userList, ",")).append("',','))");
                                }
                            }
                        }
                	}
                } else {
                	if(isDoris) {
                		if (tableFieldSize == 1) {
                        	sqlString.append(" AND ");
                            SqlUtils.appendPermissionSql(sqlString, tableAliasList.get(0) + "." + tableFieldList.get(0), Arrays.asList(user.getUid()));
                        } else {
                        	sqlString.append(" AND (");
                        	SqlUtils.appendPermissionSql(sqlString, tableAliasList.get(0) + "." + tableFieldList.get(0), Arrays.asList(user.getUid()));
                            if (tableFieldSize > 1) {
                                sqlString.append(" OR ");
                                for (int i = 1; i < tableFieldSize; i++) {
                                	SqlUtils.appendPermissionSql(sqlString, (flag ? tableAliasList.get(i) : tableAliasList.get(0)) + "." + tableFieldList.get(i), Arrays.asList(user.getUid()));
                                }
                            }
                            sqlString.append(")");
                        }
                	}else {
                		if (tableFieldSize == 1) {
                            sqlString.append(" AND string_to_array(").append(tableAliasList.get(0)).append(".").append(tableFieldList.get(0)).append(",',') && string_to_array('").append(user.getUid()).append("',',')");
                        } else {
                            sqlString.append(" AND (string_to_array(").append(tableAliasList.get(0)).append(".").append(tableFieldList.get(0)).append(",',') && string_to_array('").append(user.getUid()).append("',',')");
                            if (tableFieldSize > 1) {
                                sqlString.append(" OR ");
                                for (int i = 1; i < tableFieldSize; i++) {
                                    sqlString.append("string_to_array(").append(flag ? tableAliasList.get(i) : tableAliasList.get(0)).append(".").append(tableFieldList.get(i)).append(",',') && string_to_array('").append(user.getUid()).append("',','))");
                                }
                            }
                        }
                	}
                }
            } else if (DATA_SCOPE_SELF.equals(userRequestPermissions.getDataScope())) {
            	if(isDoris) {
            		if (tableFieldSize == 1) {
                    	sqlString.append(" AND ");
                        SqlUtils.appendPermissionSql(sqlString, tableAliasList.get(0) + "." + tableFieldList.get(0), Arrays.asList(user.getUid()));
                    } else {
                    	sqlString.append(" AND (");
                    	SqlUtils.appendPermissionSql(sqlString, tableAliasList.get(0) + "." + tableFieldList.get(0), Arrays.asList(user.getUid()));
                        if (tableFieldSize > 1) {
                            sqlString.append(" OR ");
                            for (int i = 1; i < tableFieldSize; i++) {
                            	SqlUtils.appendPermissionSql(sqlString, (flag ? tableAliasList.get(i) : tableAliasList.get(0)) + "." + tableFieldList.get(i), Arrays.asList(user.getUid()));
                            }
                        }
                        sqlString.append(")");
                    }
            	}else {
            		if (tableFieldSize == 1) {
                        sqlString.append(" AND string_to_array(").append(tableAliasList.get(0)).append(".").append(tableFieldList.get(0)).append(",',') && string_to_array('").append(user.getUid()).append("',',')");
                    } else {
                        sqlString.append(" AND (string_to_array(").append(tableAliasList.get(0)).append(".").append(tableFieldList.get(0)).append(",',') && string_to_array('").append(user.getUid()).append("',',')");
                        if (tableFieldSize > 1) {
                            sqlString.append(" OR ");
                            for (int i = 1; i < tableFieldSize; i++) {
                                sqlString.append("string_to_array(").append(flag ? tableAliasList.get(i) : tableAliasList.get(0)).append(".").append(tableFieldList.get(i)).append(",',') && string_to_array('").append(user.getUid()).append("',','))");
                            }
                        }
                    }
            	}
            }
        }
        //店铺
        String shopTableField = dataPermission.shopTableField();
        if (CharSequenceUtil.isNotBlank(shopTableField)){
            List<String> shopTableFieldList = Arrays.asList(shopTableField.split(","));
            if (CollectionUtils.isNotEmpty(shopUserList)) {
                String authType = shopUserList.stream().map(SysUserDTO.ShopDTO::getAuthType).filter("all"::equals).findFirst().orElse("part");
                if ("part".equals(authType)){
                    List<String> shopIdList = shopUserList.stream().map(SysUserDTO.ShopDTO::getShopId).distinct().collect(Collectors.toList());
                    SqlUtils.appendBlankOrInPermissionSql(sqlString, shopTableFieldList, shopIdList);
                }
            }
        }
        //仓库
        String warehouseTableField = dataPermission.warehouseTableField();
        if (CharSequenceUtil.isNotBlank(warehouseTableField)){
            List<String> warehouseTableFieldList = Arrays.asList(warehouseTableField.split(","));
            if (CollectionUtils.isNotEmpty(warehouseUserList)) {
                String authType = warehouseUserList.stream().map(SysUserDTO.WarehouseDTO::getAuthType).filter("all"::equals).findFirst().orElse("part");
                if ("part".equals(authType)){
                    List<String> warehouseIdList = warehouseUserList.stream().map(SysUserDTO.WarehouseDTO::getWarehouseId).distinct().collect(Collectors.toList());
                    SqlUtils.appendBlankOrInPermissionSql(sqlString, warehouseTableFieldList, warehouseIdList);
                }
            }
        }
        if (CharSequenceUtil.isNotBlank(sqlString.toString())){
            ObjectUtils.setFieldValue(params[inject.index()], inject.permissionSql(), sqlString.toString());
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
            if (StringUtils.isBlank(dataPermission.tableField())) {
                return;
            }
            List<String> tableFieldList = Arrays.asList(dataPermission.tableField().split(","));
            for (String field : tableFieldList) {
                Object o = jsonObject.get(StrUtils.underlineToCamel(field, true));
                if (o == null) {
                    continue;
                }
                users.addAll(Arrays.asList(o.toString().split(",")));
            }
        }
        if (DATA_SCOPE_ALL.equals(userRequestPermissions.getDataScope())) {
            return;
        } else if (DATA_SCOPE_DEPT.equals(userRequestPermissions.getDataScope())) {
            long containsUserCount = users.stream().filter(u -> userList.contains(u)).count();
            if (containsUserCount == 0) {
                throw new ServiceException(ApiError.HTTP_FORBIDDEN);
            }
        } else if (DATA_SCOPE_SELF.equals(userRequestPermissions.getDataScope())) {
            if (!users.contains(user.getUid())) {
                throw new ServiceException(ApiError.HTTP_FORBIDDEN);
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
        } else if (obj instanceof List) {
            List<Object> list = (List<Object>) obj;
            for (Object object : list) {
                Map<String, Object> mapParam = JSONObject.parseObject(JSONObject.toJSONString(object), Map.class);
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

            if (StringUtils.isBlank(dataPermission.tableField())) {
                return;
            }
            String[] tableFields = dataPermission.tableField().split(",");
            for (String tableField : tableFields) {
                Object o = jsonObject.get(StrUtils.underlineToCamel(tableField, true));
                if (o == null) {
                    continue;
                }
                users.addAll(Arrays.asList(o.toString().split(",")));
            }
        }

        if (DATA_SCOPE_ALL.equals(userRequestPermissions.getDataScope())) {
            return;
        } else if (DATA_SCOPE_DEPT.equals(userRequestPermissions.getDataScope())) {
            long containsUserCount = users.stream().filter(u -> userList.contains(u)).count();
            if (containsUserCount == 0) {
                throw new ServiceException(ApiError.HTTP_FORBIDDEN);
            }
        } else if (DATA_SCOPE_SELF.equals(userRequestPermissions.getDataScope())) {
            if (!users.contains(user.getUid())) {
                throw new ServiceException(ApiError.HTTP_FORBIDDEN);
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
