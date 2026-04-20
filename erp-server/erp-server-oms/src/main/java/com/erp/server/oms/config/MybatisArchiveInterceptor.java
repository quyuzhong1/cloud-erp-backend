package com.erp.server.oms.config;

import java.lang.reflect.Field;
import java.sql.Connection;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.springframework.stereotype.Component;

import com.common.business.enums.ServiceCodeNameEnum;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})})
public class MybatisArchiveInterceptor implements Interceptor{
	
    public Object intercept(Invocation invocation) throws Throwable {
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        BoundSql boundSql = statementHandler.getBoundSql();
        Field field = boundSql.getClass().getDeclaredField("sql");
        field.setAccessible(true);
        field.set(boundSql, getNewSql(boundSql.getSql()));
        return invocation.proceed();
    }

	
	private String getNewSql(String oldSql) {
		log.debug("替换前sql语句{}" , oldSql);
		String newSql = oldSql;
		ServiceCodeNameEnum[] values = ServiceCodeNameEnum.values();
		for(ServiceCodeNameEnum value : values) {
			if(ServiceCodeNameEnum.DEFAULT != value) {
				String code = value.getCode();
				newSql = newSql.replace("erp_" + code + ".", "erp_" + code + "_archive.");
			}
		}
		log.debug("替换后sql语句{}" , newSql);
		return newSql;
	}

}
