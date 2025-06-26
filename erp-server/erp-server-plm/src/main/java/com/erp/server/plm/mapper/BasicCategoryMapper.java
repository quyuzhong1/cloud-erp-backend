package com.erp.server.plm.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.plm.dto.BasicCategoryDTO;
import com.erp.model.plm.dto.BasicCategoryTreeDTO;
import com.erp.model.plm.dto.SkuCategoryDTO;
import com.erp.model.plm.entity.BasicCategoryEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 产品分类表 Mapper 接口
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
@Mapper
public interface BasicCategoryMapper extends BaseMapper<BasicCategoryEntity> {

    List<BasicCategoryTreeDTO> getDbTree();

    /**
     * 获取到sku 的分类
     * @author yl
     * @date 2023-03-10 14:17
     * @param categoryIds
     * @return java.util.List<com.erp.model.plm.dto.ProductDetailShowDTO>
     */
    List<SkuCategoryDTO> getSkuByCategoryIds(@Param("categoryIds") List<String> categoryIds);
    /**
     * 获取二级分类的列表 拼接一级名称
     * @return
     */
    List<BasicCategoryTreeDTO> categoryGradeDown();

    List<BasicCategoryDTO> getCategoryByPid(@Param("pid") String pid);

    List<BasicCategoryDTO.DropdownDTO> getCategoryDropdown();
}
