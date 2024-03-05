package com.common.business.handler.mybatisplus;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Mybatis OffsetDateTime处理器
 * (设置OffsetDateTime->数据库String)
 * (获取数据库String->OffsetDateTime)
 */
public class MyOffsetDateTimeTypeHandler extends BaseTypeHandler<OffsetDateTime> {

    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, OffsetDateTime parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.format(FORMATTER));
    }

    @Override
    public OffsetDateTime getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parseString(rs.getString(columnName));
    }

    @Override
    public OffsetDateTime getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parseString(rs.getString(columnIndex));
    }

    @Override
    public OffsetDateTime getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parseString(cs.getString(columnIndex));
    }

    private OffsetDateTime parseString(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        return OffsetDateTime.parse(value, FORMATTER);
    }
}
