package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.inventory.InitStockDTO;
import com.erp.model.wms.entity.InitStockEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 期初库存表 Mapper 接口
 * </p>
 *
 * @author ZHANGCHUNLIN
 * @since 2023-05-10
 */
@Mapper
public interface InitStockMapper extends BaseMapper<InitStockEntity> {

    IPage<InitStockDTO.ListDTO> page(Page query, @Param("params") InitStockDTO.SearchParamDTO params);

    List<InitStockDTO.ListDTO> exportList(@Param("params") InitStockDTO.ExportSearchParamDTO params);
    Page<InitStockDTO.ListDTO> exportList(@Param("page") Page<InitStockDTO.ListDTO> page, @Param("params") InitStockDTO.ExportSearchParamDTO params);

    Integer getTotalQty(@Param("params") InitStockDTO.ConditionDTO param);

}
