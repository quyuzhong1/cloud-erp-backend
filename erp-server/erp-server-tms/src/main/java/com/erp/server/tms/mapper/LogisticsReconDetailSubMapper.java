package com.erp.server.tms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.tms.dto.LogisticsReconDTO;
import com.erp.model.tms.entity.LogisticsReconDetailSubEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 物流商对账费用项明细 Mapper 接口
 * </p>
 *
 * @author Will
 * @since 2026-05-29
 */
@Mapper
public interface LogisticsReconDetailSubMapper extends BaseMapper<LogisticsReconDetailSubEntity> {

    /**
     * 统计主表下有效费用项数（detail 归属与 sub.main_id 一致）
     */
    int countValidByMainId(@Param("mainId") String mainId);

    /**
     * 汇总主表下有效费用项的本位币金额（detail 归属与 sub.main_id 一致），无数据返回 0
     */
    BigDecimal sumLocalAmountByMainId(@Param("mainId") String mainId);

    /**
     * 按主表 id 集合批量聚合分页列表所需费用项统计（口径与详情页有效费用项一致）
     */
    List<LogisticsReconDTO.PagingStatsDTO> listPagingStatsByMainIds(@Param("mainIds") List<String> mainIds);
}
