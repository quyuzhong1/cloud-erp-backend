package com.erp.server.sys.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.common.business.dto.UserRequestPermissionsDTO;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.sys.service.CommonService;
import com.erp.server.sys.service.SysRoleUserService;
import com.erp.server.sys.service.SysUserInfoService;
import com.erp.server.sys.service.UserDatePermissionService;
import io.seata.common.util.CollectionUtils;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class UserDatePermissionServiceImpl implements UserDatePermissionService {
    @Resource
    private SysRoleUserService sysRoleUserService;
    @Resource
    private SysUserInfoService sysUserInfoService;
    @Resource
    private SysUserFeign sysUserFeign;

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
     * 查询用户数据权限,获取到权限sql
     * @param tableField 权限过滤字段
     * @param menuCode 菜单编号
     * @return java.lang.String
     */
    public String getUserDatePermissionSql(String tableField, String menuCode) {
        if ("plm:bom:paging".equals(menuCode)) {
            System.out.println("1");
        }
        if (StringUtil.isBlank(tableField) || StringUtil.isBlank(menuCode)) {
            return "";
        }
        LoginUser user = UserContext.getDefaultLoginUser();
        List<UserRequestPermissionsDTO> requestPermissionsList = sysUserFeign.getRequestPermissionsList(user.getUid());
        UserRequestPermissionsDTO userRequestPermissions = new UserRequestPermissionsDTO();
        List<String> roleIdList = sysUserFeign.getRoleIdList(user.getUid());
        if (roleIdList.contains("1")) {
            userRequestPermissions.setPermissionsCode(menuCode);
            userRequestPermissions.setDataScope(DATA_SCOPE_ALL);
        } else {
            userRequestPermissions = requestPermissionsList
                    .stream()
                    .filter(p -> p.getPermissionsCode().equals(menuCode))
                    .findFirst().orElse(null);
            if (ObjectUtil.isEmpty(userRequestPermissions)) {
                return "AND 1 = 2";
            }
        }

        List<String> userList = sysUserFeign.getDepUserList(user.getUid());
        //字段名称
        List<String> tableFieldList = Arrays.asList(tableField.split(","));

        int tableFieldSize = tableFieldList.size();

        StringBuilder sqlString = new StringBuilder();
        if (DATA_SCOPE_ALL.equals(userRequestPermissions.getDataScope())) {
            return "";
        } else if (DATA_SCOPE_DEPT.equals(userRequestPermissions.getDataScope())) {
            List<String> list = new ArrayList<>();
            for (String s : userList) {
                list.add(s);
            }
            if (CollectionUtils.isNotEmpty(list)) {
                if (tableFieldSize == 1) {
                    sqlString.append(" AND string_to_array(" + tableFieldList.get(0) + ",',') && string_to_array('" + StringUtils.join(list, ",") + "',',')");
                } else {
                    sqlString.append(" AND (string_to_array(" + tableFieldList.get(0) + ",',') && string_to_array('" + StringUtils.join(list, ",") + "',',')");
                    if (tableFieldSize > 1) {
                        sqlString.append(" OR ");
                        for (int i = 1; i < tableFieldSize; i++) {
                            sqlString.append("string_to_array(" + tableFieldList.get(i) + ",',') && string_to_array('" + StringUtils.join(list, ",") + "',','))");
                        }
                    }
                }

            } else {
                if (tableFieldSize == 1) {
                    sqlString.append(" AND string_to_array(" + tableFieldList.get(0) + ",',') && string_to_array('" + user.getUid() + "',',')");
                } else {
                    sqlString.append(" AND (string_to_array(" + tableFieldList.get(0) + ",',') && string_to_array('" + user.getUid() + "',',')");
                    if (tableFieldSize > 1) {
                        sqlString.append(" OR ");
                        for (int i = 1; i < tableFieldSize; i++) {
                            sqlString.append("string_to_array(" + tableFieldList.get(i) + ",',') && string_to_array('" + user.getUid() + "',','))");
                        }
                    }
                }
            }
            //like any (array['%1582313948525367297%','%1549948476757303297%'])
        } else if (DATA_SCOPE_SELF.equals(userRequestPermissions.getDataScope())) {
            if (tableFieldSize == 1) {
                sqlString.append(" AND string_to_array(" + tableFieldList.get(0) + ",',') && string_to_array('" + user.getUid() + "',',')");
            } else {
                sqlString.append(" AND (string_to_array(" + tableFieldList.get(0) + ",',') && string_to_array('" + user.getUid() + "',',')");
                if (tableFieldSize > 1) {
                    sqlString.append(" OR ");
                    for (int i = 1; i < tableFieldSize; i++) {
                        sqlString.append("string_to_array(" + tableFieldList.get(i) + ",',') && string_to_array('" + user.getUid() + "',','))");
                    }
                }
            }
        }
        return sqlString.toString();
    }
    /**
     * 根据菜单code查询用户数据权限
     * @author hyj
     * @date 2024/5/9 10:43
     * @param menuCode 菜单编号
     * @return java.lang.String
     **/
    @Override
    public Boolean getUserDatePermissionByMenuCode(String menuCode) {
        LoginUser user = UserContext.getDefaultLoginUser();
        List<UserRequestPermissionsDTO> requestPermissionsList = sysUserInfoService.getRequestPermissionsList(user.getUid());
        UserRequestPermissionsDTO userRequestPermissions = new UserRequestPermissionsDTO();
        List<String> roleIdList = sysRoleUserService.findRoleIdsByUid(user.getUid());
        if (roleIdList.contains("1")) {
            return true;
        }
        userRequestPermissions = requestPermissionsList
                .stream()
                .filter(p -> p.getPermissionsCode().equals(menuCode))
                .findFirst().orElse(null);
        if (ObjectUtil.isNotEmpty(userRequestPermissions)) {
            return true;
        }
        return false;
    }
}
