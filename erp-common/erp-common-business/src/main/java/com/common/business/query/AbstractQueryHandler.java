package com.common.business.query;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.threadlocal.AdvanceQueryContext;
import com.common.business.utils.QueryUtils;
import com.common.core.constant.EnumMessage;
import org.apache.commons.lang3.StringUtils;

/**
 * 查询条件处理程序扩展handler
 */
public abstract class AbstractQueryHandler implements IQueryHandler{

    @Override
    public String splicingSQL(String field, String compareCode, Object value, String compareCodeSplicingValueSql) {
        try {
            AdvanceQueryContext.setCompareCode(EnumMessage.getByCode(QueryConditionEnum.class,compareCode));
            String sql = handleSqlLogic(field,  value, compareCodeSplicingValueSql);
            if (StringUtils.isNotBlank(sql)) {
                return sql;
            }
            sql = QueryUtils.splicingSQL(AdvanceQueryContext.getQueryList());
            if(StringUtils.isBlank(sql)){
                return this.getQueryAllSql();
            }
            return sql;
        }finally {
            AdvanceQueryContext.remove();
        }
    }

    protected abstract String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql);

    /**
     * 默认封装DTO方法 field 数据库别名，value 值
     * @param field
     * @param value
     */
    protected void buildDefaultDTO(String field, Object value){
        AdvanceQueryDTO advanceQueryDTO = AdvanceQueryDTO.buildDefaultSplicingSQLDTO(field,value);
        AdvanceQueryContext.addQuery(advanceQueryDTO);
    }

    protected void buildSplicingSQLDTO(String field, QueryConditionEnum queryConditionEnum, Object value, QueryDataTypeEnum dataTypeEnum){
        AdvanceQueryDTO advanceQueryDTO = AdvanceQueryDTO.buildSplicingSQLDTO(field,queryConditionEnum,value,dataTypeEnum);
        AdvanceQueryContext.addQuery(advanceQueryDTO);
    }
    /**
     * 默认封装DTO方法 field 数据库别名，value 值
     */
    protected String getSplicingSQL(){
        try {
            return QueryUtils.splicingSQL(AdvanceQueryContext.getQueryList());
        }finally {
            AdvanceQueryContext.remove();
        }
    }

    protected String getQueryEmptySql(){
        try {
            return " 1 = 2 ";
        }   finally {
            AdvanceQueryContext.remove();
        }
    }

    protected String getQueryAllSql(){
        return " 1 = 1 ";
    }
}