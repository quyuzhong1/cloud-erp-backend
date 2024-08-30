package com.erp.model.mrp.handler;

import com.common.core.exception.ServiceException;
import com.erp.model.mrp.vo.ReplenishmentSuggestionVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.postgresql.util.PGobject;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
public class ObjectToListSaleHandler extends BaseTypeHandler<List<ReplenishmentSuggestionVO.SalesVO>> {
    @Resource
    private ObjectMapper objectMapper;
    private static final PGobject jsonObject = new PGobject();
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<ReplenishmentSuggestionVO.SalesVO> salesVOS, JdbcType jdbcType) throws SQLException {
        //转换的操作在这里
        jsonObject.setType("json");
        jsonObject.setValue(toJson(salesVOS));
        ps.setObject(i, jsonObject);
    }

    @Override
    public List<ReplenishmentSuggestionVO.SalesVO> getNullableResult(ResultSet rs, String s) throws SQLException {
        return toList(rs.getString(s));
    }

    @Override
    public List<ReplenishmentSuggestionVO.SalesVO> getNullableResult(ResultSet rs, int i) throws SQLException {
        return toList(rs.getString(i));
    }

    @Override
    public List<ReplenishmentSuggestionVO.SalesVO> getNullableResult(CallableStatement cs, int i) throws SQLException {
        return toList(cs.getString(i));
    }

    private String toJson(List<ReplenishmentSuggestionVO.SalesVO> list) {
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            throw new ServiceException("JSON 转换失败", e);
        }
    }

    private List<ReplenishmentSuggestionVO.SalesVO> toList(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<ReplenishmentSuggestionVO.SalesVO>>() {});
        } catch (Exception e) {
            throw new ServiceException("JSON 转换失败", e);
        }
    }
}
