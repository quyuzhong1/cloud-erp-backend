package com.erp.server.mrp.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.mrp.dto.FbaHistoryInventoryDTO;
import com.erp.model.mrp.entity.FbaHistoryInventoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * fba历史库存 Mapper 接口
 * </p>
 *
 * @author Lambda
 * @since 2024-09-12
 */
@Mapper
public interface FbaHistoryInventoryMapper extends BaseMapper<FbaHistoryInventoryEntity> {

    /**
     * 分页查询
     */
    IPage<FbaHistoryInventoryDTO.ListDTO> paging(Page<FbaHistoryInventoryDTO> query, FbaHistoryInventoryDTO.PagingParamDTO params);

    /**
     * 分页查询
     */
    Page<FbaHistoryInventoryDTO.ListDTO> listExport(Page<FbaHistoryInventoryDTO.ListDTO> page, FbaHistoryInventoryDTO.ExportDTO params);
}
