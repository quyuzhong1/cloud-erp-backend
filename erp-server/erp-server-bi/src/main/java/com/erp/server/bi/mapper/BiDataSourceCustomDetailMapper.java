package com.erp.server.bi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.bi.entity.BiDataSourceCustomDetailEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/15 18:18
 */
@Mapper
public interface BiDataSourceCustomDetailMapper extends BaseMapper<BiDataSourceCustomDetailEntity> {
    /**
     * 根据主表ids查询
     */
    List<BiDataSourceCustomDetailEntity> listByCustomIds(@Param("customIds") List<String> customIds);
}
