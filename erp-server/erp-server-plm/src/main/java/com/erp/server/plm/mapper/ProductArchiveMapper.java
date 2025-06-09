package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.plm.dto.ProductArchiveDTO;
import com.erp.model.plm.dto.ProductSearchDTO;
import com.erp.model.plm.entity.ProductArchiveEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @Classname ProductArchiveMapper

 * @Date 2022-10-09 11:39
 * @Created by yl
 */
@Mapper
public interface ProductArchiveMapper extends BaseMapper<ProductArchiveEntity> {
    IPage<ProductArchiveDTO> paging(Page<ProductSearchDTO.PagingParamDTO> query, @Param("params") ProductSearchDTO.PagingParamDTO params);
}
