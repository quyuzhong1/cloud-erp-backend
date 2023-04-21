package com.erp.server.plm.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface WorkOptionMapper {
    Integer getTableNum(@Param("tableName") String string);
}
