package com.erp.server.wms.mapper;

import com.erp.model.wms.dto.StocktakingProfitLossDetailDTO;
import com.erp.model.wms.entity.StocktakingProfitLossDetailEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 盘盈盘亏单详情 Mapper 接口
 * </p>
 *
 * @author Lambda
 * @since 2023-07-31
 */
@Mapper
public interface StocktakingProfitLossDetailMapper extends BaseMapper<StocktakingProfitLossDetailEntity> {

    /**
     * 获取到详情
     * @param mainIdList
     * @return
     */
    List<StocktakingProfitLossDetailDTO.ViewDTO> listByMainIds(@Param("mainIdList") List<String> mainIdList);
}
