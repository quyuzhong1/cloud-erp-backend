package com.erp.server.oms.mapper;
import com.erp.model.oms.dto.SoB2cRefCategoryDTO;
import com.erp.model.oms.entity.SoB2cRefCategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * B2C销售订单分类表 Mapper 接口
 * </p>
 *
 * @author Will
 * @since 2023-08-18
 */
@Mapper
public interface SoB2cRefCategoryMapper extends BaseMapper<SoB2cRefCategoryEntity> {
    List<SoB2cRefCategoryDTO.CategoryNamesDTO> listCategoryNamesBySoIds(@Param("soIds")List<String> soIds);
}
