package com.erp.server.bi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.bi.dto.BiDataSourceCustomDTO;
import com.erp.model.bi.dto.BiDataSourceCustomSearchDTO;
import com.erp.model.dmp.entity.BiDataSourceCustomEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
     * @return IPage<BiDataSourceCustomDTO>
     */
    IPage<BiDataSourceCustomDTO> paging(Page query, @Param("params") BiDataSourceCustomSearchDTO params);
}
