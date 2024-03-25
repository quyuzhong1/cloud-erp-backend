package com.erp.server.tms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.tms.dto.TmsLogisticsBillCostDetailDTO;
import com.erp.model.tms.entity.TmsLogisticsBillCostDetailEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 自发货费用明细 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2024-03-20
 */
@Mapper
public interface TmsLogisticsBillCostDetailMapper extends BaseMapper<TmsLogisticsBillCostDetailEntity> {

    List<TmsLogisticsBillCostDetailDTO.CostCompareDTO> getCostCompareListById(String id);
    /**
     * @description: 查询费用
     * @author Will
     * @date: 2024/3/25 9:19
     * @param mainIdList
     * @return List<CostViewDTO>
     */
    List<TmsLogisticsBillCostDetailDTO.CostViewDTO> listCostByMainIdList(@Param("mainIdList") List<String> mainIdList);
}
