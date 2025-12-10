package com.common.business.dto.base;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @Classname PermissionsDTO

 * @Date 2022-10-15 15:49
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class PermissionsDTO extends SuperDTO  implements Serializable {

    /**
     * 数据权限
     * 1-自己,2-部门,3-全部
     */
    private Integer dataScope;

    /**
     * 拼接的sql
     */
    private String permissionSql;
}
