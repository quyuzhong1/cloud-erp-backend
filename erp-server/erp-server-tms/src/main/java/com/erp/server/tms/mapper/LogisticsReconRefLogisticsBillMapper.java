package com.erp.server.tms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.tms.dto.LogisticsReconRefLogisticsBillDTO;
import com.erp.model.tms.entity.LogisticsReconRefLogisticsBillEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 物流商对账单 - 关联关系 Mapper 接口
 * </p>
 *
 * @author Will
 * @since 2026-05-29
 */
@Mapper
public interface LogisticsReconRefLogisticsBillMapper extends BaseMapper<LogisticsReconRefLogisticsBillEntity> {

    /**
     * 按 detailId 集合查询有效关联（join logistics_bill / logistics_bill_cost 展示业务单号）
     * @author Will
     * @date: 2026/05/29
     * @param detailIds
     * @return List<LogisticsReconRefLogisticsBillDTO.ListDTO>
     */
    List<LogisticsReconRefLogisticsBillDTO.ListDTO> listByDetailIds(@Param("detailIds") List<String> detailIds);

    /**
     * 按 mainId 查询有效关联（join logistics_bill / logistics_bill_cost 展示业务单号）
     * @author Will
     * @date: 2026/05/29
     * @param mainId
     * @return List<LogisticsReconRefLogisticsBillDTO.ListDTO>
     */
    List<LogisticsReconRefLogisticsBillDTO.ListDTO> listByMainId(@Param("mainId") String mainId);
}
