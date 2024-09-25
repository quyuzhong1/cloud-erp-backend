package com.erp.server.wms.handler;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.postgresql.util.PGobject;
import org.springframework.stereotype.Component;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * JSONArray
 * @author will
 * @date 2024/9/25 17:28
 */
@Component
public class ObjectJsonArrayHandler extends BaseTypeHandler<JSONArray> {
    //引入PGSQL提供的工具类PGobject
    private static final PGobject jsonObject = new PGobject();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, JSONArray param, JdbcType jdbcType) throws SQLException {
        //转换的操作在这里
        jsonObject.setType("json");
        jsonObject.setValue(param.toString());
        ps.setObject(i, jsonObject);
    }

    @Override
    public JSONArray getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String sqlJson = rs.getString(columnName);
        if (null != sqlJson) {
            return JSONUtil.parseArray(sqlJson);
        }
        return null;
    }

    //根据列索引，获取可以为空的结果
    @Override
    public JSONArray getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String sqlJson = rs.getString(columnIndex);
        if (null != sqlJson) {
            return JSONUtil.parseArray(sqlJson);
        }
        return null;
    }

    @Override
    public JSONArray getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String sqlJson = cs.getString(columnIndex);
        if (null != sqlJson) {
            return JSONUtil.parseArray(sqlJson);
        }
        return null;
    }
}
