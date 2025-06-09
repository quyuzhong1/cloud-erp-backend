package com.erp.server.mrp.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.mrp.dto.OverseasHistoryInventoryDTO;
import com.erp.model.mrp.entity.OverseasHistoryInventoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * 海外仓库存 Mapper 接口
 * </p>
 *
 * @author liaohui
 * @since 2024-09-23
 */
@Mapper
public interface OverseasHistoryInventoryMapper extends BaseMapper<OverseasHistoryInventoryEntity> {

    /**
     * 分页查询
     * @param query 分页
     * @param params 参数
     */
    IPage<OverseasHistoryInventoryDTO.ListDTO> paging(@Param("page") Page<OverseasHistoryInventoryDTO.ListDTO> query,@Param("params") OverseasHistoryInventoryDTO.PagingParamDTO params);

    /**
     * 根据开始结束查询
     */
    List<OverseasHistoryInventoryEntity> listByStartDateAndEndDate(@Param("startDate")LocalDate startDate,@Param("endDate") LocalDate endDate,@Param("warehouseCode") Set<String> warehouseCode);
    /**
     * 分页查询
     * @param query 分页
     * @param params 参数
     */
    IPage<OverseasHistoryInventoryDTO.ListDTO> exportOverseasInventory(@Param("page") Page<OverseasHistoryInventoryDTO.ListDTO> query,@Param("params") OverseasHistoryInventoryDTO.ExportDTO params);
}
