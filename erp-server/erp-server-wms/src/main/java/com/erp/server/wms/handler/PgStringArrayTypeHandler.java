package com.erp.server.wms.handler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.*;
import java.util.Arrays;
import java.util.List;

/**
 * @author zdy
 * @ClassName PgStringArrayTypeHandler
 * @description: TODO
 * @date 2025年05月15日
 * @version: 1.0
 */
@MappedTypes(List.class)
public class PgStringArrayTypeHandler extends BaseTypeHandler<List<String>> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<String> parameter, JdbcType jdbcType) throws SQLException {
        Array array = ps.getConnection().createArrayOf("varchar", parameter.toArray());
        ps.setArray(i, array);
    }

    @Override
    public List<String> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return Arrays.asList((String[])rs.getArray(columnName).getArray());
    }

    @Override
    public List<String> getNullableResult(ResultSet resultSet, int i) throws SQLException {
        return null;
    }

    @Override
    public List<String> getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
        return null;
    }
}
