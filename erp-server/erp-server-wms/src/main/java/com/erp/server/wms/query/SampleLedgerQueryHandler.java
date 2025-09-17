package com.erp.server.wms.query;

import com.common.business.dto.FindUserDTO;
import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.erp.rpc.sys.feign.SysUserFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 样品台账统计查询处理器
 *
 * @author wuhaotian
 * @since 2025-08-21
 */
@Component
public class SampleLedgerQueryHandler extends AbstractQueryHandler {

    @Autowired
    private SysUserFeign sysUserFeign;

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if ("tab".equals(field)) {
            return getTabSql(value);
        }
        return null;
    }

    public String getTabSql(Object value) {
        if ("all".equals(value)|| "".equals(value)){
            return getQueryAllSql();
        }
        
        String tabFlag = value.toString();
        
        if ("enabled".equals(tabFlag) || "disabled".equals(tabFlag) || "personEnable".equals(tabFlag)) {
            // 通过Feign查询所有用户
            List<FindUserDTO> userList = sysUserFeign.getUserList();
            
            // 根据状态过滤用户ID
            List<String> userIdList = userList.stream()
                .filter(user -> {
                    boolean isDisabled = user.getDisabled();
                    if ("enabled".equals(tabFlag) || "personEnable".equals(tabFlag)) {
                        return !isDisabled; // 启用：disabled为false
                    } else {
                        return isDisabled; // 禁用：disabled为true
                    }
                })
                .map(FindUserDTO::getUserId)
                .collect(Collectors.toList());
            
            // 如果找到用户，则添加用户ID条件
            if (!userIdList.isEmpty()) {
                super.buildSplicingSQLDTO("sl.user_id",  QueryConditionEnum.IN_LIST, userIdList, QueryDataTypeEnum.STRING);
            } else {
                // 如果没有找到符合条件的用户，返回空结果
                super.buildSplicingSQLDTO("sl.user_id",  QueryConditionEnum.EQ, "''", QueryDataTypeEnum.STRING);
            }
        }
        
        return super.getSplicingSQL();
    }
}
