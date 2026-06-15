package com.common.business.config;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.skywalking.apm.toolkit.trace.TraceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.common.business.cache.LocalCache;
import com.common.business.constant.BusinessCommonConstants;
import com.common.business.enums.DynamicDataSourceTypeEnum;
import com.common.business.enums.ServiceCodeNameEnum;
import com.common.business.utils.ApplicationContextUtils;
import com.common.business.utils.DmpFeishuUtils;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})})
public class MybatisArchiveInterceptor implements Interceptor{
	
    public Object intercept(Invocation invocation) throws Throwable {
    	if(BusinessCommonConstants.isArchive()) {
    		StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
    		MetaObject metaObject = MetaObject.forObject(statementHandler, SystemMetaObject.DEFAULT_OBJECT_FACTORY,
                    SystemMetaObject.DEFAULT_OBJECT_WRAPPER_FACTORY, new DefaultReflectorFactory());
            
            MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("delegate.mappedStatement");
            SqlCommandType sqlType = mappedStatement.getSqlCommandType();
            BoundSql boundSql = statementHandler.getBoundSql();
            Field field = boundSql.getClass().getDeclaredField("sql");
            field.setAccessible(true);
            field.set(boundSql, getNewSql(boundSql.getSql() , sqlType));
    	}
    	return invocation.proceed();
    }

	
	private String getNewSql(String oldSql , SqlCommandType sqlType) {
		String dsKey = DynamicDataSourceContextHolder.peek();
		// 判断是否为SELECT
        if (!SqlCommandType.SELECT.equals(sqlType)) {
			String extractTableName = extractTableName(oldSql , sqlType);
			if(StringUtils.isBlank(extractTableName)) {
				throw new ServiceException("提取非select语句表名失败，原始sql语句为：" + oldSql);
			}
			if(ApplicationContextUtils.getBean(LocalCache.class).getArchiveWhiteTableList().stream().noneMatch(extractTableName::equalsIgnoreCase)) {
				String errorInfo = TraceContext.traceId() + "归档系统，"+ dsKey +"数据源执行增删改sql为：" + oldSql;
				log.error(errorInfo);
				DmpFeishuUtils.sendFeiShuMsg(errorInfo);
				throw new ServiceException(ApiError.AUTH_ARCHIVE_DENIED);
			}
		}
		log.debug("归档替换前sql语句{}" , oldSql);
		String newSql = oldSql;
		if(DynamicDataSourceTypeEnum.isDorisByStr(dsKey)) {
			ServiceCodeNameEnum[] values = ServiceCodeNameEnum.values();
			for(ServiceCodeNameEnum value : values) {
				if(ServiceCodeNameEnum.DEFAULT != value) {
					String code = value.getCode();
					newSql = newSql.replace("erp_" + code + ".", "erp_" + code + "_archive.");
				}
			}
			newSql = newSql.replace("\"index\"", "`index`");
			newSql = newSql.replace("\"key\"", "`key`");
		}
		log.debug("归档替换后sql语句{}" , newSql);
		return newSql;
	}
	
	public static void main(String[] args) {
		System.out.println(extractTableName("/* test   \n*/ \n delete FROM table_name where name = ? " , SqlCommandType.DELETE));
		System.out.println(extractTableName("--  test \n -- aaa \n update TABLE_nAME set VERSION = version,upate_time = now() where  1= 1" , SqlCommandType.UPDATE));
		System.out.println(extractTableName("--  test \n /* test   \n*/ \n -- aaa \n insert    into TABLE_nA values ()" , SqlCommandType.INSERT));
	}
	
	// 修正后的正则表达式
    private static final Pattern MULTI_LINE_COMMENT = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL);
    private static final Pattern SINGLE_LINE_COMMENT = Pattern.compile("--[^\\r\\n]*", Pattern.MULTILINE);
	
	/**
     * 移除SQL中的注释（修正版 - 正确处理多行注释）
     */
    private static String removeComments(String sql) {
        if (sql == null || sql.isEmpty()) {
            return sql;
        }
        
        // 方式1：使用 Pattern.DOTALL 标志处理多行注释
        String noMultiComments = MULTI_LINE_COMMENT.matcher(sql).replaceAll(" ");
        
        // 方式2：移除单行注释（-- 到行尾）
        return SINGLE_LINE_COMMENT.matcher(noMultiComments).replaceAll(" ");
    }

    /**
     * 提取表名
     */
    private static String extractTableName(String sql, SqlCommandType sqlType) {
        if (sql == null || sql.isEmpty()) {
            return null;
        }
        sql = removeComments(sql);
        // 标准化：将多个空格替换为单个空格
        String normalizedSql = sql.replaceAll("\\s+", " ").trim();
        String lowerSql = normalizedSql.toLowerCase();
        
        switch (sqlType) {
            case INSERT:
                Pattern insertPattern = Pattern.compile("insert\\s+into\\s+([a-zA-Z_][a-zA-Z0-9_]*)");
                Matcher insertMatcher = insertPattern.matcher(lowerSql);
                return insertMatcher.find() ? insertMatcher.group(1) : null;
                
            case UPDATE:
                Pattern updatePattern = Pattern.compile("update\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s+set");
                Matcher updateMatcher = updatePattern.matcher(lowerSql);
                return updateMatcher.find() ? updateMatcher.group(1) : null;
                
            case DELETE:
                // 支持 DELETE FROM table 和 DELETE table 两种格式
                Pattern deletePattern1 = Pattern.compile("delete\\s+from\\s+([a-zA-Z_][a-zA-Z0-9_]*)");
                Matcher deleteMatcher1 = deletePattern1.matcher(lowerSql);
                if (deleteMatcher1.find()) {
                    return deleteMatcher1.group(1);
                }
                Pattern deletePattern2 = Pattern.compile("delete\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s+from");
                Matcher deleteMatcher2 = deletePattern2.matcher(lowerSql);
                return deleteMatcher2.find() ? deleteMatcher2.group(1) : null;
                
            default:
                return null;
        }
    }
}
