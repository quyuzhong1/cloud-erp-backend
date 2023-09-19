package com.erp.server.bi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.bi.dto.BiCategoryDTO;
import com.erp.model.bi.entity.BiProductDetailEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 产品sku表 Mapper 接口
 * </p>
 *
 * @author lambda
 * @since 2023-04-21
 */
@Mapper
public interface BiProductDetailMapper extends BaseMapper<BiProductDetailEntity> {

    /**
     * 查询产品分类信息
     * @param categoryIdList
     * @return
     */
    List<BiCategoryDTO.ProductCategoryDTO> listByCategoryIds(@Param("categoryIdList") List<String> categoryIdList);
}
