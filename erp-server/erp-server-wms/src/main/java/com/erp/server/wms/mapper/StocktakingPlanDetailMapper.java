package com.erp.server.wms.mapper;
import com.erp.model.wms.dto.StocktakingPlanDetailDTO;
import com.erp.model.wms.entity.StocktakingPlanDetailEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.erp.model.wms.enums.StocktakingTypeEnum;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


/**
 * <p>
 * 盘点计划明细表 Mapper 接口
 * </p>
 *
 * @author Cloud
 * @since 2023-08-08
 */
@Mapper
public interface StocktakingPlanDetailMapper extends BaseMapper<StocktakingPlanDetailEntity> {


    /**
     * 盘点计划明细
     * @param mainId
     * @return
     */
    List<StocktakingPlanDetailDTO.ViewDTO> listByMainIdAndType(String mainId);
}
