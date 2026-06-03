package com.erp.server.tms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.tms.dto.LogisticsReconDTO;
import com.erp.model.tms.entity.LogisticsReconEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 物流商对账单（主表） Mapper 接口
 * </p>
 *
 * @author Will
 * @since 2026-05-29
 */
@Mapper
public interface LogisticsReconMapper extends BaseMapper<LogisticsReconEntity> {

    /**
     * 物流商对账单分页列表查询
     * @author Will
     * @date: 2026/05/29
     * @param query
     * @param params
     * @return IPage<LogisticsReconDTO.ListDTO>
     */
    IPage<LogisticsReconDTO.ListDTO> paging(Page<LogisticsReconDTO.ListDTO> query,
                                            @Param("params") LogisticsReconDTO.PagingParamDTO params);

    /**
     * 物流商对账单 TAB 状态统计（按 check_status[importing/pending/confirmed] 分组聚合）
     * @author Will
     * @date: 2026/05/29
     * @param params
     * @return List<LogisticsReconDTO.TabListDTO>
     */
    List<LogisticsReconDTO.TabListDTO> tabList(@Param("params") LogisticsReconDTO.PagingParamDTO params);
}
