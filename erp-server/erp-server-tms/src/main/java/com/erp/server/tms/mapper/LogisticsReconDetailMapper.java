package com.erp.server.tms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.tms.dto.LogisticsReconDetailDTO;
import com.erp.model.tms.entity.LogisticsReconDetailEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 物流商对账明细（行级） Mapper 接口
 * </p>
 *
 * @author Will
 * @since 2026-05-29
 */
@Mapper
public interface LogisticsReconDetailMapper extends BaseMapper<LogisticsReconDetailEntity> {

    /**
     * 物流商对账明细分页 COUNT（轻量，不 JOIN 主表）
     * @author Will
     * @date: 2026/07/16
     * @param params 查询参数
     * @return 总条数
     */
    Long pagingCount(@Param("params") LogisticsReconDetailDTO.PagingParamDTO params);

    /**
     * 物流商对账明细分页列表（延迟关联：内层 id 分页，外层回表）
     * @author Will
     * @date: 2026/07/16
     * @param params 查询参数
     * @param offset 偏移量
     * @param limit  每页条数
     * @return 当前页列表
     */
    List<LogisticsReconDetailDTO.ListDTO> paging(@Param("params") LogisticsReconDetailDTO.PagingParamDTO params,
                                                 @Param("offset") long offset,
                                                 @Param("limit") long limit);

    /**
     * 物流商对账费用项 TAB 数量统计（按 match_status 分组）
     * @author Will
     * @date: 2026/05/29
     * @param param
     * @return List<LogisticsReconDetailDTO.TabListDTO>
     */
    List<LogisticsReconDetailDTO.TabListDTO> tabList(@Param("params") LogisticsReconDetailDTO.TabListParamDTO param);
}
