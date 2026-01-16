package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.wms.dto.StocktakingTaskDetailDTO;
import com.erp.model.wms.entity.StocktakingTaskDetailEntity;
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


    /** 根据参数获取最新的盘点日期
     * @author yl
     * @date 2023-09-05 10:28
     * @param warehouseIdList
     * @param orgIdList
     * @param skuIdList
     * @return java.util.List<com.erp.model.wms.dto.StocktakingTaskDetailDTO.LastDTO>
     */
    List<StocktakingTaskDetailDTO.LastDTO> maxDateByParams(@Param("warehouseIdList")List<String> warehouseIdList,
                                                           @Param("orgIdList") List<String> orgIdList,
                                                           @Param("skuIdList") List<String> skuIdList);
}
