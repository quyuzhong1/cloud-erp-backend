package com.erp.server.dmp.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.dmp.dto.ThirdShopDTO;
import com.erp.model.dmp.entity.ThirdShopEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * <p>
 * 第三方系统店铺表 Mapper 接口
 * </p>
 *
 * @author hyj
 * @since 2024-05-17
 */
@Mapper
public interface ThirdShopMapper extends BaseMapper<ThirdShopEntity> {

    IPage<ThirdShopDTO.PageSelectDTO> pagingSelect(Page query, @Param("params") ThirdShopDTO.SelectDTO params);

    IPage<ThirdShopDTO.PageDTO> paging(Page query, @Param("params") ThirdShopDTO.PagingParamDTO params);
}
