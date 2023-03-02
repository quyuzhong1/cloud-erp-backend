package com.erp.server.plm.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.plm.dto.BasicCategoryTreeDTO;
import com.erp.model.plm.entity.BasicCategoryEntity;
import org.apache.ibatis.annotations.Mapper;

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
}
