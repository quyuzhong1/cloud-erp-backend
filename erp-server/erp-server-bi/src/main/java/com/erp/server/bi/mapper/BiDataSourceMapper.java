package com.erp.server.bi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.bi.dto.AdvanceSearchDTO;
import com.erp.model.bi.dto.BiDataSourceDTO;
import com.erp.model.bi.entity.BiDataSourceEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author Will
 * @version 1.0

 * @date 2022/12/19 9:30
 */
@Mapper
public interface BiDataSourceMapper extends BaseMapper<BiDataSourceEntity> {
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2022/12/19 9:36
     * @param query
     * @param params
     * @return IPage<BiDataSourceDTO>
     */
    IPage<BiDataSourceDTO> paging(Page<Object> query,@Param("params") AdvanceSearchDTO params);
}
