package com.common.business.query;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.threadlocal.AdvanceQueryContext;
import com.common.business.utils.QueryUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 查询条件处理程序扩展handler
 */
public abstract class AbstractQueryHandler implements IQueryHandler{

    @Override
    public String splicingSQL(String field, String compareCode, Object value, String compareCodeSplicingValueSql) {
        try {
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
        return " 1 = 2 ";
    }

    protected String getQueryAllSql(){
        return " 1 = 1 ";
    }
}