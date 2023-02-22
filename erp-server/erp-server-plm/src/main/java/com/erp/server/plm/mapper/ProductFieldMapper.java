package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseSearchDTO;
import com.erp.model.plm.entity.ProductFieldEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @Classname SysProductFieldMapper
 * @Description TODO
 * @Date 2022-09-15 11:56
 * @Created by yl
 */
@Mapper
public interface ProductFieldMapper extends BaseMapper<ProductFieldEntity> {
    IPage sysPaging(Page query, @Param("params") BaseSearchDTO params,@Param("flagState") Integer state);

    IPage paging(Page query,@Param("params") BaseSearchDTO params, @Param("flagState")Integer state);
}
