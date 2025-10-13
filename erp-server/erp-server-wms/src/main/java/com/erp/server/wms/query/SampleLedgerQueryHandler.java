package com.erp.server.wms.query;

import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseSearchDTO;
import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.common.core.controller.vo.ApiResult;
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
        if ("personEnable".equals(field)) {
            return getPersonEnableSql(value);
        }
        return null;
    }

    public String getTabSql(Object value) {
        if ("all".equals(value)|| "".equals(value)){
            return getQueryAllSql();
        }
        
        String tabFlag = value.toString();
        
        if ("enabled".equals(tabFlag) || "disabled".equals(tabFlag)) {
            // 通过Feign查询所有用户
            BaseSearchDTO searchDTO = new BaseSearchDTO();
            ApiResult<List<FindUserDTO>> result = sysUserFeign.userList(searchDTO);
            List<FindUserDTO> userList = result != null && result.isSuccess() ? result.getData() : null;
            
            // 根据状态过滤用户ID
            List<String> userIdList = (userList != null ? userList.stream() : java.util.stream.Stream.<FindUserDTO>empty())
                .filter(user -> {
                    if (user == null) {
                        return false; // 跳过null用户
                    }
                    Boolean disabled = user.getDisabled();
                    if (disabled == null) {
                        return false; // 跳过disabled为null的用户
                    }
                    boolean isDisabled = disabled;
                    if ("enabled".equals(tabFlag)) {
                        return !isDisabled; // 启用：disabled为false
                    } else {
                        return isDisabled; // 禁用：disabled为true
                    }
                })
                .map(FindUserDTO::getUserId)
                .filter(userId -> userId != null) // 过滤掉null的userId
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
    
    /**
     * 处理personEnable字段的查询逻辑
     * @param value boolean值，true表示查询启用的用户，false表示查询禁用的用户
     * @return SQL条件
     */
    public String getPersonEnableSql(Object value) {
        if (value == null) {
            return null;
        }
        
        boolean isEnabled = Boolean.TRUE.equals(value);
        
        // 通过Feign查询所有用户
        BaseSearchDTO searchDTO = new BaseSearchDTO();
        ApiResult<List<FindUserDTO>> result = sysUserFeign.userList(searchDTO);
        List<FindUserDTO> userList = result != null && result.isSuccess() ? result.getData() : null;
        
        // 根据启用状态过滤用户ID
        List<String> userIdList = (userList != null ? userList.stream() : java.util.stream.Stream.<FindUserDTO>empty())
            .filter(user -> {
                if (user == null) {
                    return false; // 跳过null用户
                }
                Boolean disabled = user.getDisabled();
                if (disabled == null) {
                    return false; // 跳过disabled为null的用户
                }
                boolean isDisabled = disabled;
                return isEnabled != isDisabled; // true查询启用用户，false查询禁用用户
            })
            .map(FindUserDTO::getUserId)
            .filter(userId -> userId != null) // 过滤掉null的userId
            .collect(Collectors.toList());
        
        // 如果找到用户，则添加用户ID条件
        if (!userIdList.isEmpty()) {
            super.buildSplicingSQLDTO("sl.user_id", QueryConditionEnum.IN_LIST, userIdList, QueryDataTypeEnum.STRING);
        } else {
            // 如果没有找到符合条件的用户，返回空结果
            super.buildSplicingSQLDTO("sl.user_id", QueryConditionEnum.EQ, "''", QueryDataTypeEnum.STRING);
        }
        
        return super.getSplicingSQL();
    }
}
