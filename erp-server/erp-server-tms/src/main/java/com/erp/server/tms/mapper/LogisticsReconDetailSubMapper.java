package com.erp.server.tms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.tms.dto.LogisticsReconDetailSubDTO;
import com.erp.model.tms.entity.LogisticsReconDetailSubEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
     * 物流商对账费用项查询（按 detail_id 集合，用于详情页展开 / 合并匹配读取）
     * @author Will
     * @date: 2026/05/29
     * @param detailIds
     * @return List<LogisticsReconDetailSubDTO.ListDTO>
     */
    List<LogisticsReconDetailSubDTO.ListDTO> listByDetailIds(@Param("detailIds") List<String> detailIds);
}
