package com.erp.server.wms.mapper;

import com.erp.model.wms.dto.StocktakingTaskDetailDTO;
import com.erp.model.wms.entity.StocktakingTaskDetailEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 盘点任务明细表 Mapper 接口
 * </p>
 *
 * @author Lambda
 * @since 2023-07-31
 */
@Mapper
public interface StocktakingTaskDetailMapper extends BaseMapper<StocktakingTaskDetailEntity> {

    List<StocktakingTaskDetailDTO.ExportDTO> listExportByMainId(@Param("mainId") String mainId);


    /**
     * 根据仓库id、组织id、skuId列表查询每个sku的最新盘点时间
     * @param warehouseIds 仓库id列表
     * @param orgIds 组织id列表
     * @param skuIds skuId列表
     * @return 每个sku的最新盘点时间列表
     */
    List<StocktakingTaskDetailDTO.LastDTO> maxDateByParams(List<String> warehouseIds, List<String> orgIds, List<String> skuIds);
}
