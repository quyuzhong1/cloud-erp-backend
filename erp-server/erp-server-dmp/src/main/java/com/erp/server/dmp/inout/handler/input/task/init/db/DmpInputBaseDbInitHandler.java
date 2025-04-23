package com.erp.server.dmp.inout.handler.input.task.init.db;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.DmpCfgDbEntity;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.input.task.init.DmpInputInitHandler;
import com.erp.server.dmp.inout.utils.DmpHandlerUtils;
import com.erp.server.dmp.service.DmpCfgDbService;

import cn.hutool.core.exceptions.ExceptionUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Scope("prototype")
public class DmpInputBaseDbInitHandler extends DmpInputInitHandler{
	
	@Autowired
	protected DmpCfgDbService dmpCfgDbService;
	
	public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
	
	@Override
	public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse){
		String typeId = dmpCfgInputEntity.getTypeId();
		DataSource dataSource = dmpHandlerCache.getDataSource(typeId);
		List<Map<String, Object>> queryDB = null;
		try {
			String querySql = this.getQuerySql(dmpRequest, dmpResponse);
			queryDB = DmpHandlerUtils.queryDB(dataSource, querySql);
		} catch (SQLException e) {
			log.error("获取db数据异常" , e);
			throw new ServiceException("获取db数据异常" + ExceptionUtil.stacktraceToOneLineString(e));
		}
		return Arrays.asList(DmpInputTaskInitDTO.initMsg(JSON.toJSONString(queryDB)));
	}
	
	protected String getQuerySql(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
		DmpCfgDbEntity dmpCfgDbEntity = dmpCfgDbService.getById(dmpCfgInputEntity.getTypeId());
		String taskExtendJson = dmpInputTaskEntity.getExtendJson();
		StringBuilder sqlQuery = new StringBuilder();
		sqlQuery.append("select * from ");
		sqlQuery.append(dmpCfgDbEntity.getTableName());
		sqlQuery.append(" where 1 = 1 ");
		if(StringUtils.isNotBlank(taskExtendJson)) {
			JSONObject taskParseObject = JSON.parseObject(taskExtendJson);
			if(taskParseObject != null) {
				String taskWhere = taskParseObject.getString("where");
				if(StringUtils.isNotBlank(taskWhere)) {
					sqlQuery.append(" ");
					if(taskWhere.trim().toUpperCase().startsWith("AND")) {
						sqlQuery.append(taskWhere);
					}else {
						sqlQuery.append(" and " + taskWhere);
					}
				}
			}
		}
		String extendJson = dmpCfgInputEntity.getExtendJson();
		if(StringUtils.isNotBlank(extendJson)) {
			JSONObject parseObject = JSON.parseObject(extendJson);
			if(parseObject != null) {
				String time = parseObject.getString("time");
				if(StringUtils.isNotBlank(time)) {
					sqlQuery.append(" ");
					sqlQuery.append(" and " + time + " >= '" + dmpInputTaskEntity.getStartTime().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)) + "'");
					sqlQuery.append(" and " + time + " <= '" + dmpInputTaskEntity.getEndTime().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)) + "'");
				}
				String where = parseObject.getString("where");
				if(StringUtils.isNotBlank(where)) {
					sqlQuery.append(" ");
					if(where.trim().toUpperCase().startsWith("AND")) {
						sqlQuery.append(where);
					}else {
						sqlQuery.append(" and " + where);
					}
				}
				String orderBy = parseObject.getString("orderBy");
				if(StringUtils.isNotBlank(orderBy)) {
					sqlQuery.append(" ");
					if(orderBy.trim().toUpperCase().startsWith("ORDERBY")) {
						sqlQuery.append(orderBy);
					}else {
						sqlQuery.append(" order by " + orderBy);
					}
				}
				String limit = parseObject.getString("limit");
				if(StringUtils.isNotBlank(limit)) {
					sqlQuery.append(" ");
					if(limit.trim().toUpperCase().startsWith("LIMIT")) {
						sqlQuery.append(limit);
					}else {
						sqlQuery.append(" limit " + limit);
					}
				}
			}
		}
		
		return sqlQuery.toString();
	}
	
}
