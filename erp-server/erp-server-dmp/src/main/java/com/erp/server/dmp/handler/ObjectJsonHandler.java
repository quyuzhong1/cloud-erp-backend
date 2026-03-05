package com.erp.server.dmp.handler;

import cn.hutool.json.JSONObject;
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
 * @Author: wtr
 * @Date: 2026/3/4 10:40
 * @Param:
 * @Return:
 * @Description:
 **/
@Component
public class ObjectJsonHandler extends BaseTypeHandler<JSONObject> {
    //引入PGSQL提供的工具类PGobject
    private static final PGobject jsonObject = new PGobject();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, JSONObject param, JdbcType jdbcType) throws SQLException {
        //转换的操作在这里
        jsonObject.setType("json");
        jsonObject.setValue(param.toString());
        ps.setObject(i, jsonObject);
    }

    @Override
    public JSONObject getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String sqlJson = rs.getString(columnName);
        if (null != sqlJson) {
            return JSONUtil.parseObj(sqlJson);
        }
        return null;
    }

    //根据列索引，获取可以为空的结果
    @Override
    public JSONObject getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String sqlJson = rs.getString(columnIndex);
        if (null != sqlJson) {
            return JSONUtil.parseObj(sqlJson);
        }
        return null;
    }

    @Override
    public JSONObject getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String sqlJson = cs.getString(columnIndex);
        if (null != sqlJson) {
            return JSONUtil.parseObj(sqlJson);
        }
        return null;
    }
}

