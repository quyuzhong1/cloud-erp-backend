package com.erp.model.mrp.handler;

import com.common.core.exception.ServiceException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.postgresql.util.PGobject;

import javax.annotation.Resource;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@MappedTypes(String.class)
@MappedJdbcTypes(JdbcType.VARCHAR)
public class ObjectToListStringHandler extends BaseTypeHandler<List<String>> {
    @Resource
    private ObjectMapper objectMapper;
    private static final PGobject jsonObject = new PGobject();
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<String> salesVOS, JdbcType jdbcType) throws SQLException {
        //转换的操作在这里
        jsonObject.setType("json");
        jsonObject.setValue(toJson(salesVOS));
        ps.setObject(i, jsonObject);
    }

    @Override
    public List<String> getNullableResult(ResultSet rs, String s) throws SQLException {
        return toList(rs.getString(s));
    }

    @Override
    public List<String> getNullableResult(ResultSet rs, int i) throws SQLException {
        return toList(rs.getString(i));
    }

    @Override
    public List<String> getNullableResult(CallableStatement cs, int i) throws SQLException {
        return toList(cs.getString(i));
    }

    private String toJson(List<String> list) {
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            throw new ServiceException("JSON 转换失败", e);
        }
    }

    private List<String> toList(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            throw new ServiceException("JSON 转换失败", e);
        }
    }
}
