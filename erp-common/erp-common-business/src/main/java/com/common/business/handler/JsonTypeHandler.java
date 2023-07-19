package com.common.business.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.postgresql.util.PGobject;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

@MappedTypes(Map.class)
@MappedJdbcTypes(JdbcType.OTHER)
public class JsonTypeHandler extends BaseTypeHandler<Map<String, Object>> {
    private static final PGobject jsonObject = new PGobject();

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @SneakyThrows
    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int i, Map<String, Object> stringObjectMap, JdbcType jdbcType) throws SQLException {
        jsonObject.setType("json");
        jsonObject.setValue(objectMapper.writeValueAsString(stringObjectMap));
        preparedStatement.setObject(i, jsonObject);
    }

    @SneakyThrows
    @Override
    public Map<String, Object> getNullableResult(ResultSet resultSet, String s) throws SQLException {
        return objectMapper.readValue(resultSet.getString(s), Map.class);
    }

    @SneakyThrows
    @Override
    public Map<String, Object> getNullableResult(ResultSet resultSet, int i) throws SQLException {
        return objectMapper.readValue(resultSet.getString(i), Map.class);
    }

    @SneakyThrows
    @Override
    public Map<String, Object> getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
        return objectMapper.readValue(callableStatement.getString(i), Map.class);
    }
}
