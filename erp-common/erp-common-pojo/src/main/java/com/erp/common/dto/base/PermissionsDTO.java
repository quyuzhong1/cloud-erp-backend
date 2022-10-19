package com.erp.common.dto.base;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @Classname PermissionsDTO
 * @Description TODO
 * @Date 2022-10-15 15:49
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class PermissionsDTO  implements Serializable {

    /**
     * 数据权限
     * 1-自己,2-部门,3-全部
     */
    private Integer dataScope;

    /** 请求参数 */
    private Map<String, Object> param;

    public Map<String, Object> getParam() {
        if (param == null) {
            param = new HashMap<>();
        }
        return param;
    }
}
