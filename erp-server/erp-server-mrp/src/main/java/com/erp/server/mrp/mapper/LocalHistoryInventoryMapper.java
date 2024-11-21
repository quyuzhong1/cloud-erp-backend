package com.erp.server.mrp.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.mrp.dto.LocalHistoryInventoryDTO;
import com.erp.model.mrp.entity.LocalHistoryInventoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 本地仓库存 Mapper 接口
 * </p>
 *
 * @author Lambda
 * @since 2024-11-08
 */
@Mapper
public interface LocalHistoryInventoryMapper extends BaseMapper<LocalHistoryInventoryEntity> {

    /**
     * 分页每日库存
     * @param page 页
     * @param params 参数
     */
    IPage<LocalHistoryInventoryDTO.PagingViewDTO> paging(@Param("page") Page<LocalHistoryInventoryDTO.PagingViewDTO> page, @Param("params") LocalHistoryInventoryDTO.SearchParamDTO params);

    IPage<LocalHistoryInventoryDTO.PagingViewDTO> exportLocalInventory(@Param("page") Page<LocalHistoryInventoryDTO.PagingViewDTO> page, @Param("params")LocalHistoryInventoryDTO.ExportDTO params);
}
