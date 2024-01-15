package com.common.business.query;

/**
 * 查询条件处理程序扩展handler
 */
public interface  IQueryHandler {

    String splicingSQL(String field, String compareCode, Object value, String compareCodeSplicingValueSql);

}