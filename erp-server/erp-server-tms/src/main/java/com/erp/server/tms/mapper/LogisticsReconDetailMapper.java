package com.erp.server.tms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
     * 物流商对账明细分页列表查询
     * @author Will
     * @date: 2026/05/29
     * @param query
     * @param params
     * @return IPage<LogisticsReconDetailDTO.ListDTO>
     */
    IPage<LogisticsReconDetailDTO.ListDTO> paging(Page<LogisticsReconDetailDTO.ListDTO> query,
                                                  @Param("params") LogisticsReconDetailDTO.PagingParamDTO params);

    /**
     * 按主表 id 统计已匹配费用项数（match_status = matched）
     * @author Will
     * @date: 2026/05/29
     * @param mainId
     * @return Integer
     */
    Integer countMatchedByMainId(@Param("mainId") String mainId);

    /**
     * 物流商对账费用项 TAB 数量统计（按 match_status 分组）
     * @author Will
     * @date: 2026/05/29
     * @param param
     * @return List<LogisticsReconDetailDTO.TabListDTO>
     */
    List<LogisticsReconDetailDTO.TabListDTO> tabList(@Param("params") LogisticsReconDetailDTO.TabListParamDTO param);
}
