package com.common.business.config;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.Arrays;
import java.util.List;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.skywalking.apm.toolkit.trace.TraceContext;
import org.springframework.stereotype.Component;

import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.common.business.constant.BusinessCommonConstants;
import com.common.business.enums.DynamicDataSourceTypeEnum;
import com.common.business.enums.ServiceCodeNameEnum;
import com.common.business.utils.DmpFeishuUtils;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})})
public class MybatisArchiveInterceptor implements Interceptor{
	
	private static final List<String> WHITE_TABLE_LIST = Arrays.asList(
			"file_task",
			"operate_log",
			"sys_event_tracking",
			"sys_log_record"
			);
	
    public Object intercept(Invocation invocation) throws Throwable {
    	String dataSourceName = DynamicDataSourceContextHolder.peek();
    	if(BusinessCommonConstants.isArchive() && !DynamicDataSourceTypeEnum.ARCHIVE_DORIS.getCode().equals(dataSourceName)) {
    		throw new ServiceException("归档系统必须使用归档数据源");
    	}
    	if(DynamicDataSourceTypeEnum.ARCHIVE_DORIS.getCode().equals(dataSourceName)) {
    		StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
            BoundSql boundSql = statementHandler.getBoundSql();
            Field field = boundSql.getClass().getDeclaredField("sql");
            field.setAccessible(true);
            field.set(boundSql, getNewSql(boundSql.getSql()));
    	}
        return invocation.proceed();
    }

	
	private String getNewSql(String oldSql) {
		String upperCase = oldSql.toUpperCase();
		if(upperCase.startsWith("INSERT") || upperCase.startsWith("UPDATE") || upperCase.startsWith("DELETE")) {
			String tableStartStr = upperCase.replace(" ", "").replace("INSERTINTO", "").replace("UPDATE", "").replace("DELETE", "");
			if(WHITE_TABLE_LIST.stream().noneMatch(w -> tableStartStr.startsWith(w.toUpperCase()))) {
				String errorInfo = TraceContext.traceId() + "归档系统执行增删改sql为：" + oldSql;
				log.error(errorInfo);
				DmpFeishuUtils.sendFeiShuMsg(errorInfo);
				throw new ServiceException(ApiError.AUTH_ARCHIVE_DENIED);
			}
		}
		log.debug("归档替换前sql语句{}" , oldSql);
		String newSql = oldSql;
		ServiceCodeNameEnum[] values = ServiceCodeNameEnum.values();
		for(ServiceCodeNameEnum value : values) {
			if(ServiceCodeNameEnum.DEFAULT != value) {
				String code = value.getCode();
				newSql = newSql.replace("erp_" + code + ".", "erp_" + code + "_archive.");
			}
		}
		log.debug("归档替换后sql语句{}" , newSql);
		return newSql;
	}

}
