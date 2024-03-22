package com.erp.server.tms.mapper;
import com.erp.model.tms.dto.TmsLogisticsBillCostDetailDTO;
import com.erp.model.tms.entity.TmsLogisticsBillCostDetailEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;

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
}
