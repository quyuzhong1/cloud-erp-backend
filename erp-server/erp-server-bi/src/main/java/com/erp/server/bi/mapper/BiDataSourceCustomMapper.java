package com.erp.server.bi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.bi.dto.BiDataSourceCustomSearchDTO;
import com.erp.model.dmp.entity.BiDataSourceCustomEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/14 16:53
 */
@Mapper
public interface BiDataSourceCustomMapper extends BaseMapper<BiDataSourceCustomEntity> {

    /**
     * @description: 分页查询
     * @author Will
     * @date: 2022/12/14 16:44
     * @param query
     * @param params
     * @return IPage<LinkedHashMap<String,Object>>
     */
    IPage<LinkedHashMap<String,Object>> paging(Page query, @Param("params") BiDataSourceCustomSearchDTO params);
    /**
     * @description: 查询所有数据
     * @author Will
     * @date: 2022/12/20 10:40
     * @param dto
     * @return List<LinkedHashMap<Object>>
     */
    List<LinkedHashMap<String, Object>> getAllBiDataSourceCustom( @Param("params") BiDataSourceCustomSearchDTO dto);
}
