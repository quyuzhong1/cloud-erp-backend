package com.erp.server.sys.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.erp.common.dto.base.BasePagingSearchDTO;
import com.erp.model.sys.entity.SysPostEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Classname SysPostMapper
 * @Description TODO
 * @Date 2022-07-12 16:10
 * @Created by yl
 */
@Mapper
public interface SysPostMapper extends BaseMapper<SysPostEntity> {
    IPage<SysPostEntity> paging(Page query, BasePagingSearchDTO params);
}
