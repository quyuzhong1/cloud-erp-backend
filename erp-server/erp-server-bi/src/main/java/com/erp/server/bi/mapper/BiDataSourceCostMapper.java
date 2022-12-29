package com.erp.server.bi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.bi.dto.BiDataSourceCostSearchDTO;
import com.erp.model.bi.entity.BiDataSourceCostEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/14 16:38
 */
@Mapper
public interface BiDataSourceCostMapper extends BaseMapper<BiDataSourceCostEntity> {
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2022/12/14 16:44
     * @param query
     * @param params
     * @return IPage<BiDataSourceCostDTO>
     */
    IPage<LinkedHashMap<String,Object>> paging(Page query, @Param("params") BiDataSourceCostSearchDTO params);
    /**
     * @description: 查询所有成本
     * @author Will
     * @date: 2022/12/16 15:54
     * @param params
     * @return List<LinkedHashMap<Object>>
     */
    List<LinkedHashMap<String,Object>> getAllBiDataSourceCost(@Param("params") BiDataSourceCostSearchDTO params);
}
