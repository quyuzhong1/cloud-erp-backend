package com.erp.server.plm.mapper;
import com.erp.model.plm.dto.ApplicationCategoryDTO;
import com.erp.model.plm.entity.ApplicationCategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 产品应用分类 Mapper 接口
 * </p>
 *
 * @author liaohui
 * @since 2025-01-09
 */
@Mapper
public interface ApplicationCategoryMapper extends BaseMapper<ApplicationCategoryEntity> {

    List<ApplicationCategoryDTO.ViewDTO> list(@Param("searchKeyword") String searchKeyword);
}
